package org.motechproject.openmrs.services;

import ch.lambdaj.function.convert.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.motechproject.mrs.model.Attribute;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.mrs.services.MRSPatientAdaptor;
import org.motechproject.openmrs.IdentifierType;
import org.motechproject.openmrs.helper.PatientHelper;
import org.openmrs.*;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static ch.lambdaj.Lambda.*;

public class OpenMRSPatientAdaptor implements MRSPatientAdaptor {

    @Autowired
    PatientService patientService;

    @Autowired
    PersonService personService;

    @Autowired
    UserService userService;

    @Autowired
    OpenMRSFacilityAdaptor facilityAdaptor;
    @Autowired
    OpenMRSPersonAdaptor personAdaptor;

    @Autowired
    PatientHelper patientHelper;

    @Override
    public MRSPatient getPatient(String patientId) {
        org.openmrs.Patient openMrsPatient = getOpenMrsPatient(patientId);
        return (openMrsPatient == null) ? null : getMrsPatient(openMrsPatient);
    }

    @Override
    public MRSPatient getPatientByMotechId(String motechId) {
        PatientIdentifierType motechIdType = patientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName());
        List<PatientIdentifierType> idTypes = new ArrayList<PatientIdentifierType>();
        idTypes.add(motechIdType);

        List<org.openmrs.Patient> patients = patientService.getPatients(null, motechId, idTypes, true);
        if (CollectionUtils.isEmpty(patients)) {
            return null;
        }
        return getMrsPatient(patients.get(0));
    }

    @Override
    public MRSPatient savePatient(MRSPatient patient) {
        final org.openmrs.Patient openMRSPatient = patientHelper.buildOpenMrsPatient(patient, userService.generateSystemId(),
                getPatientIdentifierType(IdentifierType.IDENTIFIER_MOTECH_ID),
                facilityAdaptor.getLocation(patient.getFacility().getId()), getAllPersonAttributeTypes());

        return getMrsPatient(patientService.savePatient(openMRSPatient));
    }

    public MRSPatient getMrsPatient(org.openmrs.Patient savedPatient) {
        final List<Attribute> attributes = project(savedPatient.getAttributes(), Attribute.class,
                on(PersonAttribute.class).getAttributeType().toString(), on(PersonAttribute.class).getValue());
        Set<PersonName> personNames = savedPatient.getNames();
        PersonName personName = personAdaptor.getFirstName(personNames);
        final PatientIdentifier patientIdentifier = savedPatient.getPatientIdentifier();
        MRSFacility mrsFacility = (patientIdentifier != null) ? facilityAdaptor.convertLocationToFacility(patientIdentifier.getLocation()) : null;
        String motechId = (patientIdentifier != null) ? patientIdentifier.getIdentifier() : null;
        MRSPerson mrsPerson = new MRSPerson().firstName(personName.getGivenName()).middleName(personName.getMiddleName()).lastName(personName.getFamilyName()).
                preferredName(personAdaptor.getPreferredName(personNames)).birthDateEstimated(savedPatient.getBirthdateEstimated()).
                gender(savedPatient.getGender()).address(patientHelper.getAddress(savedPatient)).attributes(attributes).dateOfBirth(savedPatient.getBirthdate());
        if (savedPatient.getPersonId() != null) {
            mrsPerson.id(Integer.toString(savedPatient.getPersonId()));
        }
        return new MRSPatient(String.valueOf(savedPatient.getId()), motechId, mrsPerson, mrsFacility);
    }

    public PatientIdentifierType getPatientIdentifierType(IdentifierType identifierType) {
        return patientService.getPatientIdentifierTypeByName(identifierType.getName());
    }

    private List<PersonAttributeType> getAllPersonAttributeTypes() {
        return personService.getAllPersonAttributeTypes(false);
    }

    public org.openmrs.Patient getOpenMrsPatient(String patientId) {
        return patientService.getPatient(Integer.parseInt(patientId));
    }

    @Override
    public List<MRSPatient> search(String name, String id) {
        List<MRSPatient> patients = convert(patientService.getPatients(name, id, Arrays.asList(patientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName())), false),
                new Converter<Patient, MRSPatient>() {
                    @Override
                    public MRSPatient convert(Patient patient) {
                        return getMrsPatient(patient);
                    }
                });
        Collections.sort(patients, new Comparator<MRSPatient>() {
            @Override
            public int compare(MRSPatient personToBeCompared1, MRSPatient personToBeCompared2) {
                if (personToBeCompared1.getPerson().getFirstName() == null && personToBeCompared2.getPerson().getFirstName() == null) {
                    return personToBeCompared1.getMotechId().compareTo(personToBeCompared2.getMotechId());
                } else {
                    if (personToBeCompared1.getPerson().getFirstName() == null) {
                        return -1;
                    } else if (personToBeCompared2.getPerson().getFirstName() == null) {
                        return 1;
                    } else {
                        return personToBeCompared1.getPerson().getFirstName().compareTo(personToBeCompared2.getPerson().getFirstName());
                    }
                }
            }
        });
        return patients;
    }
}
