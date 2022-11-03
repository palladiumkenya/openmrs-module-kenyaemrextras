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

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
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
		        + "      from(\n"
		        + "      select fup.patient_id,d.patient_id as disc_patient,d.visit_date as disc_date,\n"
		        + "                         mid(max(concat(fup.visit_date,fup.next_appointment_date)),11) as latest_next_appointment_date\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "      join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "      join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "      left outer JOIN\n"
		        + "                (select patient_id, coalesce(max(date(effective_discontinuation_date)),max(date(visit_date))) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                group by patient_id\n"
		        + "                ) d on d.patient_id = fup.patient_id\n"
		        + "       where fup.visit_date <= date(:endDate) and fup.next_appointment_date between date(:startDate) AND date(:endDate)\n"
		        + "                  group by patient_id\n"
		        + "          having latest_next_appointment_date between date(:startDate) AND date(:endDate) AND\n"
		        + "                 (max(e.visit_date) >= date(disc_date) or disc_patient is null or disc_date >= date(:endDate))\n"
		        + "      ) t;";
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
	public CohortDefinition missedAppointments() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n" + "where a.days_missed > 0;";
		cd.setName("missedAppointments");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients with missed appointments within period");
		
		return cd;
	}
	
	/**
	 * SQL for RTC clients within 7 days of missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcWithin7Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n"
		        + "where timestampdiff(DAY,next_appointment_date,return_date) between 1 and 7;";
		cd.setName("rtcWithin7DaysSql");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("RTC clients within 7 days of missed appointment");
		
		return cd;
	}
	
	/**
	 * SQL for clients who RTC after between 8 and 30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcBetween8And30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n"
		        + "where timestampdiff(DAY,next_appointment_date,return_date) between 8 and 30;";
		cd.setName("rtcBetween8And30DaysSql");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by 8-30 days");
		
		return cd;
	}
	
	/**
	 * Returns number of Clients who are LTFU within the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition iitOver30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n" + "where a.days_missed > 30;";
		cd.setName("iitOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients missed appointment by > 30 days");
		
		return cd;
	}
	
	/**
	 * SQL for clients who RTC >30 days after missed appointment
	 * 
	 * @return
	 */
	public CohortDefinition rtcOver30Days() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n"
		        + "where timestampdiff(DAY,next_appointment_date,return_date) > 30;";
		cd.setName("rtcOver30Days");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of RTC clients after missed appointment by > 30 days");
		
		return cd;
	}
	
	/**
	 * /** Returns number of Clients who missed an appointment by > 30 days and never returned to
	 * care
	 * 
	 * @return
	 */
	public CohortDefinition currentIIT() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.app_visit,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date as next_appointment_date,\n"
		        + "             d.patient_id as disc_patient,\n"
		        + "             d.visit_date as disc_date,\n"
		        + "             if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,current_date),\n"
		        + "                if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                    ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "      from (\n"
		        + "-- Returned after next appointment date\n"
		        + "               select f4.patient_id      as patient_id,\n"
		        + "                      max(f4.visit_date)      as app_visit,\n"
		        + "                      max(f5.visit_date) as return_date,\n"
		        + "                      mid(max(concat(f4.visit_date,f4.next_appointment_date)),11) as next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                        left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "                                  on f4.patient_id = f5.patient_id\n"
		        + "               where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               group by f5.patient_id\n"
		        + "               having next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "               union\n"
		        + "-- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f7.latest_appointment\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f7.return_date = f0.visit_date\n"
		        + "               group by f7.patient_id\n"
		        + "               having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "           ) r\n"
		        + "               inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "               left outer join (select patient_id,\n"
		        + "                                       coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                max(date(visit_date)))              visit_date,\n"
		        + "                                       max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                       discontinuation_reason\n"
		        + "                                from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                where date(visit_date) <= date(:endDate)\n"
		        + "                                  and program_name = 'HIV'\n"
		        + "                                group by patient_id) d on d.patient_id = r.patient_id\n"
		        + "      group by r.patient_id\n"
		        + "      having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "              date(d.visit_date) >= date(:endDate))) a\n"
		        + "where a.days_missed > 0 and app_visit = return_date;";
		cd.setName("currentIIT");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients experiencing IIT");
		
		return cd;
	}
}
