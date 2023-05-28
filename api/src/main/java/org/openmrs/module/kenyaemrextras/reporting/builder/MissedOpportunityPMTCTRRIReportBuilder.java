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
import org.openmrs.module.kenyaemr.calculation.library.mchcs.PersonAddressCalculation;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.RDQACalculationResultConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.*;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLCurrentRegimenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIIdDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIMissedTestDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEINextAppointmentDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.maternity.*;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.rri.*;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.pmtctRRI.*;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
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
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.AgeAtReportingDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLArtStartDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLCurrentRegimenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVLDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVLResultDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.art.ETLLastVisitDateDataDefinition;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemrextras.mchms.report.missedopportunitypmtctrri" })
public class MissedOpportunityPMTCTRRIReportBuilder extends AbstractReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		return Arrays.asList(ReportUtils.map(missedHIVTestDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedHAARTDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedDTGOptimizationDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedHIVTestHEIDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(missedInfantProphylaxisDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(cALHIVWithNoValidVLDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(cALHIVLDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(txCurrPregnantAndBreastFeedingDataSetDefinition(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition missedHIVTestDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedHIVTestCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		DateOfLastMCHClinicVisitDataDefinition dateOfLastMCHClinicVisitDataDefinition = new DateOfLastMCHClinicVisitDataDefinition();
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of MCH clinic visit", dateOfLastMCHClinicVisitDataDefinition, paramMapping, null);
		
		NextMCHVisitAppointmentDateDataDefinition nextMCHVisitAppointmentDateDataDefinition = new NextMCHVisitAppointmentDateDataDefinition();
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextMCHVisitAppointmentDateDataDefinition, paramMapping, null);
		
		ServiceDeliveryPointDataDefinition serviceDeliveryPointDataDefinition = new ServiceDeliveryPointDataDefinition();
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Service delivery point", serviceDeliveryPointDataDefinition, paramMapping, null);
		
		MissedHIVTestCohortDefinition cd = new MissedHIVTestCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);
		
		return dsd;
	}
	
	protected DataSetDefinition missedHAARTDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("missedHAARTCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		DateOfLastMCHClinicVisitDataDefinition dateOfLastMCHClinicVisitDataDefinition = new DateOfLastMCHClinicVisitDataDefinition();
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of MCH clinic visit", dateOfLastMCHClinicVisitDataDefinition, paramMapping, null);
		
		MCHDateOfHIVDiagnosisDataDefinition mchDateOfHIVDiagnosisDataDefinition = new MCHDateOfHIVDiagnosisDataDefinition();
		mchDateOfHIVDiagnosisDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		mchDateOfHIVDiagnosisDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of HIV diagnosis", mchDateOfHIVDiagnosisDataDefinition, paramMapping, null);
		
		NextMCHVisitAppointmentDateDataDefinition nextMCHVisitAppointmentDateDataDefinition = new NextMCHVisitAppointmentDateDataDefinition();
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextMCHVisitAppointmentDateDataDefinition, paramMapping, null);
		
		ServiceDeliveryPointDataDefinition serviceDeliveryPointDataDefinition = new ServiceDeliveryPointDataDefinition();
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Service delivery point", serviceDeliveryPointDataDefinition, paramMapping, null);
		
		return dsd;
	}
	
	protected DataSetDefinition missedDTGOptimizationDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("missedDTGOptimizationCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		
		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		
		ETLFirstRegimenDataDefinition etlFirstRegimenDataDefinition = new ETLFirstRegimenDataDefinition();
		etlFirstRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		etlFirstRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Initial regimen", etlFirstRegimenDataDefinition, paramMapping, null);
		
		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);
		
		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);
		
		return dsd;
	}
	
	protected DataSetDefinition missedHIVTestHEIDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedHIVTestHEICohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("HEI Id", new HEIIdDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		DateOfLastHEIFollowupDataDefinition dateOfLastVisit = new DateOfLastHEIFollowupDataDefinition();
		dateOfLastVisit.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dateOfLastVisit.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of last visit", dateOfLastVisit, paramMapping, null);
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		
		dsd.addColumn("Missing HIV Test", new HEIMissedTestDataDefinition(), "");
		dsd.addColumn("Next Appointment Date", new HEINextAppointmentDateDataDefinition(), "");
		
		MissedHIVTestHEICohortDefinition cd = new MissedHIVTestHEICohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);
		
		return dsd;
	}
	
	protected DataSetDefinition missedInfantProphylaxisDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedInfantProphylaxisCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		DateOfLastMCHClinicVisitDataDefinition dateOfLastMCHClinicVisitDataDefinition = new DateOfLastMCHClinicVisitDataDefinition();
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dateOfLastMCHClinicVisitDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Date of MCH clinic visit", dateOfLastMCHClinicVisitDataDefinition, paramMapping, null);
		
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		
		NextMCHVisitAppointmentDateDataDefinition nextMCHVisitAppointmentDateDataDefinition = new NextMCHVisitAppointmentDateDataDefinition();
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextMCHVisitAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextMCHVisitAppointmentDateDataDefinition, paramMapping, null);
		
		ServiceDeliveryPointDataDefinition serviceDeliveryPointDataDefinition = new ServiceDeliveryPointDataDefinition();
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		serviceDeliveryPointDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Service delivery point", serviceDeliveryPointDataDefinition, paramMapping, null);
		
		MissedInfantProphylaxisCohortDefinition cd = new MissedInfantProphylaxisCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);
		
		return dsd;
	}
	
	protected DataSetDefinition missedVLTestCALHIVDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("missedVLTestCALHIVCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());
		
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");
		
		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));
		
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		
		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);
		
		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);
		
		MissedVLTestCAHLHIVCohortDefinition cd = new MissedVLTestCAHLHIVCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

	protected DataSetDefinition cALHIVWithNoValidVLDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("cALHIVWithNoValidVLCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);

		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");

		String paramMapping = "startDate=${startDate},endDate=${endDate}";

		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());

		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");

		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());

		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));

		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);

		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);

		CALHIVWithoutValidVlCohortDefinition cd = new CALHIVWithoutValidVlCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

	protected DataSetDefinition cALHIVLDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("cALHIVCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);

		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");

		String paramMapping = "startDate=${startDate},endDate=${endDate}";

		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());

		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");

		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());

		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));

		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);

		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);

		CALHIVCohortDefinition cd = new CALHIVCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

	protected DataSetDefinition txCurrPregnantAndBreastFeedingDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("txCurrPregnantAndBFCohort");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);

		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");

		String paramMapping = "startDate=${startDate},endDate=${endDate}";

		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Village_Estate_Landmark", new CalculationDataDefinition("Village/Estate/Landmark",
		        new PersonAddressCalculation()), "", new RDQACalculationResultConverter());

		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Next of kin", new NextOfKinDataDefinition(), "");
		dsd.addColumn("Next of kin phone", new NextOfKinPhoneDataDefinition(), "");

		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());

		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));

		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);

		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);

		TxCurrPregnantAndBfCohortDefinition cd = new TxCurrPregnantAndBfCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

	protected DataSetDefinition txcurrReproductiveWomen() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("txcurrReproductiveWomen");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);

		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");

		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Age", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);

		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);

		TxcurrReproductiveWomenCohortDefinition cd = new TxcurrReproductiveWomenCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

	protected DataSetDefinition txcurrReproductiveWomenNoChildrenContacts() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("txcurrReproductiveWomenNoChildrenContacts");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);

		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class,
		    CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");

		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		AgeAtReportingDataDefinition ageAtReportingDataDefinition = new AgeAtReportingDataDefinition();
		ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Age", ageAtReportingDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
		dsd.addColumn("Date confirmed positive", new CalculationDataDefinition("Date confirmed positive",
		        new DateConfirmedHivPositiveCalculation()), "", new DateArtStartDateConverter());
		dsd.addColumn("Enrollment Date", new CalculationDataDefinition("Enrollment Date",
		        new DateOfEnrollmentArtCalculation()), "", new DateArtStartDateConverter());

		dsd.addColumn("Art Start Date", new ETLArtStartDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		ETLCurrentRegimenDataDefinition currentRegimenDataDefinition = new ETLCurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Current regimen", currentRegimenDataDefinition, paramMapping, null);

		ETLLastVLDateDataDefinition lastVLDateDataDefinition = new ETLLastVLDateDataDefinition();
		lastVLDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last VL Date", lastVLDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		ETLLastVLResultDataDefinition lastVlResultDataDefinition = new ETLLastVLResultDataDefinition();
		lastVlResultDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last VL Result", lastVlResultDataDefinition, "endDate=${endDate}");

		ETLLastVisitDateDataDefinition lastVisitDateDataDefinition = new ETLLastVisitDateDataDefinition();
		lastVisitDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Last Visit Date", lastVisitDateDataDefinition, "endDate=${endDate}", new DateConverter(DATE_FORMAT));

		ETLNextAppointmentDateDataDefinition nextAppointmentDateDataDefinition = new ETLNextAppointmentDateDataDefinition();
		nextAppointmentDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Next appointment date", nextAppointmentDateDataDefinition, paramMapping, null);

		TxcurrWomenNoChildrenContactsCohortDefinition cd = new TxcurrWomenNoChildrenContactsCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, paramMapping);

		return dsd;
	}

}
