/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.builder;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0201CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0202CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0203CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.LastHtsInitialResultDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.LastHtsRetestResultDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHivDiagnosisDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHivEnrollmentDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTracingAttemptsDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTracingOutcomeDataDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
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
@Builds({ "kenyaemrextras.common.report.simsReport" })
public class SimsReportBuilder extends AbstractHybridReportBuilder {
	
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
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		DataSetDefinition newlyInitiatedOnArtPatientsDSD = adultsNewlyInitiatedOnArtDataSetDefinition("S_02_01");
		DataSetDefinition missedAppointmentsDSD = missedAppointmentDatasetDefinition("S_02_02");
		DataSetDefinition sameDayInitiationDSD = sameDayARTInitiationDatasetDefinition("S_02_03");
		
		return Arrays.asList(ReportUtils.map(newlyInitiatedOnArtPatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedAppointmentsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(sameDayInitiationDSD, "startDate=${startDate},endDate=${endDate}"));
		
	}
	
	protected PatientDataSetDefinition adultsNewlyInitiatedOnArtDataSetDefinition(String datasetName) {
		
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
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		LastHtsInitialResultDataDefinition test1DataDefinition = new LastHtsInitialResultDataDefinition();
		test1DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test1DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("test 1 result", test1DataDefinition, indParams, null);
		
		LastHtsRetestResultDataDefinition test2DataDefinition = new LastHtsRetestResultDataDefinition();
		test2DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test2DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("test 2 result", test2DataDefinition, indParams, null);
		
		CohortDefinition cd = new S0201CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Newly initiated on ART");
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	protected PatientDataSetDefinition missedAppointmentDatasetDefinition(String datasetName) {
		
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
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsTracingAttemptsDataDefinition tracingAttemptsDataDefinition = new SimsTracingAttemptsDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Tracing attempts", tracingAttemptsDataDefinition, indParams, null);
		
		SimsTracingOutcomeDataDefinition tracingOutcomeDataDefinition = new SimsTracingOutcomeDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Tracing outcome", tracingOutcomeDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0202CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Missed appointments");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition sameDayARTInitiationDatasetDefinition(String datasetName) {
		
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
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHivDiagnosisDateDataDefinition hivDiagnosisDateDataDefinition = new SimsHivDiagnosisDateDataDefinition();
		hivDiagnosisDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		hivDiagnosisDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("HIV Diagnosis Date", hivDiagnosisDateDataDefinition, indParams, null);
		
		SimsHivEnrollmentDateDataDefinition hivEnrollmentDateDataDefinition = new SimsHivEnrollmentDateDataDefinition();
		hivEnrollmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		hivEnrollmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("ART Initiation Date", hivEnrollmentDateDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0203CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Same Day ART Initiation");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
}
