/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2011 Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, nor its respective contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA AND ITS CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION USA OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package org.motechproject.server.appointments;

import org.motechproject.appointments.api.EventKeys;
import org.motechproject.appointments.api.dao.AppointmentsDAO;
import org.motechproject.appointments.api.dao.RemindersDAO;
import org.motechproject.appointments.api.model.Reminder;
import org.motechproject.context.Context;
import org.motechproject.model.MotechEvent;
import org.motechproject.model.RepeatingSchedulableJob;
import org.motechproject.model.RunOnceSchedulableJob;
import org.motechproject.server.event.EventListener;
import org.motechproject.server.gateway.MotechSchedulerGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * 
 */
public class ReminderCRUDEventHandler implements EventListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	public final static String REMINDER_CRUD_HANDLER = "ReminderCRUDHandler";

	private MotechSchedulerGateway schedulerGateway = Context.getInstance().getMotechSchedulerGateway();

	@Autowired
	private AppointmentsDAO appointmentsDAO;

    @Autowired
    private RemindersDAO remindersDAO;

	@Override
	public void handle(MotechEvent event) {

        if (event.getSubject().endsWith("deleted")) {
            String jobId = EventKeys.getJobId(event);

            if (null != jobId) {
		        schedulerGateway.unscheduleJob(jobId);
            }
        }

        if (event.getSubject().endsWith("created")) {
            Reminder reminder = remindersDAO.getReminder(EventKeys.getReminderId(event));
            reminder.setJobId(UUID.randomUUID().toString());

            // This will publish an updated event so no need to talk to the scheduler twice, just wait for
            // the event to get here.
            remindersDAO.updateReminder(reminder);
        }

        if (event.getSubject().endsWith("updated")) {
            String jobId = EventKeys.getJobId(event);

            Reminder reminder = remindersDAO.getReminder(EventKeys.getReminderId(event));
            if (reminder.getEnabled()) {
                String appointmentId = EventKeys.getAppointmentId(event);
                if (null == appointmentId || 0 == appointmentId.length()) {
                    logger.error("Can not handle Event: " + event.getSubject() +
                             ". The event is invalid - missing the " + EventKeys.APPOINTMENT_ID_KEY + " parameter");
                    return;
                }

                MotechEvent reminderEvent = new MotechEvent(EventKeys.REMINDER_EVENT_SUBJECT, event.getParameters());

                // This isn't the best model object, but basically if there are no units specified then it is a single
                // reminder otherwise it is a repeating job
                if (null != reminder.getUnits()) {
                    RepeatingSchedulableJob schedulableJob = new RepeatingSchedulableJob(reminderEvent,
                                                                                         reminder.getStartDate(),
                                                                                         reminder.getEndDate(),
                                                                                         reminder.getRepeatCount(),
                                                                                         reminder.getIntervalSeconds() * 1000);
                } else {
                    RunOnceSchedulableJob schedulableJob = new RunOnceSchedulableJob(reminderEvent, reminder.getStartDate());
                    schedulerGateway.scheduleRunOnceJob(schedulableJob);
                }
            } else {
                schedulerGateway.unscheduleJob(jobId);
            }
    	}
    }

	@Override
	public String getIdentifier() {
		return REMINDER_CRUD_HANDLER;
	}
}
