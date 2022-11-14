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
		String sqlQuery = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + " f4.visit_date      as app_visit,\n"
		        + " min(f5.visit_date) as return_date,\n"
		        + " f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "                  coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "                  max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "                  discontinuation_reason\n"
		        + "           from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "           where date(visit_date) <= date(:endDate)\n"
		        + "             and program_name = 'HIV'\n"
		        + "           group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "   left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "              from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "             on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + " f2.visit_date       as app_visit,\n"
		        + " min(f23.visit_date) as return_date,\n"
		        + " f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "   left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "              from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "             on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + " f0.visit_date  as app_visit,\n"
		        + " f7.return_date as return_date,\n"
		        + " f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "   left join (select f7.patient_id,\n"
		        + "                     f7.visit_date,\n"
		        + "                     f7.next_appointment_date,\n"
		        + "                     max(f7.visit_date)                                            as return_date,\n"
		        + "                     mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "              from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "              group by f7.patient_id) f7\n"
		        + "             on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + " f2.visit_date       as app_visit,\n"
		        + " max(f24.visit_date) as return_date,\n"
		        + " f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "   left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "              from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "             on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + " fa.visit_date     as app_visit,\n"
		        + " fa.next_appointment_date,\n"
		        + " min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "   left join\n"
		        + "(select f5.patient_id,\n"
		        + "       f5.visit_date,\n"
		        + "       f5.next_appointment_date,\n"
		        + "       f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "         left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "                    from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "                   on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "    inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "    left outer join (select patient_id,\n"
		        + "     coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "     max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "     discontinuation_reason\n"
		        + "         from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + "        ) d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) > 0 or (next_appointment_date > return_date and return_date = app_visit)\n"
		        + "    group by r.patient_id\n"
		        + "        having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
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
		String sqlQuery = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + "f4.visit_date      as app_visit,\n"
		        + "min(f5.visit_date) as return_date,\n"
		        + "f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "min(f23.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + "f0.visit_date  as app_visit,\n"
		        + "f7.return_date as return_date,\n"
		        + "f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "left join (select f7.patient_id,\n"
		        + "f7.visit_date,\n"
		        + "f7.next_appointment_date,\n"
		        + "max(f7.visit_date)                                            as return_date,\n"
		        + "mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "group by f7.patient_id) f7\n"
		        + "on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "max(f24.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + "fa.visit_date     as app_visit,\n"
		        + "fa.next_appointment_date,\n"
		        + "min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "left join\n"
		        + "(select f5.patient_id,\n"
		        + "f5.visit_date,\n"
		        + "f5.next_appointment_date,\n"
		        + "f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "left outer join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) between 1 and 7\n"
		        + "group by r.patient_id\n"
		        + "having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
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
		String sqlQuery = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + "f4.visit_date      as app_visit,\n"
		        + "min(f5.visit_date) as return_date,\n"
		        + "f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "min(f23.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + "f0.visit_date  as app_visit,\n"
		        + "f7.return_date as return_date,\n"
		        + "f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "left join (select f7.patient_id,\n"
		        + "f7.visit_date,\n"
		        + "f7.next_appointment_date,\n"
		        + "max(f7.visit_date)                                            as return_date,\n"
		        + "mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "group by f7.patient_id) f7\n"
		        + "on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "max(f24.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + "fa.visit_date     as app_visit,\n"
		        + "fa.next_appointment_date,\n"
		        + "min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "left join\n"
		        + "(select f5.patient_id,\n"
		        + "f5.visit_date,\n"
		        + "f5.next_appointment_date,\n"
		        + "f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "left outer join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) between 8 and 30\n"
		        + "group by r.patient_id\n"
		        + "having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
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
		String sqlQuery = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + "f4.visit_date      as app_visit,\n"
		        + "min(f5.visit_date) as return_date,\n"
		        + "f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "min(f23.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + "f0.visit_date  as app_visit,\n"
		        + "f7.return_date as return_date,\n"
		        + "f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "left join (select f7.patient_id,\n"
		        + "f7.visit_date,\n"
		        + "f7.next_appointment_date,\n"
		        + "max(f7.visit_date)                                            as return_date,\n"
		        + "mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "group by f7.patient_id) f7\n"
		        + "on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "max(f24.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + "fa.visit_date     as app_visit,\n"
		        + "fa.next_appointment_date,\n"
		        + "min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "left join\n"
		        + "(select f5.patient_id,\n"
		        + "f5.visit_date,\n"
		        + "f5.next_appointment_date,\n"
		        + "f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "left outer join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) > 30 or (next_appointment_date > return_date and return_date = app_visit)\n"
		        + "group by r.patient_id\n"
		        + "having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
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
		String sqlQuery = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + "f4.visit_date      as app_visit,\n"
		        + "min(f5.visit_date) as return_date,\n"
		        + "f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "min(f23.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + "f0.visit_date  as app_visit,\n"
		        + "f7.return_date as return_date,\n"
		        + "f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "left join (select f7.patient_id,\n"
		        + "f7.visit_date,\n"
		        + "f7.next_appointment_date,\n"
		        + "max(f7.visit_date)                                            as return_date,\n"
		        + "mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "group by f7.patient_id) f7\n"
		        + "on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "max(f24.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + "fa.visit_date     as app_visit,\n"
		        + "fa.next_appointment_date,\n"
		        + "min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "left join\n"
		        + "(select f5.patient_id,\n"
		        + "f5.visit_date,\n"
		        + "f5.next_appointment_date,\n"
		        + "f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "left outer join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) > 30\n"
		        + "group by r.patient_id\n"
		        + "having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
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
		        + "        from (select r.patient_id,\n"
		        + "                   r.return_date,\n"
		        + "                   r.next_appointment_date,\n"
		        + "                   r.app_visit,\n"
		        + "                   d.disc_patient as disc_patient,\n"
		        + "                   d.disc_date\n"
		        + "            from (\n"
		        + "                     -- Never Returned\n"
		        + "                     select f0.patient_id  as patient_id,\n"
		        + "                                                    f0.visit_date  as app_visit,\n"
		        + "                                                    f7.return_date as return_date,\n"
		        + "                                                    f0.next_appointment_date\n"
		        + "                                             from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                                                      inner join kenyaemr_etl.etl_hiv_enrollment e on e.patient_id = f0.patient_id\n"
		        + "                                                      left join (select f7.patient_id,\n"
		        + "                                                                        f7.visit_date,\n"
		        + "                                                                        f7.next_appointment_date,\n"
		        + "                                                                        max(f7.visit_date)                                            as return_date,\n"
		        + "                                                                        mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                                                 from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                                                 group by f7.patient_id) f7\n"
		        + "                                                                on f0.patient_id = f7.patient_id\n"
		        + "                                             where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                                               and f0.next_appointment_date = latest_appointment\n"
		        + "                                               and return_date = f0.visit_date)r\n"
		        + "                                                              left outer JOIN\n"
		        + "                                         (select patient_id                                as                               disc_patient,\n"
		        + "                                                 coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "                                                 max(date(effective_discontinuation_date)) as                               disc_date,\n"
		        + "                                                 discontinuation_reason\n"
		        + "                                          from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                          where date(visit_date) <= date(:endDate)             and program_name = 'HIV'\n"
		        + "                                          group by patient_id) d on d.disc_patient = r.patient_id\n"
		        + "                                    where (return_date > date(disc_date) or disc_patient is null or disc_date >= date(:endDate))\n"
		        + "                                        and (next_appointment_date > return_date and return_date = app_visit))a;";
		cd.setName("currentIIT");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("Total number of clients experiencing IIT");
		
		return cd;
	}
}
