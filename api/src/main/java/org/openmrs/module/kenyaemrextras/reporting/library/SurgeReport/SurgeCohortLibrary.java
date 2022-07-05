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
		        + "        ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
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
		
		// checks from the last sunday
		String sqlQuery = "select t.patient_id from ( select fup.visit_date, date(d.visit_date), fup.patient_id,"
		        + "  max(e.visit_date) as enroll_date, greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)), '0000-00-00')) "
		        + "  as latest_enrolment_date, greatest(max(fup.visit_date),  ifnull(max(d.visit_date), '0000-00-00')) as latest_vis_date,"
		        + "max(fup.visit_date)  as max_fup_vis_date, greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),"
		        + " ifnull(max(d.visit_date), '0000-00-00'))  as latest_tca, timestampdiff(DAY, date(mid(max(concat(fup.visit_date,"
		        + " fup.next_appointment_date)), 11)), date(:endDate)) 'DAYS MISSED',  mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11) "
		        + "as latest_fup_tca, d.patient_id as disc_patient, d.effective_disc_date as effective_disc_date, "
		        + " d.visit_date as date_discontinued,  d.discontinuation_reason,  de.patient_id as started_on_drugs from kenyaemr_etl.etl_patient_hiv_followup"
		        + " fup join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id  join kenyaemr_etl.etl_hiv_enrollment e "
		        + "on fup.patient_id = e.patient_id left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program = 'HIV' "
		        + "and date(date_started) <= date(curdate()) left outer JOIN (select patient_id,  coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) "
		        + "as visit_date, max(date(effective_discontinuation_date))   as effective_disc_date, discontinuation_reason "
		        + "from kenyaemr_etl.etl_patient_program_discontinuation where date(visit_date) <= date(:endDate) and program_name = 'HIV' group by patient_id ) "
		        + "d on d.patient_id = fup.patient_id where fup.visit_date <= date(:endDate)  group by patient_id  having ((timestampdiff(DAY, date(latest_fup_tca),"
		        + " date(date(:endDate) - INTERVAL WEEKDAY(date(:endDate)) % 7 + 1 DAY )) <= 30) and (timestampdiff(DAY, date(latest_fup_tca),"
		        + " date(:endDate)) between 31 and 38) and ((date(enroll_date) >= date(d.visit_date) and date(max_fup_vis_date) >= date(d.visit_date) "
		        + "and date(latest_fup_tca) > date(d.visit_date))  or disc_patient is null or (date(d.visit_date) between date(date(:endDate) - INTERVAL WEEKDAY(date(:endDate)) % 7 + 1 DAY ) "
		        + "and date(:endDate)and d.discontinuation_reason = 5240)) ) ) t;";
		
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("LTFU_Recent");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Patients who turned LTFU during the reporting period");
		return cd;
		
	}
	
	/**
	 * returns a list of those who returned to care but were previously LTFU during the reporting
	 * period
	 * 
	 * @return
	 */
	public CohortDefinition ltfuRTC() {
		
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
		        + "left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(curdate())\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:startDate)\n"
		        + "group by patient_id\n"
		        + "having ((\n"
		        + "(timestampdiff(DAY,date(latest_tca),date(:endDate)) > 30)\n"
		        + "and\n"
		        + "((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "and\n"
		        + "(date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null))\n"
		        + "or (date(d.effective_disc_date) < date(:startDate) and d.discontinuation_reason =5240))) t\n"
		        + "inner join kenyaemr_etl.etl_patient_hiv_followup r on r.patient_id=t.patient_id and date(r.visit_date) between date(:startDate) and date(:endDate);";
		
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("LTFU_RTC");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Patients who were previously lost to followup and returned to care during the reporting week.");
		return cd;
		
	}
	
}
