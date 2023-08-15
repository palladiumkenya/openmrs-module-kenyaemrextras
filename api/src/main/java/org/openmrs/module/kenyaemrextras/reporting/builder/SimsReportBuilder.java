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
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.MchMetadata;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIIdDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.S0302CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.*;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.EverOnIPTDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.*;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
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
		DataSetDefinition adultsOnArtVLMonitoringDSD = adultsOnArtVLMonitoringDatasetDefinition("S_02_04");
		DataSetDefinition adultsOnArtVirallyUnsupressedDSD = adultsOnARTNonVirallySuppressedDatasetDefinition("S_02_05");
		DataSetDefinition adultsOnArtDSD = adultsOnARTDatasetDefinition("S_02_07");
		DataSetDefinition adultsOnArtWithPresumptiveTBDSD = adultsOnARTWithPresumptiveTBDatasetDefinition("S_02_12");
		DataSetDefinition adultsOnArtHighImpactServicesDSD = adultsOnARTHighImpactServicesDatasetDefinition("S_02_14");
		DataSetDefinition cervicalCancerScreeningDSD = adultsOnArtScreenedForCervicalCancerDatasetDefinition("S_02_17");
		DataSetDefinition pedsNewlyInitiatedOnArtdDSD = pedsNewlyInitiatedOnArtDatasetDefinition("S_02_18");
		DataSetDefinition pedsOnArtVLMonitoringDSD = pedsOnArtVLMonitoringDatasetDefinition("S_02_22");
		DataSetDefinition pedsOnArtVirallyUnsupressedDSD = pedsOnARTNonVirallySuppressedDatasetDefinition("S_02_23");
		DataSetDefinition pedsListedAsContacts = pedsListedAsContactsDatasetDefinition("S_02_25");
		DataSetDefinition pedsOnArtWithTBScreeningResultDSD = pedsOnArtWithTBScreeningResultDatasetDefinition("S_02_26");
		DataSetDefinition pedsOnArtCTXDispensedDSD = pedsOnArtCTXDispensedDatasetDefinition("S_02_28");
		DataSetDefinition pedOnArtWithPresumptiveTBDSD = pedsOnARTWithPresumptiveTBDatasetDefinition("S_02_29");
		DataSetDefinition pedsOnArtScreenedNegTBAndEverOnTPTDSD = pedsOnArtScreenedNegTBAndEverOnTPTDatasetDefinition("S_02_27");
		DataSetDefinition pedsMissedRecentAppointmentDSD = pedsMissedRecentAppointmentDatasetDefinition("S_02_19");
		DataSetDefinition pedsOnARTDSD = pedsOnARTDatasetDefinition("S_02_32");
		DataSetDefinition txCurrKPsWithClinicalVisitLast12MonthsDSD = txCurrKPsWithClinicalVisitLast12MonthsDatasetDefinition("S_03_02");
		DataSetDefinition txCurrKPsWithClinicalVisitLast3MonthsDSD = txCurrKPsWithClinicalVisitLast3MonthsDatasetDefinition("S_03_05");
		DataSetDefinition txNewKPsRetestDSD = txNewKPsHIVRetestDatasetDefinition("S_03_08");
		DataSetDefinition missedAppKPsTracedDSD = missedAppKPsTracedDatasetDefinition("S_03_09");
		DataSetDefinition txCurrKpVLMonitoringDSD = txCurrKpVLMonitoringDatasetDefinition("S_03_11");
		DataSetDefinition txCurrKpNonVirallySuppressedDSD = txCurrKpNonVirallySuppressedDatasetDefinition("S_03_12");
		DataSetDefinition txCurrKpWithTestedContactsDSD = txCurrKpWithTestedContactsDatasetDefinition("S_03_14");
		DataSetDefinition txCurrKpWithTestedChildContactsDSD = txCurrKpWithTestedChildContactsDatasetDefinition("S_03_15");
		DataSetDefinition txCurrKpWithTBScreeningResultDSD = txCurrKpWithTBScreeningResultDatasetDefinition("S_03_16");
		DataSetDefinition txCurrKPsRecentPositivesARTInitiationDSD = txCurrKPsRecentPositivesARTInitiationDatasetDefinition("S_03_10");
		DataSetDefinition txCurrKPsTBNegTPTInitiationDSD = txCurrKPsTBNegTPTInitiationDatasetDefinition("S_03_17");
		DataSetDefinition txCurrKPsCTXDocumentationDSD = txCurrKPsCTXDocumentationDatasetDefinition("S_03_18");
		DataSetDefinition txCurrKPsPresumptiveDocumentationDSD = txCurrKPsPresumptiveTBDocumentationDatasetDefinition("S_03_19");
		DataSetDefinition keyPopsOnARTHighImpactServicesDSD = keyPopsOnARTHighImpactServicesDatasetDefinition("S_03_21");
		DataSetDefinition txCurrKPsCacxTreatmentDocumentationDSD = txCurrKPsCacxTreatmentDocumentationDatasetDefinition("S_03_24");
		DataSetDefinition txNewPregnantOrBFRetestDocumentationDSD = txNewPregnantOrBFRetestDocumentationDatasetDefinition("S_04_01");
		DataSetDefinition txCurrPregnantOrBFMissedAppTracingDocumentationDSD = txCurrPregnantOrBFMissedAppTracingDocumentationDatasetDefinition("S_04_02");
		DataSetDefinition txCurrPregnantAndBFWithTestedChildContactsDSD = txCurrPregnantAndBFWithTestedChildContactsDatasetDefinition("S_04_08");
		DataSetDefinition txCurrPregnantAndBFWithTBScreeningResultDSD = txCurrPregnantAndBFWithTBScreeningResultDatasetDefinition("S_04_09");
		DataSetDefinition txCurrPregnantBFWTBScreenedNegativePostTPTDSD = txCurrPregnantBFWTBScreenedNegativePostTPTDatasetDefinition("S_04_10");
		DataSetDefinition txCurrPregnantBFCTXDispensedDSD = txCurrPregnantBFCTXDispensedDatasetDefinition("S_04_11");
		DataSetDefinition txCurrPregnantBFWithPresumptiveTBDSD = txCurrPregnantBFWithPresumptiveTBDatasetDefinition("S_04_12");
		DataSetDefinition txCurrPregnantBFWithHtsTestAtMatWithin3MonthsDSD = txCurrPregnantBFWithHtsTestAtMatWithin3MonthsDatasetDefinition("S_04_13");
		DataSetDefinition txCurrPregnantBFWithMotherAndBabyProphylaxisDSD = txCurrPregnantBFWithMotherAndBabyProphylaxisDatasetDefinition("S_04_14");
		DataSetDefinition txCurrPregnantBFVLLoadAccessAndMonitoringDSD = txCurrPregnantBFVLLoadAccessAndMonitoringDatasetDefinition("S_04_03");
		DataSetDefinition txCurrPregnantBFHighVLManagementDSD = txCurrPregnantBFHighVLManagementDatasetDefinition("S_04_04");
		DataSetDefinition txCurrPregnantBFPartnerServicesDSD = txCurrPregnantBFPartnerServicesDatasetDefinition("S_04_07");
		DataSetDefinition earlyInfantDiagnosisDSD = earlyInfantDiagnosisDatasetDefinition("S_04_15");
		DataSetDefinition earlyInfantConfirmatoryTestingDSD = earlyInfantConfirmatoryTestingDatasetDefinition("S_04_17");
		DataSetDefinition artProvisionForHIVPosAdultTBPatientsDSD = artProvisionForHIVPosAdultTBPatients("S_08_02");
		DataSetDefinition htsLinkageToHIVCareAndTreatmentDSD = htsLinkageToHIVCareAndTreatment("S_07_03");
		DataSetDefinition hei3To12MonthsOldOnCTXBy8WeeksDSD = hei3To12MonthsOldOnCTXBy8WeeksDatasetDefinition("S_04_18");
		DataSetDefinition hei24To36MonthsWithDocumentedFinalResultDSD = hei24To36MonthsWithDocumentedFinalResultDatasetDefinition("S_04_19");
		DataSetDefinition hei3To12MonthsOldLinkedToTreatmentDSD = hei3To12MonthsOldLinkedToTreatmentDatasetDefinition("S_04_20");
		DataSetDefinition vmmcClientsDSD = vmmcClientsDatasetDefinition("S_05_01");
		
		return Arrays
		        .asList(ReportUtils.map(newlyInitiatedOnArtPatientsDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(missedAppointmentsDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                sameDayInitiationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                adultsOnArtVirallyUnsupressedDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                adultsOnArtDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                adultsOnArtWithPresumptiveTBDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnArtWithTBScreeningResultDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                adultsOnArtHighImpactServicesDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnArtCTXDispensedDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnArtScreenedNegTBAndEverOnTPTDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(adultsOnArtVLMonitoringDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnArtVLMonitoringDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsMissedRecentAppointmentDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnARTDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsNewlyInitiatedOnArtdDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedOnArtWithPresumptiveTBDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                pedsOnArtVirallyUnsupressedDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                cervicalCancerScreeningDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsWithClinicalVisitLast12MonthsDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(txCurrKPsWithClinicalVisitLast3MonthsDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txNewKPsRetestDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                missedAppKPsTracedDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKpVLMonitoringDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKpNonVirallySuppressedDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKpWithTestedContactsDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKpWithTestedChildContactsDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKpWithTBScreeningResultDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsRecentPositivesARTInitiationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(txCurrKPsTBNegTPTInitiationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(pedsListedAsContacts, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsTBNegTPTInitiationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsCTXDocumentationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsPresumptiveDocumentationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrKPsCacxTreatmentDocumentationDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(txNewPregnantOrBFRetestDocumentationDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txCurrPregnantOrBFMissedAppTracingDocumentationDSD,
		                "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrPregnantAndBFWithTestedChildContactsDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils
		                    .map(txCurrPregnantAndBFWithTBScreeningResultDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txCurrPregnantBFWTBScreenedNegativePostTPTDSD,
		                "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(txCurrPregnantBFCTXDispensedDSD,
		                "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(txCurrPregnantBFWithPresumptiveTBDSD,
		                "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrPregnantBFWithHtsTestAtMatWithin3MonthsDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txCurrPregnantBFWithMotherAndBabyProphylaxisDSD,
		                "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                txCurrPregnantBFVLLoadAccessAndMonitoringDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txCurrPregnantBFHighVLManagementDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(txCurrPregnantBFPartnerServicesDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(earlyInfantDiagnosisDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                earlyInfantConfirmatoryTestingDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                artProvisionForHIVPosAdultTBPatientsDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils
		                    .map(htsLinkageToHIVCareAndTreatmentDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(earlyInfantDiagnosisDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                earlyInfantConfirmatoryTestingDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                hei3To12MonthsOldOnCTXBy8WeeksDSD, "startDate=${startDate},endDate=${endDate}"), ReportUtils.map(
		                hei24To36MonthsWithDocumentedFinalResultDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(hei3To12MonthsOldLinkedToTreatmentDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(keyPopsOnARTHighImpactServicesDSD, "startDate=${startDate},endDate=${endDate}"),
		            ReportUtils.map(vmmcClientsDSD, "startDate=${startDate},endDate=${endDate}"));
		
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
		
		LastHtsRetestTestOneResultDataDefinition test1DataDefinition = new LastHtsRetestTestOneResultDataDefinition();
		test1DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test1DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("test 1 result", test1DataDefinition, indParams, null);
		
		LastHtsRetestTestTwoResultDataDefinition test2DataDefinition = new LastHtsRetestTestTwoResultDataDefinition();
		test2DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test2DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("test 2 result", test2DataDefinition, indParams, null);
		
		SimsRetestVerificationDataDefinition retestVerificationDataDefinition = new SimsRetestVerificationDataDefinition();
		retestVerificationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		retestVerificationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("S_02_01 Q3", retestVerificationDataDefinition, indParams, null);
		
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
		dsd.addColumn("Attempts Done", isTracingAttemptsDoneDataDefinition, indParams, null);
		
		SimsTracingOutcomeDataDefinition tracingOutcomeDataDefinition = new SimsTracingOutcomeDataDefinition();
		tracingOutcomeDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Tracing outcome", tracingOutcomeDataDefinition, indParams, null);
		
		SimsTracingOutcomeDocumentedDataDefinition tracingOutcomeDocumentedDataDefinition = new SimsTracingOutcomeDocumentedDataDefinition();
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Documented Outcome", tracingOutcomeDocumentedDataDefinition, indParams, null);
		
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
		dsd.addColumn("HIV Diagnosis Date", hivDiagnosisDateDataDefinition, indParams, new DateConverter(DATE_FORMAT));
		
		SimsHivARTInitiationDateDataDefinition artInitiationDateDataDefinition = new SimsHivARTInitiationDateDataDefinition();
		artInitiationDateDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		artInitiationDateDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("ART Initiation Date", artInitiationDateDataDefinition, indParams, new DateConverter(DATE_FORMAT));
		
		SimsSameDayARTInitiationDataDefinition simsSameDayARTInitiationDataDefinition = new SimsSameDayARTInitiationDataDefinition();
		simsSameDayARTInitiationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsSameDayARTInitiationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_02_03 Q2", simsSameDayARTInitiationDataDefinition, indParams, null);
		
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
		
		SimsEACAfterUnsuppressedVLStatusDataDefinition enhancedAdherenceDocumentedDataDefinition = new SimsEACAfterUnsuppressedVLStatusDataDefinition();
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Enhance Adherence Documented", enhancedAdherenceDocumentedDataDefinition, indParams, null);
		
		SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition followUpVLTakenDataDefinition = new SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition();
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
	
	protected PatientDataSetDefinition adultsOnARTHighImpactServicesDatasetDefinition(String datasetName) {
		
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
		
		SimsGPHighImpactServicesDataDefinition highImpactServicesDataDefinition = new SimsGPHighImpactServicesDataDefinition();
		highImpactServicesDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		highImpactServicesDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("High Impact Services", highImpactServicesDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0214CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Adults on ART received High impact services");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition keyPopsOnARTHighImpactServicesDatasetDefinition(String datasetName) {
		
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
		
		SimsKPsHighImpactServicesDataDefinition kpsHighImpactServicesDataDefinition = new SimsKPsHighImpactServicesDataDefinition();
		kpsHighImpactServicesDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		kpsHighImpactServicesDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("KPs High Impact Services", kpsHighImpactServicesDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0321CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KPs on ART received High impact services");
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
		dsd.addColumn("Peds Tracing Attempts Made", isTracingAttemptsDoneDataDefinition, indParams, null);
		
		SimsTracingOutcomeDocumentedDataDefinition tracingOutcomeDocumentedDataDefinition = new SimsTracingOutcomeDocumentedDataDefinition();
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tracingOutcomeDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds Tracing Outcome", tracingOutcomeDocumentedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0219CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Missed Most Recent Appointment");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsNewlyInitiatedOnArtDatasetDefinition(String datasetName) {
		
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
		
		LastHtsRetestTestOneResultDataDefinition test1DataDefinition = new LastHtsRetestTestOneResultDataDefinition();
		test1DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test1DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("test 1 result", test1DataDefinition, indParams, null);
		
		LastHtsRetestTestTwoResultDataDefinition test2DataDefinition = new LastHtsRetestTestTwoResultDataDefinition();
		test2DataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		test2DataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("test 2 result", test2DataDefinition, indParams, null);
		
		SimsRetestVerificationDataDefinition retestVerificationDataDefinition = new SimsRetestVerificationDataDefinition();
		retestVerificationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		retestVerificationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Ped Retest", retestVerificationDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0218CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Newly initiated on ART");
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	protected PatientDataSetDefinition adultsOnArtScreenedForCervicalCancerDatasetDefinition(String datasetName) {
		
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
		
		SimsScreenedPostiveForCervicalCancerDataDefinition cervicalCancerDataDefinition = new SimsScreenedPostiveForCervicalCancerDataDefinition();
		cervicalCancerDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cervicalCancerDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Screened Positive for Cervical Cancer", cervicalCancerDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0217CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Screened For Cervical Cancer");
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs with clinical visits within the last 12 months Checks whether there was
	 * documentation for STI screening
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsWithClinicalVisitLast12MonthsDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrKPsSTIScreeningDocumentationStatusDataDefinition stiScreeningDocumentationStatusDataDefinition = new SimsTxCurrKPsSTIScreeningDocumentationStatusDataDefinition();
		stiScreeningDocumentationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		stiScreeningDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_02 Q4", stiScreeningDocumentationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0302CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs with clinical visits within the last 3 months and their KP typology documentation
	 * status
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsWithClinicalVisitLast3MonthsDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrKPsTypologyDocumentationStatusDataDefinition kpTypologyDocumentationStatusDataDefinition = new SimsTxCurrKPsTypologyDocumentationStatusDataDefinition();
		kpTypologyDocumentationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		kpTypologyDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_05 Q4", kpTypologyDocumentationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0305CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * TX_NEW KPs aged 15+ retest documentation
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txNewKPsHIVRetestDatasetDefinition(String datasetName) {
		
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
		
		SimsTxNewKPRetestDocumentationStatusDataDefinition txNewKPRetestDocumentationStatusDataDefinition = new SimsTxNewKPRetestDocumentationStatusDataDefinition();
		txNewKPRetestDocumentationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		txNewKPRetestDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_08 Q3", txNewKPRetestDocumentationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0308CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Dataset definition for ART KPs who missed appointment and their tracing status
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition missedAppKPsTracedDatasetDefinition(String datasetName) {
		
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
		
		SimsKPsMissedAppTrackingDocumentationStatusDataDefinition missedAppKPsTracingDocumentationStatusDataDefinition = new SimsKPsMissedAppTrackingDocumentationStatusDataDefinition();
		missedAppKPsTracingDocumentationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		missedAppKPsTracingDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		SimsKPsMissedAppTracingResultsDocumentationStatusDataDefinition simsKPsMissedAppTracingResultsDocumentationStatusDataDefinition = new SimsKPsMissedAppTracingResultsDocumentationStatusDataDefinition();
		simsKPsMissedAppTracingResultsDocumentationStatusDataDefinition.addParameter(new Parameter("startDate",
		        "Start Date", Date.class));
		simsKPsMissedAppTracingResultsDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date",
		        Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_09 Q2", missedAppKPsTracingDocumentationStatusDataDefinition, indParams, null);
		dsd.addColumn("S_03_09 Q3", simsKPsMissedAppTracingResultsDocumentationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0309CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnARTWithPresumptiveTBDatasetDefinition(String datasetName) {
		
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
		
		SimsPedSmearCultureXpertResultsDataDefinition simsSmearCultureXpertResultsDataDefinition = new SimsPedSmearCultureXpertResultsDataDefinition();
		simsSmearCultureXpertResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsSmearCultureXpertResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds Smear/Culture/GeneXpert/chestXray Resutls", simsSmearCultureXpertResultsDataDefinition,
		    indParams, null);
		
		CohortDefinition cd = new S0229CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("On ART with Presumptive TB");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrKpVLMonitoringDatasetDefinition(String datasetName) {
		
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
		
		SimsKpRecentVLTestOrderedDataDefinition kPRecentVLTestOrderedDataDefinition = new SimsKpRecentVLTestOrderedDataDefinition();
		kPRecentVLTestOrderedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		kPRecentVLTestOrderedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("KP Test Ordered", kPRecentVLTestOrderedDataDefinition, indParams, null);
		
		SimsKpRecentVLResultsDataDefinition kpRecentVLResultsDataDefinition = new SimsKpRecentVLResultsDataDefinition();
		kpRecentVLResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		kpRecentVLResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("KP Recent VL Results", kpRecentVLResultsDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0311CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KPs Current on ART VL Monitoring");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrKpNonVirallySuppressedDatasetDefinition(String datasetName) {
		
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
		
		SimsEACAfterUnsuppressedVLStatusDataDefinition enhancedAdherenceDocumentedDataDefinition = new SimsEACAfterUnsuppressedVLStatusDataDefinition();
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Enhance Adherence Documented", enhancedAdherenceDocumentedDataDefinition, indParams, null);
		
		SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition followUpVLTakenDataDefinition = new SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition();
		followUpVLTakenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		followUpVLTakenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Follow Up VL", followUpVLTakenDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0312CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KP Non virally suppressed");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrKpWithTestedContactsDatasetDefinition(String datasetName) {
		
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
		
		SimsElicitedContactsDataDefinition simsElicitedContactsDataDefinition = new SimsElicitedContactsDataDefinition();
		simsElicitedContactsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsElicitedContactsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Contacts Elicited", simsElicitedContactsDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0314CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KP Adults on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrKpWithTestedChildContactsDatasetDefinition(String datasetName) {
		
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
		
		SimsChildListedAsContactDataDefinition simsChildListedAsContactDataDefinition = new SimsChildListedAsContactDataDefinition();
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Child Listed as Contact", simsChildListedAsContactDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0315CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KP Adults on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrKpWithTBScreeningResultDatasetDefinition(String datasetName) {
		
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
		
		CohortDefinition cd = new S0316CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("KP Adults on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs with clinical visits within the last 3 months who recently turned HIV+ and their
	 * ART initiation documentation
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsRecentPositivesARTInitiationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxNewKPARTInitiationDocumentationStatusDataDefinition kpTxCurrNewPositivesRapidARTInitiationDataDefinition = new SimsTxNewKPARTInitiationDocumentationStatusDataDefinition();
		kpTxCurrNewPositivesRapidARTInitiationDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		kpTxCurrNewPositivesRapidARTInitiationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_10 Q2", kpTxCurrNewPositivesRapidARTInitiationDataDefinition, indParams, null);
		CohortDefinition cd = new S0310CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs who screened Negative for TB and TPT initiation documentation
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsTBNegTPTInitiationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrKPTBNegTPTDocumentationStatusDataDefinition kpTxCurrTBNegTPTInitiationStatusDataDefinition = new SimsTxCurrKPTBNegTPTDocumentationStatusDataDefinition();
		kpTxCurrTBNegTPTInitiationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		kpTxCurrTBNegTPTInitiationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_17 Q3", kpTxCurrTBNegTPTInitiationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0317CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsListedAsContactsDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		PatientIdentifierType openMRSId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataDefinition identifierOpenMRSDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(openMRSId.getName(), openMRSId), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("OpenMRS ID", identifierOpenMRSDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsPedsListedAsContactsHIVStatusDocumentedDataDefinition simsPedsContactHIVStatusDocumentedDataDefinition = new SimsPedsListedAsContactsHIVStatusDocumentedDataDefinition();
		simsPedsContactHIVStatusDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsPedsContactHIVStatusDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Ped Status Documented", simsPedsContactHIVStatusDocumentedDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0225CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Listed as Contacts");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs CTX dispense documentation
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsCTXDocumentationDatasetDefinition(String datasetName) {
		
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
		
		SimsCTXDispensedDataDefinition simsCTXDispensedDataDefinition = new SimsCTXDispensedDataDefinition();
		simsCTXDispensedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsCTXDispensedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_18 Q1", simsCTXDispensedDataDefinition, indParams, null);
		CohortDefinition cd = new S0318CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_Curr KPs with presumptive TB and testing/results status
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsPresumptiveTBDocumentationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrKPPresumedTBTestingResultsDocumentationStatusDataDefinition simsTBTestingResultsDataDefinition = new SimsTxCurrKPPresumedTBTestingResultsDocumentationStatusDataDefinition();
		simsTBTestingResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTBTestingResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		SimsTxCurrKPPresumedTBTestingDocumentationStatusDataDefinition simsTBTestingDataDefinition = new SimsTxCurrKPPresumedTBTestingDocumentationStatusDataDefinition();
		simsTBTestingDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTBTestingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_19 Q3", simsTBTestingResultsDataDefinition, indParams, null);
		dsd.addColumn("S_03_19 Q4", simsTBTestingDataDefinition, indParams, null);
		CohortDefinition cd = new S0319CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * TX_CURR KPs CACX screening treatment
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrKPsCacxTreatmentDocumentationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition simsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition = new SimsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition();
		simsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		simsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition.addParameter(new Parameter("endDate", "End Date",
		        Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_03_24 Q3", simsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition, indParams, null);
		CohortDefinition cd = new S0324CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_New Pregnant and BF mothers Retesting before ART initiation
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txNewPregnantOrBFRetestDocumentationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxNewPregBreastFeedingRetestDocumentationStatusDataDefinition simsBreastFeedingPregnantRetestDataDefinition = new SimsTxNewPregBreastFeedingRetestDocumentationStatusDataDefinition();
		simsBreastFeedingPregnantRetestDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsBreastFeedingPregnantRetestDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_04_01 Q3", simsBreastFeedingPregnantRetestDataDefinition, indParams, null);
		CohortDefinition cd = new S0401CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	/**
	 * Tx_New Pregnant and BF mothers missed appointment and tracing status
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrPregnantOrBFMissedAppTracingDocumentationDatasetDefinition(String datasetName) {
		
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
		
		SimsTxCurrPregBreastFeedingMissedAppTracingStatusDataDefinition simsBreastFeedingPregnantTracingStatusDataDefinition = new SimsTxCurrPregBreastFeedingMissedAppTracingStatusDataDefinition();
		simsBreastFeedingPregnantTracingStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		simsBreastFeedingPregnantTracingStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		SimsTxCurrPregBreastFeedingMissedAppTracingResultsStatusDataDefinition simsBreastFeedingPregnantTracingResultsDataDefinition = new SimsTxCurrPregBreastFeedingMissedAppTracingResultsStatusDataDefinition();
		simsBreastFeedingPregnantTracingResultsDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		simsBreastFeedingPregnantTracingResultsDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("S_04_02 Q2", simsBreastFeedingPregnantTracingStatusDataDefinition, indParams, null);
		dsd.addColumn("S_04_02 Q3", simsBreastFeedingPregnantTracingResultsDataDefinition, indParams, null);
		CohortDefinition cd = new S0402CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnARTNonVirallySuppressedDatasetDefinition(String datasetName) {
		
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
		
		SimsEnhancedAdherenceDocumentedDataDefinition enhancedAdherenceDocumentedDataDefinition = new SimsEnhancedAdherenceDocumentedDataDefinition();
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		enhancedAdherenceDocumentedDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Enhance Adherence Documented", enhancedAdherenceDocumentedDataDefinition, indParams, null);
		
		SimsFollowUpVLTakenDataDefinition followUpVLTakenDataDefinition = new SimsFollowUpVLTakenDataDefinition();
		followUpVLTakenDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		followUpVLTakenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Follow Up VL", followUpVLTakenDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0223CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Non Virally suppressed");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition pedsOnARTDatasetDefinition(String datasetName) {
		
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
		
		SimsPedsHighImpactServicesDataDefinition pedsReceivedHighImpactServices = new SimsPedsHighImpactServicesDataDefinition();
		pedsReceivedHighImpactServices.addParameter(new Parameter("startDate", "Start Date", Date.class));
		pedsReceivedHighImpactServices.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Peds received High impact services", pedsReceivedHighImpactServices, indParams, null);
		
		CohortDefinition cd = new S0232CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Peds Currently on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * Tx_Curr Pregnant and BF mothers with tested biological child contact
	 * 
	 * @param datasetName
	 * @return
	 */
	
	protected PatientDataSetDefinition txCurrPregnantAndBFWithTestedChildContactsDatasetDefinition(String datasetName) {
		
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
		
		SimsChildListedAsContactDataDefinition simsChildListedAsContactDataDefinition = new SimsChildListedAsContactDataDefinition();
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsChildListedAsContactDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Child Listed as Contact", simsChildListedAsContactDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0408CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("PMTCT on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantAndBFWithTBScreeningResultDatasetDefinition(String datasetName) {
		
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
		
		CohortDefinition cd = new S0409CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("PMTCT on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantBFWTBScreenedNegativePostTPTDatasetDefinition(String datasetName) {
		
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
		dsd.addColumn("Negative TB Result Ever on TPT", negativeTBResultsAndEverOnTPTDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0410CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("PMTCT on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantBFCTXDispensedDatasetDefinition(String datasetName) {
		
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
		
		CohortDefinition cd = new S0411CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("PMTCT on ART");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantBFWithPresumptiveTBDatasetDefinition(String datasetName) {
		
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
		
		CohortDefinition cd = new S0412CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("On ART with Presumptive TB");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantBFWithHtsTestAtMatWithin3MonthsDatasetDefinition(String datasetName) {
		
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
		
		SimsTestedWithin3MonthsOfMaternityDataDefinition simsTestedWithin3MonthsOfMaternityDataDefinition = new SimsTestedWithin3MonthsOfMaternityDataDefinition();
		simsTestedWithin3MonthsOfMaternityDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsTestedWithin3MonthsOfMaternityDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Tested within 3 months maternity", simsTestedWithin3MonthsOfMaternityDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0413CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("On ART with HTS Test at Delivery");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	protected PatientDataSetDefinition txCurrPregnantBFWithMotherAndBabyProphylaxisDatasetDefinition(String datasetName) {
		
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
		
		SimsMotherAndBabyProphylaxisGivenMaternityDataDefinition simsMotherAndBabyProphylaxisGivenMaternityDataDefinition = new SimsMotherAndBabyProphylaxisGivenMaternityDataDefinition();
		simsMotherAndBabyProphylaxisGivenMaternityDataDefinition.addParameter(new Parameter("startDate", "Start Date",
		        Date.class));
		simsMotherAndBabyProphylaxisGivenMaternityDataDefinition.addParameter(new Parameter("endDate", "End Date",
		        Date.class));
		dsd.addColumn("Mother given prophylaxis at delivery", simsMotherAndBabyProphylaxisGivenMaternityDataDefinition,
		    indParams, null);
		
		CohortDefinition cd = new S0414CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("On ART Prophylaxis given");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_03:Pregnant/Breastfeeding (BF) women on ART: Review 10 randomly selected charts of
	 * pregnant and breastfeeding patients on ART >6 months.
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrPregnantBFVLLoadAccessAndMonitoringDatasetDefinition(String datasetName) {
		
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
		
		SimsPregBFVLOrderedWithinIntervalStatusDataDefinition simsVLOrderedWithinInterval = new SimsPregBFVLOrderedWithinIntervalStatusDataDefinition();
		simsVLOrderedWithinInterval.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsVLOrderedWithinInterval.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_03 Q2", simsVLOrderedWithinInterval, indParams, null);
		
		SimsPregBFLastVLResultStatusDataDefinition simsRecentVLResultStatus = new SimsPregBFLastVLResultStatusDataDefinition();
		simsRecentVLResultStatus.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsRecentVLResultStatus.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_03 Q3", simsRecentVLResultStatus, indParams, null);
		
		CohortDefinition cd = new S0403CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_04:Pregnant/Breastfeeding (BF) women on ART: Review 10 records (e.g., charts, high viral
	 * load register, EMR entries) of pregnant and breastfeeding patients on ART ≥12 months with
	 * virologic non-suppression.
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrPregnantBFHighVLManagementDatasetDefinition(String datasetName) {
		
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
		
		SimsEACAfterUnsuppressedVLStatusDataDefinition eacAfterUnsupressedvl = new SimsEACAfterUnsuppressedVLStatusDataDefinition();
		eacAfterUnsupressedvl.addParameter(new Parameter("startDate", "Start Date", Date.class));
		eacAfterUnsupressedvl.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_04 Q2", eacAfterUnsupressedvl, indParams, null);
		
		SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition simsRepeatVLAfterUnsuppressedResults = new SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition();
		simsRepeatVLAfterUnsuppressedResults.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsRepeatVLAfterUnsuppressedResults.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_04 Q3", simsRepeatVLAfterUnsuppressedResults, indParams, null);
		
		CohortDefinition cd = new S0404CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_07:Pregnant/Breastfeeding (BF) women on ART: Review 10 register entries (individual or
	 * index/partner testing logbook) or charts (whichever source has the most updated information)
	 * of HIV-positive pregnant and breastfeeding patients on ART ≥12 months.
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition txCurrPregnantBFPartnerServicesDatasetDefinition(String datasetName) {
		
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
		
		SimsPregBFAllContactTestingStatusDataDefinition allPartnersTested = new SimsPregBFAllContactTestingStatusDataDefinition();
		allPartnersTested.addParameter(new Parameter("startDate", "Start Date", Date.class));
		allPartnersTested.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_07 Q3", allPartnersTested, indParams, null);
		
		CohortDefinition cd = new S0407CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_15: HIV-Exposed Infants (HEI): Review 10 records (register entries, charts, or HEI
	 * cards) of the most recent HIV-infected infants (i.e. born 3 or more months prior to the SIMS
	 * assessment and up to the last 12 months prior to today’s SIMS assessment).
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition earlyInfantDiagnosisDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		PatientIdentifierType heiID = MetadataUtils.existing(PatientIdentifierType.class,
		    MchMetadata._PatientIdentifierType.HEI_ID_NUMBER);
		DataDefinition heiIdentifierDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(heiID.getName(), heiID), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("heiID", heiIdentifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHIVPosHEIsEIDStatusDataDefinition heiEIDStatus = new SimsHIVPosHEIsEIDStatusDataDefinition();
		heiEIDStatus.addParameter(new Parameter("startDate", "Start Date", Date.class));
		heiEIDStatus.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_15 Q1", heiEIDStatus, indParams, null);
		
		SimsHIVPosHEIsResultsInAMonthStatusDataDefinition heiTestResultsGivenToCareGiver = new SimsHIVPosHEIsResultsInAMonthStatusDataDefinition();
		heiTestResultsGivenToCareGiver.addParameter(new Parameter("startDate", "Start Date", Date.class));
		heiTestResultsGivenToCareGiver.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_15 Q2", heiTestResultsGivenToCareGiver, indParams, null);
		
		CohortDefinition cd = new S0415CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_17: HIV-Exposed Infants (HEI): Review 10 records (register entries, charts, or HEI
	 * cards) of the most recent HIV-infected infants (i.e. born 3 or more months prior to the SIMS
	 * assessment and up to the last 12 months prior to today’s SIMS assessment) who had an initial
	 * positive virologic test result.
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition earlyInfantConfirmatoryTestingDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		PatientIdentifierType heiID = MetadataUtils.existing(PatientIdentifierType.class,
		    MchMetadata._PatientIdentifierType.HEI_ID_NUMBER);
		DataDefinition heiIdentifierDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(heiID.getName(), heiID), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("HEI ID", heiIdentifierDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHIVPosHEIsConfirmatoryVirologicResultStatusDataDefinition heiConfirmatoryVirologicResultsStatus = new SimsHIVPosHEIsConfirmatoryVirologicResultStatusDataDefinition();
		heiConfirmatoryVirologicResultsStatus.addParameter(new Parameter("startDate", "Start Date", Date.class));
		heiConfirmatoryVirologicResultsStatus.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_04_17 Q1", heiConfirmatoryVirologicResultsStatus, indParams, null);
		
		CohortDefinition cd = new S0417CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_18: HIV-Exposed Infants (HEI): Review 10 records (register entries, charts, or HEI
	 * cards) of the most recent HIV-infected infants (i.e. born 3 or more months prior to the SIMS
	 * assessment and up to the last 12 months prior to today’s SIMS assessment) with documentation
	 * that CTX was initiated by 8 weeks of age
	 * 
	 * @param datasetName
	 * @return
	 */
	
	protected PatientDataSetDefinition hei3To12MonthsOldOnCTXBy8WeeksDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		PatientIdentifierType openMRSId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		DataDefinition identifierOpenMRSDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(openMRSId.getName(), openMRSId), identifierFormatter);
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("HEI Id", new HEIIdDataDefinition(), "");
		dsd.addColumn("OpenMRS ID", identifierOpenMRSDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHEIStartedCtxBy8WeeksDataDefinition simsHEIStartedCtxBy8WeeksDataDefinition = new SimsHEIStartedCtxBy8WeeksDataDefinition();
		simsHEIStartedCtxBy8WeeksDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsHEIStartedCtxBy8WeeksDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Hei Given CTX by 8 weeks", simsHEIStartedCtxBy8WeeksDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0418CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("HEI started on CTX by 8 weeks");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_19: HIV-Exposed Infants (HEI): Review 10 records (register entries, charts, or HEI
	 * cards) of the most recent HIV-infected infants (i.e. born 3 or more months prior to the SIMS
	 * assessment and up to the last 12 months prior to today’s SIMS assessment) with documented
	 * final HIV testing result
	 * 
	 * @param datasetName
	 * @return
	 */
	
	protected PatientDataSetDefinition hei24To36MonthsWithDocumentedFinalResultDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		PatientIdentifierType openMRSId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		DataDefinition identifierOpenMRSDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(openMRSId.getName(), openMRSId), identifierFormatter);
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("HEI Id", new HEIIdDataDefinition(), "");
		dsd.addColumn("OpenMRS ID", identifierOpenMRSDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHEIWithDocumentedFinalStatusDataDefinition simsHEIWithDocumentedFinalStatusDataDefinition = new SimsHEIWithDocumentedFinalStatusDataDefinition();
		simsHEIWithDocumentedFinalStatusDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsHEIWithDocumentedFinalStatusDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Hei with final hiv status results", simsHEIWithDocumentedFinalStatusDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0419CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("HEI 24 to 36 Months");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * S_04_20: HIV-Exposed Infants (HEI): Review 10 records (register entries, charts, or HEI
	 * cards) of the most recent HIV-infected infants (i.e. born 3 or more months prior to the SIMS
	 * assessment and up to the last 12 months prior to today’s SIMS assessment) with documented
	 * linkage to treatment/initation on ART
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition hei3To12MonthsOldLinkedToTreatmentDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		PatientIdentifierType openMRSId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		DataDefinition identifierOpenMRSDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(openMRSId.getName(), openMRSId), identifierFormatter);
		
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("HEI Id", new HEIIdDataDefinition(), "");
		dsd.addColumn("OpenMRS ID", identifierOpenMRSDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsHEIDocumentedLinkageToTreatmentDataDefinition simsHEIDocumentedLinkageToTreatmentDataDefinition = new SimsHEIDocumentedLinkageToTreatmentDataDefinition();
		simsHEIDocumentedLinkageToTreatmentDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsHEIDocumentedLinkageToTreatmentDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("Hei linked to treatment", simsHEIDocumentedLinkageToTreatmentDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0420CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("HEI 3 to 12 months");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * Precision and safeguarding of VMMC Clinical Records: Do the records contain the following:
	 * complete contact details, history and physical exam, weight, Blood Pressure, surgical method,
	 * follow-up date and presence/absence of Adverse Events, and stored in a locked location?
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition vmmcClientsDatasetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		PatientIdentifierType openMRSId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		DataDefinition identifierOpenMRSDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(openMRSId.getName(), openMRSId), identifierFormatter);
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("OpenMRS ID", identifierOpenMRSDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		
		SimsVMMCDocumentationDataDefinition simsVMMCDocumentationDataDefinition = new SimsVMMCDocumentationDataDefinition();
		simsVMMCDocumentationDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		simsVMMCDocumentationDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("VMMC Documentation", simsVMMCDocumentationDataDefinition, indParams, null);
		
		CohortDefinition cd = new S0501CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("VMMC Clients");
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * TB patients diagnosed with HIV more than 3 months but less than 12 months prior to the SIMS
	 * assessment.
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition artProvisionForHIVPosAdultTBPatients(String datasetName) {
		
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
		
		SimsHIVPosTBPatientARTInitiationStatusDataDefinition tbPatientArtInitiation = new SimsHIVPosTBPatientARTInitiationStatusDataDefinition();
		tbPatientArtInitiation.addParameter(new Parameter("startDate", "Start Date", Date.class));
		tbPatientArtInitiation.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_08_02 Q2", tbPatientArtInitiation, indParams, null);
		
		CohortDefinition cd = new S0802CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
	/**
	 * HTS Linkage to HIV Care and Treatment
	 * 
	 * @param datasetName
	 * @return
	 */
	protected PatientDataSetDefinition htsLinkageToHIVCareAndTreatment(String datasetName) {
		
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
		
		SimsNewHIVPosLinkageToTreatmentDataDefinition newHIVPosLinkageToTreatment = new SimsNewHIVPosLinkageToTreatmentDataDefinition();
		newHIVPosLinkageToTreatment.addParameter(new Parameter("startDate", "Start Date", Date.class));
		newHIVPosLinkageToTreatment.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addColumn("S_07_03 Q4", newHIVPosLinkageToTreatment, indParams, null);
		
		CohortDefinition cd = new S0703CohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addRowFilter(cd, indParams);
		
		return dsd;
	}
	
}
