/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.library.continuityOfTreatment;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.KPTypeDataDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by dev on 1/10/18.
 */

/**
 * Library of cohort definitions for Continuity of treatment tool
 */
@Component
public class AppointmentAttritionCohortLibrary {
	
	public CohortDefinition totalAppointments() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "        select fup.visit_date,fup.patient_id,d.patient_id as disc_patient,d.visit_date as disc_date,\n"
		        + "               fup.next_appointment_date\n"
		        + "        from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "                 join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "                 join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "                 left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(curdate())\n"
		        + "                 left outer JOIN\n"
		        + "             (select patient_id, coalesce(max(date(effective_discontinuation_date)),max(date(visit_date))) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "              group by patient_id\n"
		        + "             ) d on d.patient_id = fup.patient_id\n"
		        + "        where fup.visit_date <= date(:endDate) and fup.next_appointment_date between date(:startDate) AND date(:endDate)\n"
		        + "        group by patient_id\n"
		        + "        having (max(e.visit_date) >= date(disc_date) or disc_patient is null or disc_date >= date(:endDate))\n"
		        + "    ) t;";
		cd.setName("totalAppointments");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients with appointments within period");
		
		return cd;
	}
	
	public CohortDefinition missedAppointmentsSql() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "d.patient_id as disc_patient,\n"
		        + "d.effective_disc_date as effective_disc_date,\n"
		        + "max(d.visit_date) as date_discontinued,\n"
		        + "d.discontinuation_reason,\n"
		        + "de.patient_id as started_on_drugs\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate)\n"
		        + "group by patient_id\n"
		        + "having (\n"
		        + "(timestampdiff(DAY,date(latest_tca),date(:endDate)) between 1 and 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + ")\n" + ") t;";
		cd.setName("missedAppointments");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients with missed appointments within period");
		
		return cd;
	}
	
	public CohortDefinition missedAppointments() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("totalAppointments", ReportUtils.map(totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("missedAppointmentsSql",
		    ReportUtils.map(missedAppointmentsSql(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txML AND missedAppointmentsSql");
		return cd;
	}
	
	public CohortDefinition rtcWithin7Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "";
		cd.setName("rtcWithin7Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients within 7 days of missed appointment");
		
		return cd;
	}
	
	public CohortDefinition rtcBetween8And30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "";
		cd.setName("rtcWithin30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by 8-30 days");
		
		return cd;
	}
	
	public CohortDefinition iitOver30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "";
		cd.setName("iitOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients missed appointment by > 30 days");
		
		return cd;
	}
	
	public CohortDefinition rtcOver30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "";
		cd.setName("rtcOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by > 30 days");
		
		return cd;
	}
	
	public CohortDefinition currentIIT() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "";
		cd.setName("currentIIT");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients experiencing IIT");
		
		return cd;
	}
}
