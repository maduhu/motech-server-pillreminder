package org.motechproject.scheduletracking.api.domain.filtering;

import org.junit.Test;
import org.motechproject.scheduletracking.api.domain.Enrollment;
import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class StatusCriterionTest {

    @Test
    public void shouldFilterByExternalId() {
        List<Enrollment> allEnrollments = new ArrayList<Enrollment>();
        allEnrollments.add(new Enrollment(null, null, null, null, null, null, EnrollmentStatus.COMPLETED, null));
        allEnrollments.add(new Enrollment(null, null, null, null, null, null, EnrollmentStatus.ACTIVE, null));
        allEnrollments.add(new Enrollment(null, null, null, null, null, null, EnrollmentStatus.DEFAULTED, null));
        allEnrollments.add(new Enrollment(null, null, null, null, null, null, EnrollmentStatus.ACTIVE, null));

        List<Enrollment> filteredEnrollments = new StatusCriterion(asList(new EnrollmentStatus[]{ EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED})).filter(allEnrollments, null);
        assertEquals(asList(new EnrollmentStatus[]{ EnrollmentStatus.COMPLETED, EnrollmentStatus.ACTIVE, EnrollmentStatus.ACTIVE}), extract(filteredEnrollments, on(Enrollment.class).getStatus()));
    }
}