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
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.*;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.ARTPedsQualityOfCareCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.*;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsCTXDispensedDataDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
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
@Builds({ "kenyaemr.extras.report.HIVARTReport" })
public class HIVARTReportBuilder extends AbstractHybridReportBuilder {
	
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
	
	protected Mapped<CohortDefinition> activePedsCohort() {
		CohortDefinition cd = new ARTPedsQualityOfCareCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Active Peds");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	String paramMapping = "startDate=${startDate},endDate=${endDate}";
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition activePedsDataSetDefinition = activePedsDataSetDefinition("activePeds");
		activePedsDataSetDefinition.addRowFilter(activePedsCohort());
		DataSetDefinition activePedsDSD = activePedsDataSetDefinition;
		
		return Arrays.asList(ReportUtils.map(activePedsDSD, "startDate=${startDate},endDate=${endDate}"));
		
	}
	
	protected PatientDataSetDefinition activePedsDataSetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		DQAWeightDataDefinition weightDataDefinition = new DQAWeightDataDefinition();
		weightDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		weightDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Weight", weightDataDefinition, indParams, null);
		
		DQAHeightDataDefinition heightDataDefinition = new DQAHeightDataDefinition();
		heightDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		heightDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Height", heightDataDefinition, indParams, null);
		
		dsd.addColumn("Date Enrolled in HIV Care", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		
		ETLArtStartDateDataDefinition artInitiationDataDefinition = new ETLArtStartDateDataDefinition();
		artInitiationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		artInitiationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Date Started ART", artInitiationDataDefinition, indParams, new DateConverter(DATE_FORMAT));
		
		dsd.addColumn("Start Regimen", new ETLFirstRegimenDataDefinition(), "");
		
		DQACurrentRegimenDataDefinition currentRegimenDataDefinition = new DQACurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, indParams, null);
		dsd.addColumn("Baseline CD4 Date", new BaselineCD4DateDataDefinition(), "", null);
		dsd.addColumn("Baseline CD4", new DQABaselineCD4DataDefinition(), "", null);
		
		ETLLastVLDateDataDefinition lastVLDateDataDefinition = new ETLLastVLDateDataDefinition();
		lastVLDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVLDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last VL date", lastVLDateDataDefinition, paramMapping, null);
		
		ETLLastVLResultDataDefinition lastVLResultDataDefinition = new ETLLastVLResultDataDefinition();
		lastVLResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVLResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last VL result", lastVLResultDataDefinition, paramMapping, null);
		
		dsd.addColumn("CTX dispensed", new SimsCTXDispensedDataDefinition(), "");
		
		DQATBScreeningLastVisitOutcomeDataDefinition tbScreeningOutcomeDataDefinition = new DQATBScreeningLastVisitOutcomeDataDefinition();
		tbScreeningOutcomeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tbScreeningOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("TB Screening Outcome", tbScreeningOutcomeDataDefinition, indParams, null);
		
		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		
		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Next Appointment Date", nextAppointmentDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		
		return dsd;
	}
	
}
