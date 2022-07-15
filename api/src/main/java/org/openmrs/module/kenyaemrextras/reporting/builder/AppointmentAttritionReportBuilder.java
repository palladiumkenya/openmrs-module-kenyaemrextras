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
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.ANCRegisterCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.*;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.anc.*;
import org.openmrs.module.kenyaemr.reporting.library.pmtct.ANCIndicatorLibrary;
import org.openmrs.module.kenyaemrextras.reporting.library.continuityOfTreatment.AppointmentAttritionIndicatorLibrary;
import org.openmrs.module.metadatadeploy.MetadataUtils;
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
		return Arrays.asList(ReportUtils.map(appointmentAndAttrition(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	protected DataSetDefinition appointmentAndAttrition() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.setName("AppointmentsAndAttritionAndRTC");
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		cohortDsd.addColumn("Total_Appointments", "Total Number of Appointments",
		    ReportUtils.map(cot.totalAppointments(), indParams), "");
		cohortDsd.addColumn("Missed_Appointments", "Total number of Missed Appointments",
		    ReportUtils.map(cot.missedAppointments(), indParams), "");
		cohortDsd.addColumn("RTC_Within_7_Days", "Returned to Care within 7 days",
		    ReportUtils.map(cot.rtcWithin7Days(), indParams), "");
		cohortDsd.addColumn("RTC_After_8_TO_30_Days", "Returned to Care after 8 to 30 days of missed appointment",
		    ReportUtils.map(cot.rtcBetween8And30Days(), indParams), "");
		cohortDsd.addColumn("IIT_Within_Reporting_period",
		    "Missed appointment for more than 30 days and not yet returned to care as of end date",
		    ReportUtils.map(cot.iitOver30Days(), indParams), "");
		cohortDsd.addColumn("RTC_Over_30_Days_From_Missed_App",
		    "Returned to care after more than 30 days since their interruption",
		    ReportUtils.map(cot.rtcOver30Days(), indParams), "");
		cohortDsd.addColumn("ALL_IIT", "All Current IIT", ReportUtils.map(cot.currentIIT(), indParams), "");
		return cohortDsd;
	}
	
}
