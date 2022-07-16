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
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
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
import org.openmrs.module.kenyaemr.reporting.cohort.definition.ANCRegisterCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.*;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.anc.*;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.AgeAtReportingDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLArtStartDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.library.pmtct.ANCIndicatorLibrary;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.AppointmentsAndAttritionCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DaysMissedAppointmentDateBasedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ETLDateBasedCurrentRegimenDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ETLDateBasedLastVLDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ETLDateBasedLastVLResultDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ETLDateBasedLastVisitDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ETLDateBasedNextAppointmentDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.LastDefaulterTracingDateBasedDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ReturnToCareDateBasedDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.TracingNumberDateBasedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.TracingOutcomeDateBasedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.TracingTypeDateBasedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.library.continuityOfTreatment.AppointmentAttritionIndicatorLibrary;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemrextras.common.report.appointmentAndAttrition" })
public class AppointmentAttritionReportBuilder extends AbstractReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Autowired
	private AppointmentAttritionIndicatorLibrary cot;
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		return Arrays.asList(ReportUtils.map(appointmentAndAttrition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(appointmentsAttritionDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition appointmentAndAttrition() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.setName("Appointments, Attrition and Return to care");
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		cohortDsd.addColumn("Appointments scheduled within reporting period", "",
		    ReportUtils.map(cot.totalAppointments(), indParams), "");
		cohortDsd.addColumn("Missed appointments within reporting period", "",
		    ReportUtils.map(cot.missedAppointments(), indParams), "");
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
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLDateBasedNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLDateBasedNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		DaysMissedAppointmentDateBasedDataDefinition daysMissedAppointmentDataDefinition = new DaysMissedAppointmentDateBasedDataDefinition();
		daysMissedAppointmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
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
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
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
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Appointment Date", lastAppointmentDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Last Tracing Date", lastTracingDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Tracing Type", lastTracingTypeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Tracing attempt No", tracingAttemptsDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Outcome", tracingOutcomeDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Return to Care Date", returnToCareDateDataDefinition, "endDate=${endDate}", new DateConverter(
		        DATE_FORMAT));
		dsd.addColumn("Number of days late", daysMissedAppointmentDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		
		CohortDefinition cd = new AppointmentsAndAttritionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
}
