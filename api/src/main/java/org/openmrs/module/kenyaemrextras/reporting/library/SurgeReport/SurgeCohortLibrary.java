/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.library.SurgeReport;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by dev on 1/10/18.
 */

/**
 * Library of cohort definitions used specifically in Datim Reports
 */
@Component
public class SurgeCohortLibrary {
	
	/**
	 * Patients currently on ART TX_Curr Datim indicator
	 * 
	 * @return
	 */
	public CohortDefinition currentOnArt() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		
		String sqlQuery = "select e.patient_id \n"
		        + "from (\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "           max(d.visit_date) as date_discontinued,\n"
		        + "           d.patient_id as disc_patient,\n"
		        + "           de.patient_id as started_on_drugs,\n"
		        + "           de.program as hiv_program,\n"
		        + "       d.effective_disc_date as effective_disc_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and date(date_started) <= date(:endDate)\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where de.program = 'HIV' and fup.visit_date <= date(:endDate)\n"
		        + "group by patient_id\n"
		        + "having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "    (\n"
		        + "        ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and (date(d.effective_disc_date) > date(:endDate) or d.effective_disc_date is null))\n"
		        + "          and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "        )\n" + "    )) e;";
		
		cd.setName("TX_CURR");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("currently on ART");
		return cd;
	}
	
	/**
	 * returns a list of those who started drugs during a period
	 * 
	 * @return
	 */
	public CohortDefinition newOnArt() {
		
		String sqlQuery = "select net.patient_id   \n"
		        + "                 from (   \n"
		        + "                 select e.patient_id,e.date_started,   \n"
		        + "                 e.gender,  \n"
		        + "                 e.dob,  \n"
		        + "                 d.visit_date as dis_date,   \n"
		        + "                 if(d.visit_date is not null, 1, 0) as TOut,  \n"
		        + "                 e.regimen, e.regimen_line, e.alternative_regimen,   \n"
		        + "                 mid(max(concat(fup.visit_date,fup.next_appointment_date)),11) as latest_tca,   \n"
		        + "                 max(if(enr.date_started_art_at_transferring_facility is not null and enr.facility_transferred_from is not null, 1, 0)) as TI_on_art,  \n"
		        + "                 max(if(enr.transfer_in_date is not null, 1, 0)) as TIn,   \n"
		        + "                 max(fup.visit_date) as latest_vis_date  \n"
		        + "                 from (select e.patient_id,p.dob,p.Gender,min(e.date_started) as date_started,   \n"
		        + "                 mid(min(concat(e.date_started,e.regimen_name)),11) as regimen,   \n"
		        + "                 mid(min(concat(e.date_started,e.regimen_line)),11) as regimen_line,   \n"
		        + "                 max(if(discontinued,1,0))as alternative_regimen   \n"
		        + "                 from kenyaemr_etl.etl_drug_event e \n"
		        + "                 join kenyaemr_etl.etl_patient_demographics p on p.patient_id=e.patient_id \n"
		        + "                 where e.program = 'HIV' \n"
		        + "                 group by e.patient_id) e   \n"
		        + "                 left outer join kenyaemr_etl.etl_patient_program_discontinuation d on d.patient_id=e.patient_id and d.program_name='HIV'  \n"
		        + "                 left outer join kenyaemr_etl.etl_hiv_enrollment enr on enr.patient_id=e.patient_id   \n"
		        + "                 left outer join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id=e.patient_id   \n"
		        + "                 where date(e.date_started) between :startDate and :endDate \n"
		        + "                 group by e.patient_id   \n" + "                 having TI_on_art=0  \n"
		        + "                 )net;";
		
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("TX_New");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Newly Started ART");
		return cd;
		
	}
	
	/**
	 * returns a list of those who turned LTFU during the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition ltfuRecent() {
		
		String sqlQuery = " select  e.patient_id\n"
		        + "from (\n"
		        + "select fup.visit_date,fup.patient_id, min(e.visit_date) as enroll_date,\n"
		        + " max(fup.visit_date) as latest_vis_date,\n"
		        + " mid(max(concat(fup.visit_date,fup.next_appointment_date)),11) as latest_tca,\n"
		        + " max(d.visit_date) as date_discontinued,\n"
		        + " d.patient_id as disc_patient\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "-- ensure those discontinued are catered for\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, visit_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= :endDate  and program_name='HIV'\n"
		        + "group by patient_id -- check if this line is necessary\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= :endDate\n"
		        + "group by patient_id\n"
		        + "--  we may need to filter lost to follow-up using this\n"
		        + "having (\n"
		        + "(((date(latest_tca) < :endDate) and (date(latest_vis_date) < date(latest_tca))) ) and ((date(latest_tca) > date(date_discontinued) and date(latest_vis_date) > date(date_discontinued)) or disc_patient is null ) and datediff(:endDate, date(latest_tca)) between 31 and 37)\n"
		        + "-- drop missd completely\n" + ") e;";
		
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("LTFU_Recent");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Patients who turned LTFU during the reporting period");
		return cd;
		
	}
	
	/**
	 * returns a list of those who turned LTFU during the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition ltfuRTC() {
		
		String sqlQuery = " select  e.patient_id\n"
		        + "\tfrom (\n"
		        + "\tselect fup.visit_date,fup.patient_id, min(e.visit_date) as enroll_date,\n"
		        + "\t max(fup.visit_date) as latest_vis_date,\n"
		        + "\t mid(max(concat(fup.visit_date,fup.next_appointment_date)),11) as latest_tca,\n"
		        + "\t max(d.visit_date) as date_discontinued,\n"
		        + "\t d.patient_id as disc_patient\n"
		        + "\tfrom kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "\tjoin kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "\tjoin kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "\t-- ensure those discontinued are catered for\n"
		        + "\tleft outer JOIN\n"
		        + "\t(select patient_id, visit_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "\twhere date(visit_date) <= date_sub(:startDate, INTERVAL 1 DAY)  and program_name='HIV'\n"
		        + "\tgroup by patient_id -- check if this line is necessary\n"
		        + "\t) d on d.patient_id = fup.patient_id\n"
		        + "\twhere fup.visit_date <= date_sub(:startDate, INTERVAL 1 DAY)\n"
		        + "\tgroup by patient_id\n"
		        + "\t--  we may need to filter lost to follow-up using this\n"
		        + "\thaving (\n"
		        + "\t(((date(latest_tca) < date_sub(:startDate, INTERVAL 1 DAY)) and (date(latest_vis_date) < date(latest_tca))) ) and ((date(latest_tca) > date(date_discontinued) and date(latest_vis_date) > date(date_discontinued)) or disc_patient is null ) and datediff(date_sub(:startDate, INTERVAL 1 DAY), date(latest_tca)) > 30)\n"
		        + "\t-- drop missd completely\n"
		        + "\t) e inner join kenyaemr_etl.etl_patient_hiv_followup r on r.patient_id=e.patient_id and date(r.visit_date) between date(:startDate) and date(:endDate); ";
		
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("LTFU_RTC");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Patients who were previously lost to followup and returned to care during the reporting week.");
		return cd;
		
	}
	
}
