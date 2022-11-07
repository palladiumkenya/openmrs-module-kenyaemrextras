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
import org.openmrs.module.kenyaemr.calculation.library.hiv.DateConfirmedHivPositiveCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.PatientProgramEnrollmentCalculation;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.PatientProgramEnrollmentConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.ActivePatientsPopulationTypeDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.MFLCodeDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.AgeAtReportingDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLArtStartDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.*;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.*;
import org.openmrs.module.kenyaemrextras.reporting.library.continuityOfTreatment.AppointmentAttritionIndicatorLibrary;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731Greencard.ETLMoh731GreenCardIndicatorLibrary;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemrextras.common.report.appointmentAndAttrition" })
public class AppointmentAttritionReportBuilder extends AbstractReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Autowired
	private AppointmentAttritionIndicatorLibrary cot;
	
	@Autowired
	private ETLMoh731GreenCardIndicatorLibrary moh731IndicatorLibrary;
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		return Arrays.asList(ReportUtils.map(appointmentsAttritionDataSetDefinitionColumns(),
		    "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(appointmentAndAttritionIndicators(),
		    "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(missedAppointmentsDataSetDefinitionColumns(),
		    "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		    missedAppointmentsUnder31DaysNotRTCDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedAppointmentsUnder7DaysRTCDataSetDefinitionColumns(),
		        "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		        missedAppointments8To30DaysRTCDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedAppointmentsOver30DaysDataSetDefinitionColumns(),
		        "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		        missedAppointmentsOver30DaysRTCDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(ltfuDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition appointmentAndAttritionIndicators() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.setName("Appointments-Attrition-Return-To-Care");
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		String indParams = "startDate=${startDate},endDate=${endDate}";
		cohortDsd.addColumn("Clients currently on ART", "",
		    ReportUtils.map(moh731IndicatorLibrary.currentlyOnArt(), indParams), "");
		cohortDsd.addColumn("Appointments scheduled within reporting period", "",
		    ReportUtils.map(cot.totalAppointments(), indParams), "");
		cohortDsd.addColumn("Missed appointments within reporting period", "",
		    ReportUtils.map(cot.missedAppointments(), indParams), "");
		cohortDsd.addColumn("Missed appointments under 31 days and not RTC", "",
		    ReportUtils.map(cot.missedAppointmentsNotRTCEndOfReportingPeriod(), indParams), "");
		cohortDsd.addColumn(
		    "Clients who were missed appointment and returned to care within 7 days since their interruption", "",
		    ReportUtils.map(cot.rtcWithin7Days(), indParams), "");
		cohortDsd.addColumn(
		    "Clients who were missed appointment and returned to care after 7 to 30 days since their interruption", "",
		    ReportUtils.map(cot.rtcBetween8And30Days(), indParams), "");
		cohortDsd.addColumn("Clients who have missed an appointment for more than 30 days", "",
		    ReportUtils.map(cot.iitOver30Days(), indParams), "");
		cohortDsd.addColumn(
		    "Clients who were missed appointment and returned to care after 30 days since their interruption", "",
		    ReportUtils.map(cot.rtcOver30Days(), indParams), "");
		cohortDsd
		        .addColumn(
		            "Clients who have missed appointment for more than 30 days and have not yet returned to care as of reporting date",
		            "", ReportUtils.map(cot.currentIIT(), indParams), "");
		
		return cohortDsd;
	}
	
	protected DataSetDefinition appointmentsAttritionDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("appointmentAndAttrition");
		dsd.setDescription("Appointment attrition information");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		AppointmentOutcomeDataDefinition appointmentOutcomeDataDefinition = new AppointmentOutcomeDataDefinition();
		appointmentOutcomeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		appointmentOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name",
		        new org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Appointment Date", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Appointment Status", appointmentOutcomeDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		AppointmentsAndAttritionCohortDefinition cd = new AppointmentsAndAttritionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointmentsDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointments");
		dsd.setDescription("Missed Appointments");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsAndAttritionCohortDefinition cd = new MissedAppointmentsAndAttritionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointmentsUnder31DaysNotRTCDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointmentsUnder31DaysNotRTC");
		dsd.setDescription("Missed Appointments less than 31 days and not RTC");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsUnder31DaysNotRTCCohortDefinition cd = new MissedAppointmentsUnder31DaysNotRTCCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointmentsUnder7DaysRTCDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointmentsUnder7DaysRTC");
		dsd.setDescription("Missed Appointments and RTC withing 7 days");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsRTCWithin7DaysCohortDefinition cd = new MissedAppointmentsRTCWithin7DaysCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointments8To30DaysRTCDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointments8To30DaysRTC");
		dsd.setDescription("Missed Appointments and RTC after 8 to 30 days");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsRTC8To30DaysCohortDefinition cd = new MissedAppointmentsRTC8To30DaysCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointmentsOver30DaysDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointmentsOver30Days");
		dsd.setDescription("Missed appointments over 30 days");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsIITOver30DaysCohortDefinition cd = new MissedAppointmentsIITOver30DaysCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition missedAppointmentsOver30DaysRTCDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedAppointmentsOver30DaysRTC");
		dsd.setDescription("Missed appointments and RTC after 30 days");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		MissedAppointmentsRTCOver30DaysCohortDefinition cd = new MissedAppointmentsRTCOver30DaysCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition ltfuDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("ltfuClients");
		dsd.setDescription("Missed appointments over 30 days and have not returned");
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
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLDateBasedCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLResultDataDefinition lastVlResultDataDefinition = new ETLDateBasedLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVLDateDataDefinition lastVlDateDataDefinition = new ETLDateBasedLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLDateBasedLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		lastAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		LastDefaulterTracingDateBasedDateDataDefinition lastTracingDateDataDefinition = new LastDefaulterTracingDateBasedDateDataDefinition();
		lastTracingDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingTypeDateBasedDataDefinition lastTracingTypeDataDefinition = new TracingTypeDateBasedDataDefinition();
		lastTracingTypeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingNumberDateBasedDataDefinition tracingAttemptsDataDefinition = new TracingNumberDateBasedDataDefinition();
		tracingAttemptsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		TracingOutcomeDateBasedDataDefinition tracingOutcomeDataDefinition = new TracingOutcomeDateBasedDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ReturnToCareDateBasedDateDataDefinition returnToCareDateDataDefinition = new ReturnToCareDateBasedDateDataDefinition();
		returnToCareDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		returnToCareDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("MFL Code", new MFLCodeDataDefinition(), "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age at reporting", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Population Type", new ActivePatientsPopulationTypeDataDefinition(), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Date missed appointment", lastAppointmentDateDataDefinition, paramMapping, new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, paramMapping, new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, paramMapping);
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		LTFUClientsCohortDefinition cd = new LTFUClientsCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
}
