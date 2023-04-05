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
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIEnrollmentDateDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEIIdDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.hei.HEISerialNumberDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.pama.PamaCareGiverStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHEICohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivAndTBPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedTBPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.AgeAtDeathDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiCareGiverEducationDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiCareGiverOccupationDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiDateOfDeathDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMaritalStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherArtRegimenDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherDateArtInitiatedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherEntryPointDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherGestationAgeDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherHaartStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherHeightDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherHivDiagnosisDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherLatestTriageDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherMuacDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherWeightDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherWhoStageAtPmtctStartDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiPrimaryCareGiverDataDefinition;
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
		return Arrays.asList(ReportUtils.map(heiDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}")
		//	ReportUtils.map(hivDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		// ReportUtils.map(hivAndTBDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		// ReportUtils.map(tbDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		        );
	}
	
	protected DataSetDefinition hivDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedHIVPatients");
		dsd.setDescription("Deceased HIV Patients");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		//Add columns here
		
		DeceasedHivPatientCohortDefinition cd = new DeceasedHivPatientCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
	protected DataSetDefinition hivAndTBDataSetDefinitionColumns() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("DeceasedHIVTBPatients");
		dsd.setDescription("Deceased HIV Patients with TB");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		String paramMapping = "startDate=${startDate},endDate=${endDate}";
		
		//Add columns here
		
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
		
		AgeAtDeathDataDefinition ageAtDeathDataDefinition = new AgeAtDeathDataDefinition();
		ageAtDeathDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
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
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("HEI Number", new HEIIdDataDefinition(), "");
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Date of Death", new HeiDateOfDeathDataDefinition(), "");
		dsd.addColumn("Age at Death", ageAtDeathDataDefinition, "endDate=${endDate}");
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
		//dsd.addColumn("Recent VL Results", new HeiMotherMuacDataDefinition(), "");
		
		DeceasedHEICohortDefinition cd = new DeceasedHEICohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
}
