/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.data.converter.HTSEntryPointConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.*;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.HTSAdolescentsQualityOfCareCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.*;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemr.extras.report.HIVHTSReport" })
public class HIVHTSReportBuilder extends AbstractHybridReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	/**
	 * @see org.openmrs.module.kenyacore.report.builder.AbstractCohortReportBuilder#addColumns(org.openmrs.module.kenyacore.report.CohortReportDescriptor,
	 *      PatientDataSetDefinition)
	 */
	@Override
	protected void addColumns(HybridReportDescriptor report, PatientDataSetDefinition dsd) {
		
	}
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor descriptor, PatientDataSetDefinition dsd) {
		return null;
	}
	
	protected Mapped<CohortDefinition> htsAdolescentsCohort() {
		CohortDefinition cd = new HTSAdolescentsQualityOfCareCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("HTS Adolescents");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	String paramMapping = "startDate=${startDate},endDate=${endDate}";
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition htsAdolescentsDataSetDefinition = htsAdolescentsDefinition("htsAdolescents");
		htsAdolescentsDataSetDefinition.addRowFilter(htsAdolescentsCohort());
		DataSetDefinition htsPedsDSD = htsAdolescentsDataSetDefinition;
		
		return Arrays.asList(ReportUtils.map(htsPedsDSD, "startDate=${startDate},endDate=${endDate}"));
		
	}
	
	protected PatientDataSetDefinition htsAdolescentsDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		
		HTSEntryPointDataDefinition entryPointDataDefinition = new HTSEntryPointDataDefinition();
		entryPointDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		entryPointDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Entry Point", entryPointDataDefinition, indParams, new HTSEntryPointConverter());
		
		HTSDateDataDefinition htsDateDataDefinition = new HTSDateDataDefinition();
		htsDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		htsDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date tested", htsDateDataDefinition, indParams, new DateConverter(DATE_FORMAT));
		
		FinalHTSResultDataDefinition finalHTSResultDataDefinition = new FinalHTSResultDataDefinition();
		finalHTSResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		finalHTSResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Final result", finalHTSResultDataDefinition, indParams, null);
		
		FinalHTSResultGivenDataDefinition finalHTSResultGivenDataDefinition = new FinalHTSResultGivenDataDefinition();
		finalHTSResultGivenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		finalHTSResultGivenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Results Received", finalHTSResultGivenDataDefinition, indParams, null);
		
		RetestedBeforeARTInititionDataDefinition confirmatoryTestBeforeEnrollmentDataDefinition = new RetestedBeforeARTInititionDataDefinition();
		confirmatoryTestBeforeEnrollmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		confirmatoryTestBeforeEnrollmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Confirmatory Tests Done Before Enrollment", confirmatoryTestBeforeEnrollmentDataDefinition,
		    indParams, null);
		
		ReferredForServicesDataDefinition referredForServicesDataDefinition = new ReferredForServicesDataDefinition();
		referredForServicesDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		referredForServicesDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Referred for Services", referredForServicesDataDefinition, indParams, null);
		
		return dsd;
	}
}
