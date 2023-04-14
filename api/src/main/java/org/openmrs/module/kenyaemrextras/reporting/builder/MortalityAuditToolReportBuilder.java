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

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.KenyaEMRMaritalStatusDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLArtStartDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLCurrentRegimenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLFirstRegimenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.WHOStageArtDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIEnrollmentDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIIdDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.pama.PamaCareGiverStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHEICohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivAndTBPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedTBPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.*;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.*;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Mortality Audit Tool
 */

@Component
@Builds({ "kenyaemrextras.common.report.mortalityaudittool" })
public class MortalityAuditToolReportBuilder extends AbstractReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		return Arrays.asList(ReportUtils.map(heiDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(hivDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(hivAndTBDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(tbDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition hivDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedHIVPatients");
		dsd.setDescription("Deceased HIV Patients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		PatientIdentifierType nupi = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition nupiDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nupi.getName(), nupi), identifierFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("DOB", new DOBDataDefinition(), "");
		dsd.addColumn("Death date",
		    new org.openmrs.module.kenyaemr.reporting.data.converter.definition.DateOfDeathDataDefinition(), "");
		dsd.addColumn("Age at Death", new AgeAtDeathPatientDataDefinition(), "", null);
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		
		dsd.addColumn("Marital Status", new KenyaEMRMaritalStatusDataDefinition(), "");
		dsd.addColumn("Pregnant or Breastfeeding", new PregnantOrBreastfeedingDataDefinition(), "");
		dsd.addColumn("Occupation",
		    new ObsForPersonDataDefinition("Occupation", TimeQualifier.LAST, Dictionary.getConcept(Dictionary.OCCUPATION),
		            null, null), "", new ObsValueConverter());
		dsd.addColumn("Primary caregiver", new HeiPrimaryCareGiverDataDefinition(), "");
		dsd.addColumn("HIV Status of caregiver", new PamaCareGiverStatusDataDefinition(), "");
		dsd.addColumn("Caregiver's Education level", new HeiCareGiverEducationDataDefinition(), "");
		dsd.addColumn("Caregiver's occupation", new HeiCareGiverOccupationDataDefinition(), "");
		dsd.addColumn("Date of HIV diagnosis", new DateOfHIVDiagnosisDataDefinition(), "");
		dsd.addColumn("Date of enrollment into care", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("WHO clinical stage at enrollment", new BaselineWHOStageDataDefinition(), "");
		dsd.addColumn("Baseline WHO staging date", new BaselineWHOStageDateDataDefinition(), "");
		dsd.addColumn("WHO Clinical stage at time of death", new WHOStageArtDataDefinition(), "");
		dsd.addColumn("Date of ART initiation", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Duration on ART", new DurationOnARTDataDefinition(), "");
		dsd.addColumn("Initial regimen", new ETLFirstRegimenDataDefinition(), "");
		dsd.addColumn("Date of start regimen", new DateOfFirstARTRegimenDataDefinition(), "");
		dsd.addColumn("Reasons for change of first regimen", new FirstRegimenChangeReasonDataDefinition(), "");
		dsd.addColumn("2nd Regimen", new SecondRegimenDataDefinition(), "");
		dsd.addColumn("Date of 2nd regimen switch", new SecondARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 2nd regimen", new SecondRegimenChangeReasonDataDefinition(), "");//2,1
		dsd.addColumn("3rd Regimen", new ThirdRegimenDataDefinition(), "");
		dsd.addColumn("Date of switch of 3rd Regimen", new ThirdARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 3rd Regimen", new ThirdARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("4th Regimen", new FourthRegimenDataDefinition(), "");
		dsd.addColumn("Date of switch of 4th Regimen", new FourthARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 4th Regimen", new FourthRegimenChangeReasonDataDefinition(), "");
		dsd.addColumn("Regimen at the time of death", new ETLCurrentRegimenDataDefinition(), "");
		dsd.addColumn("Baseline CD4 count done", new BaselineCD4DoneDataDefinition(), "");
		dsd.addColumn("Baseline CD4", new BaselineCD4CountDataDefinition(), "");
		dsd.addColumn("Date of Baseline CD4 test", new BaselineCD4DateDataDefinition(), "");
		dsd.addColumn("CTX/Dapsone given", new CTXDapsoneDispensedDataDefinition(), "");
		dsd.addColumn("CRAG test done for adolescents and adults with < 200 cd4", new CrAgTestDoneDataDefinition(), "");
		dsd.addColumn("CRAG test results", new CrAgTestResultDataDefinition(), "");
		dsd.addColumn("Is there a more recent CD4 count", new CD4RecencyDataDefinition(), "");
		dsd.addColumn("Date of most recent CD4 count", new RecentCD4DateDataDefinition(), "");
		ValidVLDataDefinition validVLDataDefinition = new ValidVLDataDefinition();
		validVLDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL test done", validVLDataDefinition, paramMapping, null);
		
		ValidVLDateDataDefinition validVLDateDataDefinition = new ValidVLDateDataDefinition();
		validVLDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL result date", validVLDateDataDefinition, paramMapping, null);
		
		ValidVLResultDataDefinition validVLResultDataDefinition = new ValidVLResultDataDefinition();
		validVLResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL result", validVLResultDataDefinition, paramMapping, null);
		
		RecentInvalidVLDataDefinition recentInvalidVLDataDefinition = new RecentInvalidVLDataDefinition();
		recentInvalidVLDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentInvalidVLDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of most recent VL test if no VL done in the last 1 year", recentInvalidVLDataDefinition,
		    paramMapping, null);
		
		RecentInvalidVLResultDataDefinition recentInvalidVLResultDataDefinition = new RecentInvalidVLResultDataDefinition();
		recentInvalidVLResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentInvalidVLResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Result of most recent VL test if no VL done in the last 1 year", recentInvalidVLResultDataDefinition,
		    paramMapping, null);
		dsd.addColumn("VL result for care giver", new VLResultForCaregiverDataDefinition(), "");
		dsd.addColumn("VL result date for care giver", new VLDateForCaregiverDataDefinition(), "");
		dsd.addColumn("Ever Initiated on TPT", new EverOnIPTDataDefinition(), "");
		dsd.addColumn("TPT Initiation date", new IPTInitiationDateDataDefinition(), "");
		dsd.addColumn("TPT Outcome", new IPTOutcomeHIVPatientsDataDefinition(), "");
		dsd.addColumn("TPT Completion date", new IPTCompletionDateDataDefinition(), "");
		dsd.addColumn("Ever diagnosed with presumptive TB in the last 12 months prior to death",
		    new PresumtiveTBDataDefinition(), "");
		dsd.addColumn("TB investigations done following Presumptive TB",
		    new TBInvestigationsDoneAfterPresumedTBDataDefinition(), "");
		dsd.addColumn("Adhered to clinic appointments for HIV medication", new AdheredToClinicAppointmentsDataDefinition(),
		    "");
		dsd.addColumn("Clinic appointments synchronized with caregiver's",
		    new ClinicAppointmentsSyncWithCareGiversDataDefinition(), "");
		dsd.addColumn("Honoured last clinic appointment", new HonouredLastAppointmentDataDefinition(), "");
		dsd.addColumn("Morisky Medication Adherence (MMAS-4)", new MoriskyMedicationAdherenceDataDefinition(), "");
		TBScreeningDoneDataDefinition tbScreeningDoneDataDefinition = new TBScreeningDoneDataDefinition();
		tbScreeningDoneDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tbScreeningDoneDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Screened for TB in the last visit", tbScreeningDoneDataDefinition, paramMapping, null);
		dsd.addColumn("TB screening results", new TBScreeningResultsDataDefinition(), "");
		dsd.addColumn("TB investigations done", new TBInvestigationsDoneDataDefinition(), "");
		
		DeceasedHivPatientCohortDefinition cd = new DeceasedHivPatientCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition hivAndTBDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedHIVAndTBPatients");
		dsd.setDescription("Deceased HIV Patients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		PatientIdentifierType nupi = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition nupiDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nupi.getName(), nupi), identifierFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("DOB", new DOBDataDefinition(), "");
		dsd.addColumn("Death date",
		    new org.openmrs.module.kenyaemr.reporting.data.converter.definition.DateOfDeathDataDefinition(), "");
		dsd.addColumn("Age at Death", new AgeAtDeathPatientDataDefinition(), "", null);
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		
		dsd.addColumn("Marital Status", new KenyaEMRMaritalStatusDataDefinition(), "");
		dsd.addColumn("Pregnant or Breastfeeding", new PregnantOrBreastfeedingDataDefinition(), "");
		dsd.addColumn("Occupation",
		    new ObsForPersonDataDefinition("Occupation", TimeQualifier.LAST, Dictionary.getConcept(Dictionary.OCCUPATION),
		            null, null), "", new ObsValueConverter());
		dsd.addColumn("Primary caregiver", new HeiPrimaryCareGiverDataDefinition(), "");
		dsd.addColumn("HIV Status of caregiver", new PamaCareGiverStatusDataDefinition(), "");
		dsd.addColumn("Caregiver's Education level", new HeiCareGiverEducationDataDefinition(), "");
		dsd.addColumn("Caregiver's occupation", new HeiCareGiverOccupationDataDefinition(), "");
		dsd.addColumn("Date of HIV diagnosis", new DateOfHIVDiagnosisDataDefinition(), "");
		dsd.addColumn("Date of enrollment into care", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("WHO clinical stage at enrollment", new BaselineWHOStageDataDefinition(), "");
		dsd.addColumn("Baseline WHO staging date", new BaselineWHOStageDateDataDefinition(), "");
		dsd.addColumn("WHO Clinical stage at time of death", new WHOStageArtDataDefinition(), "");
		dsd.addColumn("Date of ART initiation", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Duration on ART", new DurationOnARTDataDefinition(), "");
		dsd.addColumn("Initial regimen", new ETLFirstRegimenDataDefinition(), "");
		dsd.addColumn("Date of start regimen", new DateOfFirstARTRegimenDataDefinition(), "");
		dsd.addColumn("Reasons for change of first regimen", new FirstRegimenChangeReasonDataDefinition(), "");
		dsd.addColumn("2nd Regimen", new SecondRegimenDataDefinition(), "");
		dsd.addColumn("Date of 2nd regimen switch", new SecondARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 2nd regimen", new SecondRegimenChangeReasonDataDefinition(), "");//2,1
		dsd.addColumn("3rd Regimen", new ThirdRegimenDataDefinition(), "");
		dsd.addColumn("Date of switch of 3rd Regimen", new ThirdARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 3rd Regimen", new ThirdARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("4th Regimen", new FourthRegimenDataDefinition(), "");
		dsd.addColumn("Date of switch of 4th Regimen", new FourthARTRegimenSwitchDateDataDefinition(), "");
		dsd.addColumn("Reasons for change of 4th Regimen", new FourthRegimenChangeReasonDataDefinition(), "");
		dsd.addColumn("Regimen at the time of death", new ETLCurrentRegimenDataDefinition(), "");
		dsd.addColumn("Baseline CD4 count done", new BaselineCD4DoneDataDefinition(), "");
		dsd.addColumn("Baseline CD4", new BaselineCD4CountDataDefinition(), "");
		dsd.addColumn("Date of Baseline CD4 test", new BaselineCD4DateDataDefinition(), "");
		dsd.addColumn("CTX/Dapsone given", new CTXDapsoneDispensedDataDefinition(), "");
		dsd.addColumn("CRAG test done for adolescents and adults with < 200 cd4", new CrAgTestDoneDataDefinition(), "");
		dsd.addColumn("CRAG test results", new CrAgTestResultDataDefinition(), "");
		dsd.addColumn("Is there a more recent CD4 count", new CD4RecencyDataDefinition(), "");
		dsd.addColumn("Date of most recent CD4 count", new RecentCD4DateDataDefinition(), "");
		ValidVLDataDefinition validVLDataDefinition = new ValidVLDataDefinition();
		validVLDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL test done", validVLDataDefinition, paramMapping, null);
		
		ValidVLDateDataDefinition validVLDateDataDefinition = new ValidVLDateDataDefinition();
		validVLDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL result date", validVLDateDataDefinition, paramMapping, null);
		
		ValidVLResultDataDefinition validVLResultDataDefinition = new ValidVLResultDataDefinition();
		validVLResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		validVLResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Valid VL result", validVLResultDataDefinition, paramMapping, null);
		
		RecentInvalidVLDataDefinition recentInvalidVLDataDefinition = new RecentInvalidVLDataDefinition();
		recentInvalidVLDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentInvalidVLDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of most recent VL test if no VL done in the last 1 year", recentInvalidVLDataDefinition,
		    paramMapping, null);
		
		RecentInvalidVLResultDataDefinition recentInvalidVLResultDataDefinition = new RecentInvalidVLResultDataDefinition();
		recentInvalidVLResultDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		recentInvalidVLResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Result of most recent VL test if no VL done in the last 1 year", recentInvalidVLResultDataDefinition,
		    paramMapping, null);
		dsd.addColumn("VL result for care giver", new VLResultForCaregiverDataDefinition(), "");
		dsd.addColumn("VL result date for care giver", new VLDateForCaregiverDataDefinition(), "");
		dsd.addColumn("Ever Initiated on TPT", new EverOnIPTDataDefinition(), "");
		dsd.addColumn("TPT Initiation date", new IPTInitiationDateDataDefinition(), "");
		dsd.addColumn("TPT Outcome", new IPTOutcomeHIVPatientsDataDefinition(), "");
		dsd.addColumn("TPT Completion date", new IPTCompletionDateDataDefinition(), "");
		dsd.addColumn("Ever diagnosed with presumptive TB in the last 12 months prior to death",
		    new PresumtiveTBDataDefinition(), "");
		dsd.addColumn("TB investigations done following Presumptive TB",
		    new TBInvestigationsDoneAfterPresumedTBDataDefinition(), "");
		dsd.addColumn("Diagnosed with TB in the last 12 months prior to death",
		    new DiagnosedTBWithin12MonthsToDeathDataDefinition(), "");
		dsd.addColumn("Type of TB Diagnosed in the last 12 months prior to death", new TbTypeDataDefinition(), "");
		dsd.addColumn("Adhered to clinic appointments for HIV medication", new AdheredToClinicAppointmentsDataDefinition(),
		    "");
		dsd.addColumn("Clinic appointments synchronized with caregiver's",
		    new ClinicAppointmentsSyncWithCareGiversDataDefinition(), "");
		dsd.addColumn("Honoured last clinic appointment", new HonouredLastAppointmentDataDefinition(), "");
		dsd.addColumn("Morisky Medication Adherence (MMAS-4)", new MoriskyMedicationAdherenceDataDefinition(), "");
		dsd.addColumn("Screened for TB in the last visit", new TBScreeningDoneDataDefinition(), "");
		dsd.addColumn("TB screening results", new TBScreeningResultsDataDefinition(), "");
		dsd.addColumn("TB investigations done", new TBInvestigationsDoneDataDefinition(), "");
		dsd.addColumn("Source of patient", new TbPatientSourceDataDefinition(), "");
		dsd.addColumn("Primary method of TB diagnosis", new TbMethodOfDiagnosisDataDefinition(), "");
		dsd.addColumn("Date of TB diagnosis", new TbDateOfDiagnosisDataDefinition(), "");
		dsd.addColumn("Type of TB", new TbTypeDataDefinition(), "");
		TBPatientTypeDataDefinition tbPatientTypeDataDefinition = new TBPatientTypeDataDefinition();
		tbPatientTypeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tbPatientTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("TB patient Type", tbPatientTypeDataDefinition, paramMapping, null);
		dsd.addColumn("Initiated on anti-TB", new TbInitiatedOnDrugsDataDefinition(), "");
		dsd.addColumn("Date Initiated on anti-TB", new TbDateInitiatedOnDrugsDataDefinition(), "");
		dsd.addColumn("Diagnosis to anti-TB drug initiation duration", new TbDiagnosisToInitiationDurationDataDefinition(),
		    "");
		dsd.addColumn("Patient anti-TB Initiation regimen", new TbPatientInitiationRegimenDataDefinition(), "");
		dsd.addColumn("Patient anti-TB Final regimen", new TbPatientFinalRegimenDataDefinition(), "");
		dsd.addColumn("TB treatment outcome-status at death", new TbTreatmentOutcomeAtDeathDataDefinition(), "");
		dsd.addColumn("HIV test done", new TbPatientHivTestDoneDataDefinition(), "");
		dsd.addColumn("HIV test date", new TbPatientHivTestDateDataDefinition(), "");
		dsd.addColumn("HIV test results", new TbPatientHivTestResultsDataDefinition(), "");
		dsd.addColumn("Client enrolled in HIV care", new TbPatientEnrolledInHivCareDataDefinition(), "");
		
		DeceasedHivAndTBPatientCohortDefinition cd = new DeceasedHivAndTBPatientCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition tbDatasetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedTBPatients");
		dsd.setDescription("Deceased TB Patients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		//Add columns here
		PatientIdentifierType nupi = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition nupiDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nupi.getName(), nupi), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Date of Death", new MortalityDateDataDefinition(), "");
		dsd.addColumn("Age at Death", new AgeAtDeathPatientDataDefinition(), "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "");
		dsd.addColumn("Marital Status", new KenyaEMRMaritalStatusDataDefinition(), "");
		dsd.addColumn("Pregnant or Breastfeeding", new PregnantOrBreastfeedingDataDefinition(), "");
		dsd.addColumn("Occupation",
		    new ObsForPersonDataDefinition("Occupation", TimeQualifier.LAST, Dictionary.getConcept(Dictionary.OCCUPATION),
		            null, null), "", new ObsValueConverter());
		dsd.addColumn("Primary Caregiver", new HeiPrimaryCareGiverDataDefinition(), "");
		dsd.addColumn("HIV status of care giver", new PamaCareGiverStatusDataDefinition(), "");
		dsd.addColumn("Education level of care giver", new HeiCareGiverEducationDataDefinition(), "");
		dsd.addColumn("Occupation of care giver", new HeiCareGiverOccupationDataDefinition(), "");
		
		dsd.addColumn("Source of patient", new TbPatientSourceDataDefinition(), "");
		dsd.addColumn("Primary method of TB diagnosis", new TbMethodOfDiagnosisDataDefinition(), "");
		dsd.addColumn("Date of TB diagnosis", new TbDateOfDiagnosisDataDefinition(), "");
		dsd.addColumn("Type of TB", new TbTypeDataDefinition(), "");
		//dsd.addColumn("Confirmed Drug Resistance", new TbTypeDataDefinition(), "");
		dsd.addColumn("Initiated on anti-TB", new TbInitiatedOnDrugsDataDefinition(), "");
		dsd.addColumn("Date Initiated on anti-TB", new TbDateInitiatedOnDrugsDataDefinition(), "");
		dsd.addColumn("Diagnosis to anti-TB drug initiation duration", new TbDiagnosisToInitiationDurationDataDefinition(),
		    "");
		dsd.addColumn("Patient anti-TB Initiation regimen", new TbPatientInitiationRegimenDataDefinition(), "");
		dsd.addColumn("Patient anti-TB Final regimen", new TbPatientFinalRegimenDataDefinition(), "");
		dsd.addColumn("TB treatment outcome-status at death", new TbTreatmentOutcomeAtDeathDataDefinition(), "");
		dsd.addColumn("HIV test done", new TbPatientHivTestDoneDataDefinition(), "");
		dsd.addColumn("HIV test date", new TbPatientHivTestDateDataDefinition(), "");
		dsd.addColumn("HIV test results", new TbPatientHivTestResultsDataDefinition(), "");
		dsd.addColumn("Client enrolled in HIV care", new TbPatientEnrolledInHivCareDataDefinition(), "");
		
		DeceasedTBPatientCohortDefinition cd = new DeceasedTBPatientCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition heiDatasetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedHEIs");
		dsd.setDescription("Deceased HEIs");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		//Add columns here
		PatientIdentifierType nupi = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition nupiDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nupi.getName(), nupi), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("HEI Number", new HEIIdDataDefinition(), "");
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Date of Death", new MortalityDateDataDefinition(), "");
		dsd.addColumn("Age at Death", new AgeAtDeathPatientDataDefinition(), "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "");
		dsd.addColumn("Marital Status", new HeiMaritalStatusDataDefinition(), "");
		dsd.addColumn("Primary Caregiver", new HeiPrimaryCareGiverDataDefinition(), "");
		dsd.addColumn("HIV status of care giver", new PamaCareGiverStatusDataDefinition(), "");
		dsd.addColumn("Education level of care giver", new HeiCareGiverEducationDataDefinition(), "");
		dsd.addColumn("Occupation of care giver", new HeiCareGiverOccupationDataDefinition(), "");
		dsd.addColumn("Entry point of care giver", new HeiMotherEntryPointDataDefinition(), "");
		dsd.addColumn("Gestation Age", new HeiMotherGestationAgeDataDefinition(), "");
		dsd.addColumn("Date Maternal HIV Diagnosed", new HeiMotherHivDiagnosisDateDataDefinition(), "");
		dsd.addColumn("WHO stage at PMTCT enrolment", new HeiMotherWhoStageAtPmtctStartDataDefinition(), "");
		dsd.addColumn("Maternal HAART Status", new HeiMotherHaartStatusDataDefinition(), "");
		dsd.addColumn("Date ART Initiated", new HeiMotherDateArtInitiatedDataDefinition(), "");
		dsd.addColumn("Mother ART Regimen", new HeiMotherArtRegimenDataDefinition(), "");
		dsd.addColumn("Nutritional Assessment Date", new HeiMotherLatestTriageDateDataDefinition(), "");
		dsd.addColumn("Weight", new HeiMotherWeightDataDefinition(), "");
		dsd.addColumn("Height", new HeiMotherHeightDataDefinition(), "");
		dsd.addColumn("Muac", new HeiMotherMuacDataDefinition(), "");
		
		dsd.addColumn("Recent VL Results", new HeiMotherRecentViralLoadDataDefinition(), "");
		dsd.addColumn("Recent VL Date", new HeiMotherRecentViralLoadDateDataDefinition(), "");
		dsd.addColumn("VL Results at PMTCT enrollment", new HeiMotherVLAtMCHEnrollmentDataDefinition(), "");
		dsd.addColumn("VL Results date at PMTCT enrollment", new HeiMotherVLDateAtMCHEnrollmentDataDefinition(), "");
		dsd.addColumn("HIV Disclosure Status", new HeiMotherPwpDisclosureDataDefinition(), "");
		dsd.addColumn("Adherence History", new HeiMotherAdherenceDataDefinition(), "");
		
		dsd.addColumn("HEI Enrollment Date", new HEIEnrollmentDateDataDefinition(), "");
		dsd.addColumn("DOB", new MortalityDateDataDefinition(), "");
		dsd.addColumn("Age at HEI enrollment", new MortalityDateDataDefinition(), "");
		dsd.addColumn("Place of delivery", new HeiPlaceOfDeliveryDataDefinition(), "");
		dsd.addColumn("Mode of Delivery", new HeiModeOfDeliveryDataDefinition(), "");
		dsd.addColumn("Infant Feeding", new HeiInfantFeedingDataDefinition(), "");
		dsd.addColumn("1st PCR Date", new HeiFirstPCRTestDateDataDefinition(), "");
		dsd.addColumn("1st PCR Result", new HeiFirstPCRTestResultDataDefinition(), "");
		dsd.addColumn("2nd PCR Date", new HeiSecondPCRTestDateDataDefinition(), "");
		dsd.addColumn("2nd PCR Result", new HeiSecondPCRTestResultDataDefinition(), "");
		dsd.addColumn("3rd PCR Date", new HeiThirdPCRTestDateDataDefinition(), "");
		dsd.addColumn("3rd PCR Result", new HeiThirdPCRTestResultDataDefinition(), "");
		dsd.addColumn("Final AB Test Date", new HeiFinalAntibodyTestDateDataDefinition(), "");
		dsd.addColumn("Final AB Test Result", new HeiFinalAntibodyTestResultDataDefinition(), "");
		dsd.addColumn("Infant Prophylaxis", new HeiInfantProphylaxisDataDefinition(), "");
		dsd.addColumn("Immunization Status", new HeiImmunizationStatusDataDefinition(), "");
		dsd.addColumn("Growth and Nutritional Assessment", new HeiNutritionalAssessmentDataDefinition(), "");
		dsd.addColumn("Developmental Assessment", new HeiDevelopmentalAssessmentDataDefinition(), "");
		
		DeceasedHEICohortDefinition cd = new DeceasedHEICohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
}
