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
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.data.converter.NationalIdentifiersTypeConverter;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.AutomatedNupiClientsCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.NupiVerificationDateDataDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
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
 * Clients who acquired NUPI number through automated scheduler Created by pwangoo on 23/11/22.
 */

@Component
@Builds({ "kenyaemrextras.common.report.automatedNupiClients" })
public class AutomatedNupiClientsReportBuilder extends AbstractHybridReportBuilder {
	
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
	
	protected Mapped<CohortDefinition> automatedNupiCohort() {
		CohortDefinition cd = new AutomatedNupiClientsCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setName("Automated Nupi Clients");
		return ReportUtils.map(cd, "startDate=${startDate},endDate=${endDate}");
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition report) {
		
		PatientDataSetDefinition allAutomatedNupi = automatedNupiDataSetDefinition("automatedNupi");
		allAutomatedNupi.addRowFilter(automatedNupiCohort());
		DataSetDefinition allAutomatedNupiDSD = allAutomatedNupi;
		
		return Arrays.asList(ReportUtils.map(allAutomatedNupiDSD, "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected PatientDataSetDefinition automatedNupiDataSetDefinition(String datasetName) {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(datasetName);
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		PatientIdentifierType nupi = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition nupiDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nupi.getName(), nupi), identifierFormatter);
		
		PatientIdentifierType passport = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.PASSPORT_NUMBER);
		DataDefinition passportDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        passport.getName(), passport), identifierFormatter);
		
		PatientIdentifierType nationalId = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.NATIONAL_ID);
		DataDefinition nationalIdDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        nationalId.getName(), nationalId), identifierFormatter);
		
		PatientIdentifierType birthCertificateNumber = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.BIRTH_CERTIFICATE_NUMBER);
		DataDefinition birthCertificateNumberDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(birthCertificateNumber.getName(), birthCertificateNumber),
		        identifierFormatter);
		
		PatientIdentifierType alienIdNumber = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.ALIEN_ID_NUMBER);
		DataDefinition alienIdNumberDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(alienIdNumber.getName(), alienIdNumber), identifierFormatter);
		
		PatientIdentifierType drivingLicense = MetadataUtils.existing(PatientIdentifierType.class,
		    CommonMetadata._PatientIdentifierType.DRIVING_LICENSE);
		DataDefinition drivingLicenceDef = new ConvertedPatientDataDefinition("identifier",
		        new PatientIdentifierDataDefinition(drivingLicense.getName(), drivingLicense), identifierFormatter);
		
		DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
		dsd.addColumn("id", new PersonIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
		dsd.addColumn("NUPI", nupiDef, "");
		dsd.addColumn("Verification date", new NupiVerificationDateDataDefinition(), "", new DateConverter(DATE_FORMAT));
		dsd.addColumn("CCC No", identifierDef, "");
		dsd.addColumn("National Id", nationalIdDef, "", new NationalIdentifiersTypeConverter());
		dsd.addColumn("Passport Number", passportDef, "", new NationalIdentifiersTypeConverter());
		dsd.addColumn("Birth Certificate Number", birthCertificateNumberDef, "", new NationalIdentifiersTypeConverter());
		dsd.addColumn("Driving License", drivingLicenceDef, "", new NationalIdentifiersTypeConverter());
		dsd.addColumn("Alien Id Number", alienIdNumberDef, "", new NationalIdentifiersTypeConverter());
		
		return dsd;
	}
}
