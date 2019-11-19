/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.library.SurgeReport;

import org.openmrs.module.kenyacore.report.ReportUtils;
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
public class SurgeIndicatorLibrary {
	
	@Autowired
	private SurgeCohortLibrary surgeCohorts;
	
	/**
	 * Number of patients who are currently on ART
	 * 
	 * @return the indicator
	 */
	public CohortIndicator currentlyOnArt() {
		return cohortIndicator("Currently on ART",
		    ReportUtils.map(surgeCohorts.currentOnArt(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Disaggregated by Age / Sex
	 * 
	 * @return the indicator
	 */
	public CohortIndicator newOnArt() {
		return cohortIndicator("Newly Started ART",
		    ReportUtils.<CohortDefinition> map(surgeCohorts.newOnArt(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Disaggregated by Age / Sex
	 * 
	 * @return the indicator
	 */
	public CohortIndicator ltfuRecent() {
		return cohortIndicator("Patients who are turned to LTFU during the reporting period",
		    ReportUtils.<CohortDefinition> map(surgeCohorts.ltfuRecent(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Disaggregated by Age / Sex
	 * 
	 * @return the indicator
	 */
	public CohortIndicator ltfuRTC() {
		return cohortIndicator("Patients who returned to care during the reporting period after being LTFU",
		    ReportUtils.<CohortDefinition> map(surgeCohorts.ltfuRTC(), "startDate=${startDate},endDate=${endDate}"));
	}
	
}
