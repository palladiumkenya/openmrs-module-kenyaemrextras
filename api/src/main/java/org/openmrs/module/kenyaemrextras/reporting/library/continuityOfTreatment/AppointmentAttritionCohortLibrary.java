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
	
	/**
	 * Total number of appointments within period
	 * 
	 * @return
	 */
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
	
	/**
	 * Total number of clients with missed appointments within period
	 * 
	 * @return
	 */
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
		cd.setCompositionString("totalAppointments AND missedAppointmentsSql");
		return cd;
	}
	
	/**
	 * SQL for RTC clients within 7 days of missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcWithin7DaysSql() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "    greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "    timestampdiff(DAY,greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')),date(:endDate)) as days_missed,\n"
		        + "    d.patient_id as disc_patient,\n"
		        + "    d.effective_disc_date as effective_disc_date,\n"
		        + "    max(d.visit_date) as date_discontinued,\n"
		        + "    d.discontinuation_reason,\n"
		        + "    de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "    join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "    join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "    left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "    left outer JOIN\n"
		        + "    (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "    where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "    group by patient_id\n"
		        + "    ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having (\n"
		        + "    (timestampdiff(DAY,date(latest_tca),date(:endDate)) between 1 and 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "    and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + "    )) t\n"
		        + "inner join(select a.patient_id, a.first_visit_after_missed_app\n"
		        + "           from (select f.patient_id,\n"
		        + "                        least(f.first_visit_after_missed_app, ifnull(d.eff_disc_after_end_date,f.first_visit_after_missed_app)) as first_visit_after_missed_app\n"
		        + "                 from (select f.patient_id, min(date(f.visit_date)) as first_visit_after_missed_app\n"
		        + "                       from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                       where date(f.visit_date) > date(:endDate)\n"
		        + "                       group by f.patient_id) f\n"
		        + "                          left join\n"
		        + "                      (select d.patient_id,\n"
		        + "                              date(d.visit_date),\n"
		        + "                              min(date(d.visit_date))                                                          as disc_after_end_date,\n"
		        + "                              mid(min(concat(date(d.visit_date), date(d.effective_discontinuation_date))),11)                                                                          as eff_disc_after_end_date\n"
		        + "                       from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                       where date(d.visit_date) > date(:endDate)\n"
		        + "                         and d.program_name = 'HIV'\n"
		        + "                       group by d.patient_id\n"
		        + "                       having cast(eff_disc_after_end_date AS CHAR CHARACTER SET latin1) > disc_after_end_date) d\n"
		        + "                      on f.patient_id = d.patient_id)a)v on t.patient_id = v.patient_id where timestampdiff(DAY,t.latest_tca,first_visit_after_missed_app) < 7\n"
		        + "group by t.patient_id;";
		cd.setName("rtcWithinNDaysSql");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("RTC clients within n days of missed appointment");
		
		return cd;
	}
	
	/**
	 * Returns booked clients who RTC within 7 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcWithin7Days() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("rtcWithin7DaysSql", ReportUtils.map(rtcWithin7DaysSql(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("totalAppointments", ReportUtils.map(totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("totalAppointments AND rtcWithin7DaysSql");
		return cd;
	}
	
	/**
	 * SQL for clients who RTC after between 8 and 30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcBetween8And30DaysSql() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "    greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "    timestampdiff(DAY,greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')),date(:endDate)) as days_missed,\n"
		        + "    d.patient_id as disc_patient,\n"
		        + "    d.effective_disc_date as effective_disc_date,\n"
		        + "    max(d.visit_date) as date_discontinued,\n"
		        + "    d.discontinuation_reason,\n"
		        + "    de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "    join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "    join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "    left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "    left outer JOIN\n"
		        + "    (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "    where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "    group by patient_id\n"
		        + "    ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having (\n"
		        + "    (timestampdiff(DAY,date(latest_tca),date(:endDate)) between 1 and 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "    and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + "    )) t\n"
		        + "inner join(select a.patient_id, a.first_visit_after_missed_app\n"
		        + "           from (select f.patient_id,\n"
		        + "                        least(f.first_visit_after_missed_app, ifnull(d.eff_disc_after_end_date,f.first_visit_after_missed_app)) as first_visit_after_missed_app\n"
		        + "                 from (select f.patient_id, min(date(f.visit_date)) as first_visit_after_missed_app\n"
		        + "                       from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                       where date(f.visit_date) > date(:endDate)\n"
		        + "                       group by f.patient_id) f\n"
		        + "                          left join\n"
		        + "                      (select d.patient_id,\n"
		        + "                              date(d.visit_date),\n"
		        + "                              min(date(d.visit_date))                                                          as disc_after_end_date,\n"
		        + "                              mid(min(concat(date(d.visit_date), date(d.effective_discontinuation_date))),11)                                                                          as eff_disc_after_end_date\n"
		        + "                       from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                       where date(d.visit_date) > date(:endDate)\n"
		        + "                         and d.program_name = 'HIV'\n"
		        + "                       group by d.patient_id\n"
		        + "                       having cast(eff_disc_after_end_date AS CHAR CHARACTER SET latin1) > disc_after_end_date) d\n"
		        + "                      on f.patient_id = d.patient_id)a)v on t.patient_id = v.patient_id where timestampdiff(DAY,t.latest_tca,first_visit_after_missed_app) between 8 and 30\n"
		        + "group by t.patient_id;";
		cd.setName("rtcBetween8And30DaysSql");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by 8-30 days");
		
		return cd;
	}
	
	/**
	 * Returns booked clients who RTC after between 8 and 30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcBetween8And30Days() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("rtcBetween8And30DaysSql",
		    ReportUtils.map(rtcBetween8And30DaysSql(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("totalAppointments", ReportUtils.map(totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("totalAppointments AND rtcBetween8And30DaysSql");
		return cd;
	}
	
	/**
	 * Returns number of Clients who are LTFU withing the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition iitOver30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from (\n"
		        + "select fup.visit_date,\n"
		        + "    date(d.visit_date),\n"
		        + "    fup.patient_id,\n"
		        + "    max(e.visit_date)                                               as enroll_date,\n"
		        + "    greatest(max(e.visit_date),\n"
		        + "             ifnull(max(date(e.transfer_in_date)), '0000-00-00'))   as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date),\n"
		        + "             ifnull(max(d.visit_date), '0000-00-00'))               as latest_vis_date,\n"
		        + "    max(fup.visit_date)                                             as max_fup_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),\n"
		        + "             ifnull(max(d.visit_date), '0000-00-00'))               as latest_tca, timestampdiff(DAY, date(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11)), date(:endDate)) 'DAYS MISSED',\n"
		        + "    mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11) as latest_fup_tca,\n"
		        + "    d.patient_id                                                    as disc_patient,\n"
		        + "    d.effective_disc_date                                           as effective_disc_date,\n"
		        + "    d.visit_date                                                    as date_discontinued,\n"
		        + "    d.discontinuation_reason,\n"
		        + "    de.patient_id                                                   as started_on_drugs\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "      join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id\n"
		        + "      join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id = e.patient_id\n"
		        + "      left outer join kenyaemr_etl.etl_drug_event de\n"
		        + "                      on e.patient_id = de.patient_id and de.program = 'HIV' and\n"
		        + "                         date(date_started) <= date(curdate())\n"
		        + "      left outer JOIN\n"
		        + "  (select patient_id,\n"
		        + "          coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) as visit_date,\n"
		        + "          max(date(effective_discontinuation_date))                                  as effective_disc_date,\n"
		        + "          discontinuation_reason\n" + "   from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "   where date(visit_date) <= date(:endDate)\n" + "     and program_name = 'HIV'\n"
		        + "   group by patient_id\n" + "  ) d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate)\n" + "group by patient_id\n" + "having (\n"
		        + "            (timestampdiff(DAY, date(latest_fup_tca), date(:startDate)) <= 30) and\n"
		        + "            (timestampdiff(DAY, date(latest_fup_tca), date(:endDate)) > 30) and\n" + "            (\n"
		        + "                    (date(enroll_date) >= date(d.visit_date) and\n"
		        + "                     date(max_fup_vis_date) >= date(d.visit_date) and\n"
		        + "                     date(latest_fup_tca) > date(d.visit_date))\n"
		        + "                    or disc_patient is null\n"
		        + "                    or (date(d.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "                    and d.discontinuation_reason = 5240))\n" + "        )\n" + ") t;";
		cd.setName("iitOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients missed appointment by > 30 days");
		
		return cd;
	}
	
	/**
	 * SQL for clients who RTC >30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcOver30DaysSql() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "    greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "    timestampdiff(DAY,greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')),date(:endDate)) as days_missed,\n"
		        + "    d.patient_id as disc_patient,\n"
		        + "    d.effective_disc_date as effective_disc_date,\n"
		        + "    max(d.visit_date) as date_discontinued,\n"
		        + "    d.discontinuation_reason,\n"
		        + "    de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "    join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "    join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "    left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "    left outer JOIN\n"
		        + "    (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "    where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "    group by patient_id\n"
		        + "    ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having (\n"
		        + "    (timestampdiff(DAY,date(latest_tca),date(:endDate)) between 1 and 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "    and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + "    )) t\n"
		        + "inner join(select a.patient_id, a.first_visit_after_missed_app\n"
		        + "           from (select f.patient_id,\n"
		        + "                        least(f.first_visit_after_missed_app, ifnull(d.eff_disc_after_end_date,f.first_visit_after_missed_app)) as first_visit_after_missed_app\n"
		        + "                 from (select f.patient_id, min(date(f.visit_date)) as first_visit_after_missed_app\n"
		        + "                       from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                       where date(f.visit_date) > date(:endDate)\n"
		        + "                       group by f.patient_id) f\n"
		        + "                          left join\n"
		        + "                      (select d.patient_id,\n"
		        + "                              date(d.visit_date),\n"
		        + "                              min(date(d.visit_date))                                                          as disc_after_end_date,\n"
		        + "                              mid(min(concat(date(d.visit_date), date(d.effective_discontinuation_date))),11)                                                                          as eff_disc_after_end_date\n"
		        + "                       from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                       where date(d.visit_date) > date(:endDate)\n"
		        + "                         and d.program_name = 'HIV'\n"
		        + "                       group by d.patient_id\n"
		        + "                       having cast(eff_disc_after_end_date AS CHAR CHARACTER SET latin1) > disc_after_end_date) d\n"
		        + "                      on f.patient_id = d.patient_id)a)v on t.patient_id = v.patient_id where timestampdiff(DAY,t.latest_tca,first_visit_after_missed_app) > 30\n"
		        + "group by t.patient_id;";
		cd.setName("rtcOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by > 30 days");
		
		return cd;
	}
	
	/**
	 * Returns booked clients who RTC >30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcOver30Days() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("rtcOver30DaysSql", ReportUtils.map(rtcOver30DaysSql(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("totalAppointments", ReportUtils.map(totalAppointments(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("totalAppointments AND rtcOver30DaysSql");
		return cd;
	}
	
	/**
	 * Returns number of Clients who are LTFU and yet to RTC as of reporting date
	 * 
	 * @return
	 */
	public CohortDefinition currentIIT() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from (\n"
		        + "select fup.visit_date,\n"
		        + "    date(d.visit_date),\n"
		        + "    fup.patient_id,\n"
		        + "    max(e.visit_date)                                               as enroll_date,\n"
		        + "    greatest(max(e.visit_date),\n"
		        + "             ifnull(max(date(e.transfer_in_date)), '0000-00-00'))   as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date),\n"
		        + "             ifnull(max(d.visit_date), '0000-00-00'))               as latest_vis_date,\n"
		        + "    max(fup.visit_date)                                             as max_fup_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),\n"
		        + "             ifnull(max(d.visit_date), '0000-00-00'))               as latest_tca, timestampdiff(DAY, date(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11)), date(CURRENT_DATE)) 'DAYS MISSED',\n"
		        + "    mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11) as latest_fup_tca,\n"
		        + "    d.patient_id                                                    as disc_patient,\n"
		        + "    d.effective_disc_date                                           as effective_disc_date,\n"
		        + "    d.visit_date                                                    as date_discontinued,\n"
		        + "    d.discontinuation_reason,\n"
		        + "    de.patient_id                                                   as started_on_drugs\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "      join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id\n"
		        + "      join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id = e.patient_id\n"
		        + "      left outer join kenyaemr_etl.etl_drug_event de\n"
		        + "                      on e.patient_id = de.patient_id and de.program = 'HIV' and\n"
		        + "                         date(date_started) <= date(:endDate)\n"
		        + "      left outer JOIN\n"
		        + "  (select patient_id,\n"
		        + "          coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) as visit_date,\n"
		        + "          max(date(effective_discontinuation_date))                                  as effective_disc_date,\n"
		        + "          discontinuation_reason\n" + "   from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "   where date(visit_date) <= date(:endDate)\n" + "     and program_name = 'HIV'\n"
		        + "   group by patient_id\n" + "  ) d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate)\n" + "group by patient_id\n" + "having (\n"
		        + "            (timestampdiff(DAY, date(latest_fup_tca), date(:endDate)) > 30) and\n" + "            (\n"
		        + "                    (date(enroll_date) >= date(d.visit_date) and\n"
		        + "                     date(max_fup_vis_date) >= date(d.visit_date) and\n"
		        + "                     date(latest_fup_tca) > date(d.visit_date))\n"
		        + "                    or disc_patient is null\n"
		        + "                    or (date(d.visit_date) < date(:endDate)\n"
		        + "                    and d.discontinuation_reason = 5240))\n" + "        )\n" + ") t;";
		cd.setName("currentIIT");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients experiencing IIT");
		
		return cd;
	}
}
