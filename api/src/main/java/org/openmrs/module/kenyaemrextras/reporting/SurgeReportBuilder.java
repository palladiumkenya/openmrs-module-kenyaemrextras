/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.reporting.ColumnParameters;
import org.openmrs.module.kenyaemr.reporting.EmrReportingUtils;
import org.openmrs.module.kenyaemr.reporting.library.shared.common.CommonDimensionLibrary;
import org.openmrs.module.kenyaemrextras.reporting.library.SurgeReport.SurgeIndicatorLibrary;
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
 * Surge reporting for CDC 10th June, 2019
 */
@Component
@Builds({ "kenyaemrextras.common.report.weeklysurge" })
public class SurgeReportBuilder extends AbstractReportBuilder {
	
	protected static final Log log = LogFactory.getLog(SurgeReportBuilder.class);
	
	@Autowired
	private CommonDimensionLibrary commonDimensions;
	
	@Autowired
	private SurgeIndicatorLibrary datimIndicators;
	
	/**
	 * @see AbstractReportBuilder#getParameters(ReportDescriptor)
	 */
	@Override
	protected List<Parameter> getParameters(ReportDescriptor descriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	/**
	 * @see AbstractReportBuilder#buildDataSets(ReportDescriptor, ReportDefinition)
	 */
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		return Arrays.asList(ReportUtils.map(careAndTreatmentDataSet(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Creates the dataset for section #2: Prevention of Mother-to-Child Transmission
	 * 
	 * @return the dataset
	 */
	
	/**
	 * Creates the dataset for section #3: Care and Treatment
	 * 
	 * @return The dataset
	 */

	protected DataSetDefinition careAndTreatmentDataSet() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.setName("WeeklySurge");
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cohortDsd.addDimension("age", ReportUtils.map(commonDimensions.datimFineAgeGroups(), "onDate=${endDate}"));
		cohortDsd.addDimension("gender", ReportUtils.map(commonDimensions.gender()));
		
		ColumnParameters colTotal = new ColumnParameters(null, "Total", "");
		
		List<ColumnParameters> disaggregation = Arrays.asList(colTotal);
		
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		EmrReportingUtils.addRow(cohortDsd, "TX_New", "Newly Started ART",
		    ReportUtils.map(datimIndicators.newOnArt(), indParams), disaggregation, Arrays.asList("25"));
		
		//Number of Adults and Children with HIV infection receiving ART By Age/Sex Disagreggation
		EmrReportingUtils.addRow(cohortDsd, "TX_CURR", "Adults and Children with HIV infection receiving ART",
		    ReportUtils.map(datimIndicators.currentlyOnArt(), indParams), disaggregation, Arrays.asList("25"));
		
		EmrReportingUtils.addRow(cohortDsd, "LTFU_RECENT", "LTFU in the previous reporting period",
		    ReportUtils.map(datimIndicators.ltfuRecent(), indParams), disaggregation, Arrays.asList("01"));
		
		EmrReportingUtils.addRow(cohortDsd, "LTFU_RTC", "LTFU returned to care during the reporting period",
		    ReportUtils.map(datimIndicators.ltfuRTC(), indParams), disaggregation, Arrays.asList("01"));
		
		return cohortDsd;
		
	}
}
