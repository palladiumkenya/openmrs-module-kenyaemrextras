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

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.library.hiv.BMIAtLastVisitCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.DateConfirmedHivPositiveCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.IPTOutcomeCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.IPTOutcomeDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.IPTStartDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.LastReturnVisitDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.SecondLastVLCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.CurrentArtRegimenCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateOfEnrollmentArtCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.InitialArtRegimenCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.InitialArtStartDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.TransferInDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.TransferOutDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.ViralLoadResultCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.DateOfDeathCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.DateOfLastCTXCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.PatientCheckOutStatusCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.ValueAtDateOfOtherPatientCalculationCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.VisitsForAPatientCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.WeightAtArtStartDateCalculation;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.CustomDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.DateArtStartDateConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.EncounterDatetimeConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.GenderConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.IPTOutcomeDataConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.ObsDatetimeConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.ObsValueDatetimeConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.ObsValueNumericConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.RDQACalculationResultConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.RDQASimpleObjectRegimenConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.WHOStageDataConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.WeightConverter;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.RDQAActiveCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.RDQACohortDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.CalculationResultDateYYMMDDConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.TBScreeningConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.TBScreeningAtLastVisitDataDefinition;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731.ETLMoh731IndicatorLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731.ETLPmtctIndicatorLibrary;
import org.openmrs.module.kenyaemr.reporting.library.rdqa.RDQAIndicatorLibrary;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeLtfuCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeTxCurrCohortDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemr.common.report.monthlySurgeReport" })
public class MonthlySurgeReportBuilder extends AbstractHybridReportBuilder {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Autowired
	private RDQAIndicatorLibrary rdqa;
	
	@Autowired
	private ETLPmtctIndicatorLibrary pmtctIndicators;
	
	@Autowired
	private ETLMoh731IndicatorLibrary hivIndicators;
	
	/**
	 * @see org.openmrs.module.kenyacore.report.builder.AbstractCohortReportBuilder#addColumns(org.openmrs.module.kenyacore.report.CohortReportDescriptor,
	 *      org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition)
	 */
	@Override
	protected void addColumns(HybridReportDescriptor report, PatientDataSetDefinition dsd) {
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.setName("TX_CURR");
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Unique Patient No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", new GenderConverter());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Art Start Date",
		    new CalculationDataDefinition("Art Start Date", new InitialArtStartDateCalculation()), "",
		    new DateArtStartDateConverter());
		dsd.addColumn(
		    "Current WHO Stage",
		    new ObsForPersonDataDefinition("Last WHO Stage", TimeQualifier.LAST, Dictionary
		            .getConcept(Dictionary.CURRENT_WHO_STAGE), null, null), "", new WHOStageDataConverter());
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new ObsForPersonDataDefinition("Next Appointment Date", TimeQualifier.LAST,
		        Dictionary.getConcept(Dictionary.RETURN_VISIT_DATE), null, null), "", new ObsValueDatetimeConverter());
		
	}
	
	@Override
	protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor descriptor, PatientDataSetDefinition dsd) {
		CohortDefinition cd = new MonthlySurgeTxCurrCohortDefinition();
		cd.setName("TX CURR Patients");
		return ReportUtils.map(cd, "");
	}
	
	protected Mapped<CohortDefinition> txCurrPatientsCohort() {
		CohortDefinition cd = new MonthlySurgeTxCurrCohortDefinition();
		cd.setName("TX Curr Patients");
		return ReportUtils.map(cd, "");
	}
	
	protected Mapped<CohortDefinition> ltfuRecentCohort() {
		CohortDefinition cd = new MonthlySurgeLtfuCohortDefinition();
		cd.setName("LTFU Recent Patients");
		return ReportUtils.map(cd, "");
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition tx_curr = txCurrDataSetDefinition("TX_CURR");
		tx_curr.addRowFilter(txCurrPatientsCohort());
		DataSetDefinition txCurrDSD = tx_curr;
		
		PatientDataSetDefinition ltfuRecentPatients = ltfuRecentDataSetDefinition("LTFU_RECENT");
		ltfuRecentPatients.addRowFilter(ltfuRecentCohort());
		DataSetDefinition ltfuPatientsDSD = ltfuRecentPatients;
		
		return Arrays.asList(ReportUtils.map(txCurrDSD, ""), ReportUtils.map(ltfuPatientsDSD, ""));
	}
	
	protected PatientDataSetDefinition txCurrDataSetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		Concept startIptConcept = Dictionary.getConcept("1265AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		Concept iptOutcomeConcept = Dictionary.getConcept("160632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
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
	
	protected PatientDataSetDefinition ltfuRecentDataSetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		Concept startIptConcept = Dictionary.getConcept("1265AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
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
		dsd.addColumn("Current Regimen",
		    new CalculationDataDefinition("Current Regimen", new CurrentArtRegimenCalculation()), "", null);
		dsd.addColumn("Next Appointment Date", new CalculationDataDefinition("Next Appointment Date",
		        new LastReturnVisitDateCalculation()), "",
		    new DataConverter[] { new CalculationResultDateYYMMDDConverter() });
		
		return dsd;
	}
	
}
