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

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.calculation.library.NumberOfDaysLateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.DateConfirmedHivPositiveCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.PatientProgramEnrollmentCalculation;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.PatientProgramEnrollmentConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.ActivePatientsPopulationTypeDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.AgeAtReportingDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLArtStartDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLCurrentRegimenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVLDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVLResultDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVisitDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLNextAppointmentDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.defaulterTracing.FinalOutcomeDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.defaulterTracing.HonouredAppointmentDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.defaulterTracing.TracingNumberDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.defaulterTracing.TracingTypeDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.AppointmentsAndAttritionCohortDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
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
@Builds({ "kenyaemr.extras.common.report.appointmentsAndAttrition" })
public class AppointmentsAndAttritionReportBuilder extends AbstractHybridReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected void addColumns(HybridReportDescriptor report, PatientDataSetDefinition dsd) {
	}
	
	@Override
	protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor descriptor, PatientDataSetDefinition dsd) {
		return null;
	}
	
	protected Mapped<CohortDefinition> allPatientsCohort() {
		CohortDefinition cd = new AppointmentsAndAttritionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Patients appointments");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition allVisits = patientsAppointmentsDataSetDefinition("patientsAppointments");
		allVisits.addRowFilter(allPatientsCohort());
		DataSetDefinition allPatientsDSD = allVisits;
		
		return Arrays.asList(ReportUtils.map(allPatientsDSD, "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected PatientDataSetDefinition patientsAppointmentsDataSetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String defParam = "startDate=${startDate},endDate=${endDate}";
		
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
		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLLastVLResultDataDefinition lastVlResultDataDefinition = new ETLLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLLastVLDateDataDefinition lastVlDateDataDefinition = new ETLLastVLDateDataDefinition();
		lastVlDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		ETLNextAppointmentDateDataDefinition lastAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		lastAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
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
		dsd.addColumn("Last VL Result",  lastVlResultDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Last VL Date", lastVlDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Last Appointment Date", lastAppointmentDateDataDefinition, "endDate=${endDate}",
		    new DateConverter(DATE_FORMAT));
		//dsd.addColumn("Last Tracing Date", new EncounterDatetimeDataDefinition(), "", new DateConverter(DATE_FORMAT));
		//dsd.addColumn("Tracing Type", new TracingTypeDataDefinition(), "");
		//dsd.addColumn("Tracing attempt No", new TracingNumberDataDefinition(), "");
		//.addColumn("Outcome", new FinalOutcomeDataDefinition(), "");
		//dsd.addColumn("Return to Care Date", new HonouredAppointmentDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("Number of days late", new CalculationDataDefinition("Number of days late",
		        new NumberOfDaysLateCalculation()), "", new DataConverter[] { new CalculationResultConverter() });
		dsd.addColumn("Program", new CalculationDataDefinition("Program", new PatientProgramEnrollmentCalculation()), "",
		    new PatientProgramEnrollmentConverter());
		return dsd;
	}
}
