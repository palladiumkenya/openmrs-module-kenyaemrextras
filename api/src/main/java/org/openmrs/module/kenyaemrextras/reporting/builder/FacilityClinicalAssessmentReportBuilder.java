/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.builder;

import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731Greencard.ETLMoh731GreenCardIndicatorLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.RevisedDatim.DatimIndicatorLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.publicHealthActionReport.PublicHealthActionIndicatorLibrary;
import org.openmrs.module.kenyaemrextras.reporting.library.FacilityClinicalAssessmentIndicatorLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Report builder for facility clinical assessment report
 */
@Component
@Builds({ "kenyaemrextras.common.report.facilityClinicalAssessment" })
public class FacilityClinicalAssessmentReportBuilder extends AbstractReportBuilder {
	
	@Autowired
	private FacilityClinicalAssessmentIndicatorLibrary assessmentIndicatorLibrary;
	
	@Autowired
	DatimIndicatorLibrary datimIndicators;
	
	@Autowired
	FacilityClinicalAssessmentIndicatorLibrary clinicalActionIndicators;
	
	@Autowired
	ETLMoh731GreenCardIndicatorLibrary moh731Indicators;
	
	@Autowired
	private PublicHealthActionIndicatorLibrary publicHealthActionIndicatorLibrary;
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor,
	        ReportDefinition reportDefinition) {
		return Arrays.asList(ReportUtils.map(clinicalAssessment(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition clinicalAssessment() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cohortDsd.setName("Clinical-Assessment");
		cohortDsd.setDescription("Clinical Assessment Report");
		cohortDsd.addColumn("TX_CURR", " (Current on ART)",
		    ReportUtils.map(datimIndicators.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_NEW", " (Started on ART)",
		    ReportUtils.map(datimIndicators.newlyStartedARTByAgeSex(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_ML(IIT)", " (Clients who Interrupted treatment)",
		    ReportUtils.map(assessmentIndicatorLibrary.txML(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_RTT", "(Clients restarting treatment after interruption)",
		    ReportUtils.map(datimIndicators.txRTT(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd
		        .addColumn("VL Testing denominator",
		            " (Excludes TX_CURR who have been on treatment for less than six months)", ReportUtils.map(
		                assessmentIndicatorLibrary.eligibleForVlTest(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("VL Testing numerator", " (Clients with a valid VL result)",
		    ReportUtils.map(assessmentIndicatorLibrary.vlUptake(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("VL Test Results < 1000 copies/ml ", "",
		    ReportUtils.map(assessmentIndicatorLibrary.suppressedVL(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Unsuppressed VL-Peds", " (under 18 years with Unsuppressed repeat VLs and EAC)",
		    ReportUtils.map(assessmentIndicatorLibrary.unsuppressedVLPeds(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("Unsuppressed VL-Adults", " (18+ years with Unsuppressed repeat VLs and EAC)",
		    ReportUtils.map(assessmentIndicatorLibrary.unsuppressedVLAdults(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("DSD (MMD)", " (TX_CURR with ≥ 3 months prescriptions)",
		    ReportUtils.map(assessmentIndicatorLibrary.dsd(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("OTZ", " (TX_CURR aged between 10-19 years active in OTZ module)",
		    ReportUtils.map(assessmentIndicatorLibrary.otz(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("OVC", "(TX_CURR aged between 0-17 years active in OVC module)",
		    ReportUtils.map(assessmentIndicatorLibrary.ovc(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Covid Vaccination", "(TX_CURR aged 15+ years fully vaccinated)",
		    ReportUtils.map(assessmentIndicatorLibrary.covidVaccination(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("PrEP numerator", "Number of clients newly initiated on PrEP",
		    ReportUtils.map(datimIndicators.newlyEnrolledInPrEP(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("HTS", " (Total tested)",
		    ReportUtils.map(assessmentIndicatorLibrary.eHTS(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("KP numerator", " (Clients currently in KP program)",
		    ReportUtils.map(assessmentIndicatorLibrary.kpClientsNumerator(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("MCH", " (TX_CURR active in MCH module)",
		    ReportUtils.map(assessmentIndicatorLibrary.activeInMCHAndTXCurr(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("TB", " (TX_CURR active in TB module or on anti-TB drugs as documented in greencard)",
		    ReportUtils.map(assessmentIndicatorLibrary.activeInTBRx(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd
		        .addColumn("ART_Verification_Adults numerator", " (TX_CURR 18+ years old assigned a NUPI)", ReportUtils.map(
		            assessmentIndicatorLibrary.artVerifiedAdults(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Adults denominator", " (TX_CURR 18+ years old)",
		    ReportUtils.map(assessmentIndicatorLibrary.txCurrAdults(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Peds numerator", " (TX_CURR under 18 years old assigned a NUPI)",
		    ReportUtils.map(assessmentIndicatorLibrary.artVerifiedPeds(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Peds denominator", " (TX_CURR under 18 years old)",
		    ReportUtils.map(assessmentIndicatorLibrary.txCurrPeds(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Lab Manifest", " (Number of VL test results received)",
		    ReportUtils.map(assessmentIndicatorLibrary.vlTestsWithResults(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("Lab Manifest denominator", " (Number of Samples sent)",
		    ReportUtils.map(assessmentIndicatorLibrary.vlTestsOrdered(), "startDate=${startDate},endDate=${endDate}"), "");
		
		return cohortDsd;
		
	}
}
