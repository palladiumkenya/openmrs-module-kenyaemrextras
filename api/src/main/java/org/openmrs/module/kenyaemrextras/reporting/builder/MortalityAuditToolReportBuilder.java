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

import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHEICohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivAndTBPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivPatientCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedTBPatientCohortDefinition;
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
		return Arrays.asList(ReportUtils.map(hivDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(hivAndTBDataSetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(tbDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"),
		    ReportUtils.map(heiDatasetDefinitionColumns(), "startDate=${startDate},endDate=${endDate}"));
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
		
		//Add columns here
		
		DeceasedHEICohortDefinition cd = new DeceasedHEICohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
	}
	
}
