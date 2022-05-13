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
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.calculation.library.hiv.LastReturnVisitDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.CurrentArtRegimenCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.InitialArtStartDateCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.GenderConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.CalculationResultDateYYMMDDConverter;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeLtfuCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeLtfuRtcCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeNewArtCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeTxCurrCohortDefinition;
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
@Builds({ "kenyaemrextras.common.report.monthlySurgeReport" })
public class MonthlySurgeReportBuilder extends AbstractHybridReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Override
	protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor descriptor, PatientDataSetDefinition dsd) {
		return activePatientsCohort();
	}
	
	protected Mapped<CohortDefinition> activePatientsCohort() {
		CohortDefinition cd = new MonthlySurgeTxCurrCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("txCurrPatients");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	protected Mapped<CohortDefinition> ltfuRecentPatientsCohort() {
		CohortDefinition cd = new MonthlySurgeLtfuCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("ltfuRecentPatients");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	protected Mapped<CohortDefinition> ltfuRtcPatientsCohort() {
		CohortDefinition cd = new MonthlySurgeLtfuRtcCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("ltfuRtcPatients");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	protected Mapped<CohortDefinition> newArtPatientsCohort() {
		CohortDefinition cd = new MonthlySurgeNewArtCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("newArtPatients");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition activePatients = activePatientsDataSetDefinition();
		activePatients.addRowFilter(activePatientsCohort());
		DataSetDefinition activePatientsDSD = activePatients;
		
		PatientDataSetDefinition ltfuRecentPatients = ltfuRecentPatientsDataSetDefinition();
		ltfuRecentPatients.addRowFilter(ltfuRecentPatientsCohort());
		DataSetDefinition ltfuRecentPatientsDSD = ltfuRecentPatients;
		
		PatientDataSetDefinition ltfuRtcPatients = ltfuRtcPatientsDataSetDefinition();
		ltfuRtcPatients.addRowFilter(ltfuRtcPatientsCohort());
		DataSetDefinition ltfuRtcPatientsDSD = ltfuRtcPatients;
		
		PatientDataSetDefinition newArtPatients = newOnArtPatientsDataSetDefinition();
		newArtPatients.addRowFilter(newArtPatientsCohort());
		DataSetDefinition newArtPatientsDSD = newArtPatients;
		
		return Arrays.asList(ReportUtils.map(activePatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(ltfuRecentPatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(ltfuRtcPatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(newArtPatientsDSD, "startDate=${startDate},endDate=${endDate}"));
	}
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class));
	}
	
	protected PatientDataSetDefinition activePatientsDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("txCurrPatients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		String defParam = "startDate=${startDate}";
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Unique Patient No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", new GenderConverter());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Art Start Date",
		    new CalculationDataDefinition("Art Start Date", new InitialArtStartDateCalculation()), "",
		    new DateArtStartDateConverter());
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new CalculationDataDefinition("Next Appointment Date",
		        new LastReturnVisitDateCalculation()), "",
		    new DataConverter[] { new CalculationResultDateYYMMDDConverter() });
		
		return dsd;
	}
	
	protected PatientDataSetDefinition ltfuRecentPatientsDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("ltfuRecentPatients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		String defParam = "startDate=${startDate}";
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Unique Patient No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", new GenderConverter());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Art Start Date",
		    new CalculationDataDefinition("Art Start Date", new InitialArtStartDateCalculation()), "",
		    new DateArtStartDateConverter());
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new CalculationDataDefinition("Next Appointment Date",
		        new LastReturnVisitDateCalculation()), "",
		    new DataConverter[] { new CalculationResultDateYYMMDDConverter() });
		
		return dsd;
	}
	
	protected PatientDataSetDefinition ltfuRtcPatientsDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("ltfuRtcPatients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		String defParam = "startDate=${startDate}";
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Unique Patient No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", new GenderConverter());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Art Start Date",
		    new CalculationDataDefinition("Art Start Date", new InitialArtStartDateCalculation()), "",
		    new DateArtStartDateConverter());
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new CalculationDataDefinition("Next Appointment Date",
		        new LastReturnVisitDateCalculation()), "",
		    new DataConverter[] { new CalculationResultDateYYMMDDConverter() });
		
		return dsd;
	}
	
	protected PatientDataSetDefinition newOnArtPatientsDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition("newArtPatients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		String defParam = "startDate=${startDate}";
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Unique Patient No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", new GenderConverter());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Art Start Date",
		    new CalculationDataDefinition("Art Start Date", new InitialArtStartDateCalculation()), "",
		    new DateArtStartDateConverter());
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new CalculationDataDefinition("Next Appointment Date",
		        new LastReturnVisitDateCalculation()), "",
		    new DataConverter[] { new CalculationResultDateYYMMDDConverter() });
		
		return dsd;
	}
}
