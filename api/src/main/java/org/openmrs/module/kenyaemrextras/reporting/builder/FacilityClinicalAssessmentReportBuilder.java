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
		cohortDsd.addColumn("TX_CURR", "",
		    ReportUtils.map(datimIndicators.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_NEW", "",
		    ReportUtils.map(assessmentIndicatorLibrary.txNew(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_ML(IIT)", "",
		    ReportUtils.map(datimIndicators.txML(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TX_RTT", "",
		    ReportUtils.map(datimIndicators.txRTT(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd
		        .addColumn("Eligible for VL test", "", ReportUtils.map(assessmentIndicatorLibrary.eligibleForVlTest(),
		            "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("VL Uptake", "",
		    ReportUtils.map(assessmentIndicatorLibrary.vlUptake(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Suppressed VL", "",
		    ReportUtils.map(assessmentIndicatorLibrary.suppressedVL(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("DSD (MMD)", "",
		    ReportUtils.map(assessmentIndicatorLibrary.dsd(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("OTZ", "",
		    ReportUtils.map(assessmentIndicatorLibrary.otz(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("OVC", "",
		    ReportUtils.map(assessmentIndicatorLibrary.ovc(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Covid Vaccination", "",
		    ReportUtils.map(assessmentIndicatorLibrary.covidVaccination(), "startDate=${startDate},endDate=${endDate}"), "");
		/*cohortDsd.addColumn("eHTS numerator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.htsNumberTested(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("eHTS denominator", "",
		    ReportUtils.map(moh731Indicators.htsNumberTested(), "startDate=${startDate},endDate=${endDate}"), "");*/
		cohortDsd.addColumn("PrEP numerator", "", ReportUtils.map(assessmentIndicatorLibrary.newlyEnrolledInPrEPRegister(),
		    "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("PrEP denominator", "",
		    ReportUtils.map(datimIndicators.newlyEnrolledInPrEP(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("KP numerator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.kpClientsNumerator(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		/*cohortDsd.addColumn("KP denominator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.kpClientsQuarterly(), "startDate=${startDate},endDate=${endDate}"),
		    "");*/
		cohortDsd.addColumn("MCH", "",
		    ReportUtils.map(assessmentIndicatorLibrary.activeInMCH(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("TB", "",
		    ReportUtils.map(assessmentIndicatorLibrary.activeInTBRx(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd
		        .addColumn("ART_Verification_Adults numerator", "", ReportUtils.map(
		            assessmentIndicatorLibrary.artVerifiedAdults(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Adults denominator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.txCurrAdults(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Peds numerator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.artVerifiedPeds(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("ART_Verification_Peds denominator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.txCurrPeds(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Lab_Manifest numerator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.vlTestsWithResults(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("Lab_Manifest denominator", "",
		    ReportUtils.map(assessmentIndicatorLibrary.vlTestsOrdered(), "startDate=${startDate},endDate=${endDate}"), "");
		cohortDsd.addColumn("Unsuppressed VL-Peds", " (under 18 years with Unsuppressed repeat VLs and EAC)",
		    ReportUtils.map(assessmentIndicatorLibrary.unsuppressedVLPeds(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		cohortDsd.addColumn("Unsuppressed VL-Adults", " (18+ years with Unsuppressed repeat VLs and EAC)",
		    ReportUtils.map(assessmentIndicatorLibrary.unsuppressedVLAdults(), "startDate=${startDate},endDate=${endDate}"),
		    "");
		
		return cohortDsd;
		
	}
}
