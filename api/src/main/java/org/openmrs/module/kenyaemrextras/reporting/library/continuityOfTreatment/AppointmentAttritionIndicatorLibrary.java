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
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.KPTypeDataDefinition;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.RevisedDatim.DatimCohortLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.kenyaemr.reporting.EmrReportingUtils.cohortIndicator;

/**
 * Library of DATIM related indicator definitions. All indicators require parameters ${startDate}
 * and ${endDate}
 */
@Component
public class AppointmentAttritionIndicatorLibrary {
	
	@Autowired
	private AppointmentAttritionCohortLibrary cotCohorts;
	
	public CohortIndicator totalAppointments() {
		return cohortIndicator("Total Appointments",
		    ReportUtils.<CohortDefinition> map(cotCohorts.totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator missedAppointments() {
		return cohortIndicator("Missed Appointments",
		    ReportUtils.<CohortDefinition> map(cotCohorts.missedAppointments(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator rtcWithin7Days() {
		return cohortIndicator("Return to care within 7 days",
		    ReportUtils.<CohortDefinition> map(cotCohorts.rtcWithin7Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator rtcBetween8And30Days() {
		return cohortIndicator("Return to care within 30 days", ReportUtils.<CohortDefinition> map(
		    cotCohorts.rtcBetween8And30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator iitOver30Days() {
		return cohortIndicator("Missed appointment for over 30 days",
		    ReportUtils.<CohortDefinition> map(cotCohorts.iitOver30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator rtcOver30Days() {
		return cohortIndicator("Return to care after 30 days missed appointment",
		    ReportUtils.<CohortDefinition> map(cotCohorts.rtcOver30Days(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	public CohortIndicator currentIIT() {
		return cohortIndicator("All Current IIT",
		    ReportUtils.<CohortDefinition> map(cotCohorts.currentIIT(), "startDate=${startDate},endDate=${endDate}"));
	}
}
