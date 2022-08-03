/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.library.continuityOfTreatment;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.kenyaemr.reporting.EmrReportingUtils.cohortIndicator;

/**
 * Library of Appointment and Attrition report
 */
@Component
public class AppointmentAttritionIndicatorLibrary {
	
	@Autowired
	private AppointmentAttritionCohortLibrary cotCohorts;
	
	/**
	 * Total number of appointments within period
	 * 
	 * @return
	 */
	public CohortIndicator totalAppointments() {
		return cohortIndicator("Total Appointments",
		    ReportUtils.<CohortDefinition> map(cotCohorts.totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Total number of Missed appointments within period
	 * 
	 * @return
	 */
	public CohortIndicator missedAppointments() {
		return cohortIndicator("Missed Appointments",
		    ReportUtils.<CohortDefinition> map(cotCohorts.missedAppointments(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Missed Appointments and RTC within 7 days
	 * 
	 * @return
	 */
	public CohortIndicator rtcWithin7Days() {
		return cohortIndicator("Return to care within 7 days",
		    ReportUtils.<CohortDefinition> map(cotCohorts.rtcWithin7Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Missed Appointments and RTC after between 8 and 30 days
	 * 
	 * @return
	 */
	public CohortIndicator rtcBetween8And30Days() {
		return cohortIndicator("Return to care within 30 days", ReportUtils.<CohortDefinition> map(
		    cotCohorts.rtcBetween8And30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Missed appointment > 30 days within the period
	 * 
	 * @return
	 */
	public CohortIndicator iitOver30Days() {
		return cohortIndicator("Missed appointment for over 30 days",
		    ReportUtils.<CohortDefinition> map(cotCohorts.iitOver30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Missed Appointments and RTC after > 30 days
	 * 
	 * @return
	 */
	public CohortIndicator rtcOver30Days() {
		return cohortIndicator("Return to care after 30 days missed appointment",
		    ReportUtils.<CohortDefinition> map(cotCohorts.rtcOver30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Missed appointment and not yet RTC as of reporting period
	 * 
	 * @return
	 */
	public CohortIndicator currentIIT() {
		return cohortIndicator("All Current IIT",
		    ReportUtils.<CohortDefinition> map(cotCohorts.currentIIT(), "startDate=${startDate},endDate=${endDate}"));
	}
}
