/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.kenyaemrextras.reporting.library;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.kenyaemr.reporting.EmrReportingUtils.cohortIndicator;

/**
 * Library of HIV related indicator definitions.
 */
@Component
public class FacilityClinicalAssessmentIndicatorLibrary {
	
	@Autowired
	private FacilityClinicalAssessmentCohortLibrary cohortLibrary;
	
	/**
	 * TX_ML
	 */
	public CohortIndicator txML() {
		return cohortIndicator("TX_ML", ReportUtils.map(cohortLibrary.txML(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR who have been on treatment for at least six months and eligible for VL test
	 * 
	 * @return the indicator
	 */
	public CohortIndicator eligibleForVlTest() {
		return cohortIndicator("TX_CURR who have been on treatment for at least six months",
		    ReportUtils.map(cohortLibrary.eligibleForVlTest(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR who have been on treatment for at least six months and had a VL test result within
	 * the reporting period
	 * 
	 * @return the indicator
	 */
	public CohortIndicator vlUptake() {
		return cohortIndicator(
		    "TX_CURR who have been on treatment for at least six months and had a VL test result within the reporting period",
		    ReportUtils.map(cohortLibrary.vlUptake(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR who had Suppressed VL test result within reporting period
	 * 
	 * @return
	 */
	public CohortIndicator suppressedVL() {
		return cohortIndicator("TX_CURR who had Suppressed VL test result within reporting period",
		    ReportUtils.map(cohortLibrary.suppressedVL(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR with MMD (Multi-month dispense)
	 * 
	 * @return
	 */
	public CohortIndicator dsd() {
		return cohortIndicator("TX_CURR with MMD",
		    ReportUtils.map(cohortLibrary.dsd(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR aged between 10-19 years
	 * 
	 * @return
	 */
	public CohortIndicator otz() {
		return cohortIndicator("TX_CURR aged between 10-19 years",
		    ReportUtils.map(cohortLibrary.otz(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR aged between 0-17 years
	 * 
	 * @return
	 */
	public CohortIndicator ovc() {
		return cohortIndicator("TX_CURR aged between 0-17 years",
		    ReportUtils.map(cohortLibrary.ovc(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * eHTS
	 * 
	 * @return
	 */
	public CohortIndicator eHTS() {
		return cohortIndicator("Total Tests",
		    ReportUtils.map(cohortLibrary.eHTS(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR aged 15+ years who are vaccinated against Covid-19
	 * 
	 * @return
	 */
	public CohortIndicator covidVaccination() {
		return cohortIndicator("TX_CURR aged 15+ years who are vaccinated against Covid-19",
		    ReportUtils.map(cohortLibrary.covidVaccination(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Number of tests captured in eHTS from the HTS Register
	 * 
	 * @return
	 */
	public CohortIndicator htsNumberTested() {
		return cohortIndicator("Number of tests captured in eHTS from the HTS Register",
		    ReportUtils.map(cohortLibrary.htsNumberTested(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Newly initiated PrEP - From PrEP register
	 * 
	 * @return
	 */
	public CohortIndicator newlyEnrolledInPrEPRegister() {
		return cohortIndicator("Newly initiated PrEP - From PrEP register",
		    ReportUtils.map(cohortLibrary.newlyEnrolledInPrEPRegister(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * KP Clients monthly
	 * 
	 * @return
	 */
	public CohortIndicator kpClientsNumerator() {
		return cohortIndicator("Number of clients newly initiated on PrEP ",
		    ReportUtils.map(cohortLibrary.kpClientsNumerator(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Number of KP clients Quarterly
	 * 
	 * @return
	 */
	public CohortIndicator kpClientsQuarterly() {
		return cohortIndicator("Number of KP clients Quarterly ",
		    ReportUtils.map(cohortLibrary.kpClientsQuarterly(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR who are active in PMTCT
	 * 
	 * @return
	 */
	public CohortIndicator activeInMCHAndTXCurr() {
		return cohortIndicator("TX_CURR who are active in PMTCT",
		    ReportUtils.map(cohortLibrary.activeInMCHAndTXCurr(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR who are active in TB treatment
	 * 
	 * @return
	 */
	public CohortIndicator activeInTBRx() {
		return cohortIndicator("TX_CURR who are active in TB treatment",
		    ReportUtils.map(cohortLibrary.activeInTBRx(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR (adults, 18+)
	 * 
	 * @return
	 */
	public CohortIndicator txCurrAdults() {
		return cohortIndicator("TX_CURR (adults, 18+) ",
		    ReportUtils.map(cohortLibrary.txCurrAdults(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Number of children contacts with undocumented HIV status
	 * 
	 * @return the indicator
	 */
	public CohortIndicator artVerifiedAdults() {
		return cohortIndicator("Children Contacts with undocumented HIV status",
		    ReportUtils.map(cohortLibrary.artVerifiedAdults(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR peds (<18 years)
	 * 
	 * @return
	 */
	public CohortIndicator txCurrPeds() {
		return cohortIndicator("TX_Curr Peds",
		    ReportUtils.map(cohortLibrary.txCurrPeds(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * TX_CURR (peds, <18) with Birth cert no. captured
	 * 
	 * @return
	 */
	public CohortIndicator artVerifiedPeds() {
		return cohortIndicator("TX_CURR (peds, <18) with Birth cert no. captured",
		    ReportUtils.map(cohortLibrary.artVerifiedPeds(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Number of VL tests with results - Lab Manifest
	 * 
	 * @return the indicator
	 */
	public CohortIndicator vlTestsWithResults() {
		return cohortIndicator("Number of VL tests with results",
		    ReportUtils.map(cohortLibrary.vlTestsWithResults(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Number of VL tests ordered - Lab manifest
	 * 
	 * @return the indicator
	 */
	public CohortIndicator vlTestsOrdered() {
		return cohortIndicator("Number of VL tests ordered",
		    ReportUtils.map(cohortLibrary.vlTestsOrdered(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Children (<18 years) with unsuppressed VL
	 * 
	 * @return
	 */
	public CohortIndicator unsuppressedVLPeds() {
		return cohortIndicator("Children (<18 years) with unsuppressed VL",
		    ReportUtils.map(cohortLibrary.unsuppressedVLPeds(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Adults (18+ years) with unsuppressed VL
	 * 
	 * @return
	 */
	public CohortIndicator unsuppressedVLAdults() {
		return cohortIndicator("Adults (18+ years) with unsuppressed VL",
		    ReportUtils.map(cohortLibrary.unsuppressedVLAdults(), "startDate=${startDate},endDate=${endDate}"));
	}
}
