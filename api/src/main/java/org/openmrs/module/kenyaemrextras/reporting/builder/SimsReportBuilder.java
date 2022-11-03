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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S02012CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0201CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0202CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0203CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0205CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0207CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0219CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0226To28CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.EverOnIPTDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.converter.SimsDataConverter;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.*;
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
		DataSetDefinition adultsOnArtVLMonitoringBDSD = adultsOnArtVLMonitoringDatasetDefinition("S_02_04");
		DataSetDefinition adultsOnArtVirallyUnsupressedDSD = adultsOnARTNonVirallySuppressedDatasetDefinition("S_02_05");
		DataSetDefinition adultsOnArtDSD = adultsOnARTDatasetDefinition("S_02_07");
		DataSetDefinition adultsOnArtWithPresumptiveTBDSD = adultsOnARTWithPresumptiveTBDatasetDefinition("S_02_12");
		DataSetDefinition pedsOnArtVLMonitoringBDSD = pedsOnArtVLMonitoringDatasetDefinition("S_02_22");
		DataSetDefinition pedsOnArtWithTBScreeningResultBDSD = pedsOnArtWithTBScreeningResultDatasetDefinition("S_02_26");
		DataSetDefinition pedsOnArtCTXDispensedBDSD = pedsOnArtCTXDispensedDatasetDefinition("S_02_28");
		DataSetDefinition pedsOnArtScreenedNegTBAndEverOnTPTBDSD = pedsOnArtScreenedNegTBAndEverOnTPTDatasetDefinition("S_02_27");
		DataSetDefinition pedsMissedRecentAppointmentBDSD = pedsMissedRecentAppointmentDatasetDefinition("S_02_19");
		
		return Arrays.asList(ReportUtils.map(newlyInitiatedOnArtPatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedAppointmentsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(sameDayInitiationDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(adultsOnArtVirallyUnsupressedDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(adultsOnArtDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(adultsOnArtWithPresumptiveTBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(pedsOnArtWithTBScreeningResultBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(pedsOnArtCTXDispensedBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(pedsOnArtScreenedNegTBAndEverOnTPTBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(adultsOnArtVLMonitoringBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(pedsOnArtVLMonitoringBDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(pedsMissedRecentAppointmentBDSD, "startDate=${startDate},endDate=${endDate}")
		
		);
		
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
		
		SimsRetestVerificationDataDefinition retestVerificationDataDefinition = new SimsRetestVerificationDataDefinition();
		retestVerificationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		retestVerificationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("S_02_01 Q3", retestVerificationDataDefinition, indParams, new SimsDataConverter());
		
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
		
		SimsTracingAttemptsDoneDataDefinition isTracingAttemptsDoneDataDefinition = new SimsTracingAttemptsDoneDataDefinition();
		isTracingAttemptsDoneDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		isTracingAttemptsDoneDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_02_02 Q2", isTracingAttemptsDoneDataDefinition, indParams, new SimsDataConverter());
		
		SimsTracingOutcomeDataDefinition tracingOutcomeDataDefinition = new SimsTracingOutcomeDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Tracing outcome", tracingOutcomeDataDefinition, indParams, null);
		
		SimsTracingOutcomeDocumentedDataDefinition tracingOutcomeDocumentedDataDefinition = new SimsTracingOutcomeDocumentedDataDefinition();
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_02_02 Q3", tracingOutcomeDocumentedDataDefinition, indParams, new SimsDataConverter());
		
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
		
		SimsSameDayARTInitiationDataDefinition simsSameDayARTInitiationDataDefinition = new SimsSameDayARTInitiationDataDefinition();
		simsSameDayARTInitiationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsSameDayARTInitiationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_02_03 Q2", simsSameDayARTInitiationDataDefinition, indParams, new SimsDataConverter());
		
		CohortDefinition cd = new S0203CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Same Day ART Initiation");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition adultsOnARTNonVirallySuppressedDatasetDefinition(String datasetName) {
		
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
		
		SimsEnhancedAdherenceDateDataDefinition enhancedAdherenceDateDataDefinition = new SimsEnhancedAdherenceDateDataDefinition();
		enhancedAdherenceDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		enhancedAdherenceDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Enhance Adherence Date", enhancedAdherenceDateDataDefinition, indParams, null);
		
		SimsEnhancedAdherenceDocumentedDataDefinition enhancedAdherenceDocumentedDataDefinition = new SimsEnhancedAdherenceDocumentedDataDefinition();
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Enhance Adherence Documented", enhancedAdherenceDocumentedDataDefinition, indParams, null);
		
		SimsFollowUpVLTakenDataDefinition followUpVLTakenDataDefinition = new SimsFollowUpVLTakenDataDefinition();
		followUpVLTakenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		followUpVLTakenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Follow Up VL", followUpVLTakenDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0205CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Non virally suppressed");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition adultsOnARTDatasetDefinition(String datasetName) {
		
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
		
		SimsTBScreeningResultDataDefinition tbScreeningResultDataDefinition = new SimsTBScreeningResultDataDefinition();
		tbScreeningResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tbScreeningResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("TB Screening Result", tbScreeningResultDataDefinition, indParams, null);
		
		EverOnIPTDataDefinition everOnIPTDataDefinition = new EverOnIPTDataDefinition();
		everOnIPTDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		everOnIPTDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Ever on TPT/IPT", everOnIPTDataDefinition, indParams, null);
		
		SimsTBResultDoumentedDataDefinition simsTBResultDoumentedDataDefinition = new SimsTBResultDoumentedDataDefinition();
		simsTBResultDoumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTBResultDoumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("TB Result Documented on lastVisit", simsTBResultDoumentedDataDefinition, indParams, null);
		
		SimsElicitedContactsDataDefinition simsElicitedContactsDataDefinition = new SimsElicitedContactsDataDefinition();
		simsElicitedContactsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsElicitedContactsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Contacts Elicited", simsElicitedContactsDataDefinition, indParams, null);
		
		SimsChildListedAsContactDataDefinition simsChildListedAsContactDataDefinition = new SimsChildListedAsContactDataDefinition();
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Child Listed as Contact", simsChildListedAsContactDataDefinition, indParams, null);
		
		SimsCTXDispensedDataDefinition simsCTXDispensedDataDefinition = new SimsCTXDispensedDataDefinition();
		simsCTXDispensedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsCTXDispensedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("CTX Dispensed", simsCTXDispensedDataDefinition, indParams, null);
		
		SimsNegativeTBResultsAndEverOnTPTDataDefinition negativeTBResultsAndEverOnTPTDataDefinition = new SimsNegativeTBResultsAndEverOnTPTDataDefinition();
		negativeTBResultsAndEverOnTPTDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		negativeTBResultsAndEverOnTPTDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Negative TB Result Ever on TPT", negativeTBResultsAndEverOnTPTDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0207CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Adults on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition adultsOnARTWithPresumptiveTBDatasetDefinition(String datasetName) {
		
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
		
		SimsSmearCultureXpertResultsDataDefinition simsSmearCultureXpertResultsDataDefinition = new SimsSmearCultureXpertResultsDataDefinition();
		simsSmearCultureXpertResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsSmearCultureXpertResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Smear/Culture/GeneXpert Resutls", simsSmearCultureXpertResultsDataDefinition, indParams, null);
		
		SimsTBMolecularTestingDataDefinition simsTBMolecularTestingDataDefinition = new SimsTBMolecularTestingDataDefinition();
		simsTBMolecularTestingDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTBMolecularTestingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("TB Molecular Testing", simsTBMolecularTestingDataDefinition, indParams, null);
		
		CohortDefinition cd = new S02012CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("On ART with Presumptive TB");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnArtWithTBScreeningResultDatasetDefinition(String datasetName) {
		
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
		
		SimsTBResultDoumentedDataDefinition simsTBResultDoumentedDataDefinition = new SimsTBResultDoumentedDataDefinition();
		simsTBResultDoumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTBResultDoumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("TB Result Documented on lastVisit", simsTBResultDoumentedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0226To28CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Current on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnArtCTXDispensedDatasetDefinition(String datasetName) {
		
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
		
		SimsCTXDispensedDataDefinition simsCTXDispensedDataDefinition = new SimsCTXDispensedDataDefinition();
		simsCTXDispensedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsCTXDispensedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("CTX Dispensed", simsCTXDispensedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0226To28CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Current on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnArtScreenedNegTBAndEverOnTPTDatasetDefinition(String datasetName) {
		
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
		
		SimsNegativeTBResultsAndEverOnTPTDataDefinition negativeTBResultsAndEverOnTPTDataDefinition = new SimsNegativeTBResultsAndEverOnTPTDataDefinition();
		negativeTBResultsAndEverOnTPTDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		negativeTBResultsAndEverOnTPTDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Negative TB Result And Ever on TPT", negativeTBResultsAndEverOnTPTDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0226To28CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Current on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnArtVLMonitoringDatasetDefinition(String datasetName) {
		
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
		
		SimsPedsRecentVLResultsDataDefinition pedsRecentVLResultsDataDefinition = new SimsPedsRecentVLResultsDataDefinition();
		pedsRecentVLResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		pedsRecentVLResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds Recent VL Results", pedsRecentVLResultsDataDefinition, indParams, null);
		
		SimsPedsRecentVLTestOrderedDataDefinition pedsRecentVLTestOrderedDataDefinition = new SimsPedsRecentVLTestOrderedDataDefinition();
		pedsRecentVLTestOrderedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		pedsRecentVLTestOrderedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Ped Test Ordered", pedsRecentVLTestOrderedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0226To28CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Current on ART VL Monitoring");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition adultsOnArtVLMonitoringDatasetDefinition(String datasetName) {
		
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
		
		SimsRecentVLResultsDataDefinition recentVLResultsDataDefinition = new SimsRecentVLResultsDataDefinition();
		recentVLResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentVLResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Recent VL Results", recentVLResultsDataDefinition, indParams, null);
		
		SimsRecentVLTestOrderedDataDefinition recentVLTestOrderedDataDefinition = new SimsRecentVLTestOrderedDataDefinition();
		recentVLTestOrderedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentVLTestOrderedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Test Ordered", recentVLTestOrderedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0207CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Adults On ART VL Monitoring");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsMissedRecentAppointmentDatasetDefinition(String datasetName) {
		
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
		
		SimsTracingAttemptsDoneDataDefinition isTracingAttemptsDoneDataDefinition = new SimsTracingAttemptsDoneDataDefinition();
		isTracingAttemptsDoneDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		isTracingAttemptsDoneDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds Tracing Attempts Made", isTracingAttemptsDoneDataDefinition, indParams, new SimsDataConverter());
		
		SimsTracingOutcomeDocumentedDataDefinition tracingOutcomeDocumentedDataDefinition = new SimsTracingOutcomeDocumentedDataDefinition();
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds Tracing Outcome", tracingOutcomeDocumentedDataDefinition, indParams, new SimsDataConverter());
		
		CohortDefinition cd = new S0219CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Missed Most Recent Appointment");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
}
