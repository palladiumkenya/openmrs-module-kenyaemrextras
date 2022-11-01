package org.openmrs.module.kenyaemrextras.reporting.library.sims;

public class SimsReportQueries {
	
	public static String missedAppointmentQuery() {
		String qry = "select t.patient_id\n"
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
		        + "where date(visit_date) <= date(curdate()) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(curdate())\n"
		        + "group by patient_id\n"
		        + "having (\n"
		        + "(timestampdiff(DAY,date(latest_tca),date(curdate())) between 1 and 30) and ((date(d.effective_disc_date) > date(curdate()) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + ")\n" + ") t limit 10;";
		return qry;
	}
	
	public static String newlyDiagnosedQuery() {
		String qry = "select e.patient_id\n"
		        + "from kenyaemr_etl.etl_hiv_enrollment e \n"
		        + "left join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = e.patient_id and fup.visit_date between date(:startDate) and date(:endDate)\n"
		        + "where date(e.date_confirmed_hiv_positive) between date(:startDate) and date(:endDate) ";
		return qry;
	}
	
	/*
	*Adult and adolescent patients on ART ≥ 12 months with virologic non-suppression.
	*
	* */
	public static String unSupressedVLQuery() {
		String qry = "select a.patient_id as patient_id\n"
		        + "                        from(select t.patient_id,vl.vl_date,vl.lab_test,vl.vl_result,vl.urgency from (\n"
		        + "                        select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "                               greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "                               greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "                               greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "                               d.patient_id as disc_patient,\n"
		        + "                               d.effective_disc_date as effective_disc_date,\n"
		        + "                               max(d.visit_date) as date_discontinued,\n"
		        + "                               de.patient_id as started_on_drugs,\n"
		        + "                               de.date_started,\n"
		        + "                               timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
		        + "                        from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "                               join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "                               join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "                               left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and de.date_started <= date_sub(date(:endDate) , interval 12 MONTH)\n"
		        + "                               left outer JOIN\n"
		        + "                                 (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                  where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                                  group by patient_id\n"
		        + "                                 ) d on d.patient_id = fup.patient_id\n"
		        + "                        where fup.visit_date <= date(:endDate)\n"
		        + "                        group by patient_id\n"
		        + "                        having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "                            (\n"
		        + "                                ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                                  and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15\n"
		        + "                                )\n"
		        + "                            ) order by date_started desc\n"
		        + "                        ) t\n"
		        + "                          inner join (\n"
		        + "                                     select\n"
		        + "                           b.patient_id,\n"
		        + "                           max(b.visit_date) as vl_date,\n"
		        + "                           date_sub(date(:endDate) , interval 12 MONTH),\n"
		        + "                           mid(max(concat(b.visit_date,b.lab_test)),11) as lab_test,\n"
		        + "                           if(mid(max(concat(b.visit_date,b.lab_test)),11) = 856, mid(max(concat(b.visit_date,b.test_result)),11), if(mid(max(concat(b.visit_date,b.lab_test)),11)=1305 and mid(max(concat(visit_date,test_result)),11) = 1302, \"LDL\",\"\")) as vl_result,\n"
		        + "                           mid(max(concat(b.visit_date,b.urgency)),11) as urgency\n"
		        + "                                           from (select x.patient_id as patient_id,x.visit_date as visit_date,x.lab_test as lab_test, x.test_result as test_result,urgency as urgency\n"
		        + "                                           from kenyaemr_etl.etl_laboratory_extract x where x.lab_test in (1305,856)\n"
		        + "                                           group by x.patient_id,x.visit_date order by visit_date desc)b\n"
		        + "                                     group by patient_id\n"
		        + "                                     having max(visit_date) between\n"
		        + "                        date_sub(date(:endDate) , interval 12 MONTH) and date(:endDate)\n"
		        + "                                     )vl\n"
		        + "                            on t.patient_id = vl.patient_id where vl_result >= 1000)a";
		return qry;
	}
	
	/*
	* Instructions: Review 10 register entries (individual or index/partner testing logbook) or charts (whichever source has the most updated information)
	* of HIV-positive adult and adolescent patients ≥15 years old on ART ≥12 months.
	*
	*
	* */
	public static String currentlyOnArtQuery() {
		String qry = "select t.patient_id from (\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "            greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "            greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "            greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "            d.patient_id as disc_patient,\n"
		        + "            d.effective_disc_date as effective_disc_date,\n"
		        + "            max(d.visit_date) as date_discontinued,\n"
		        + "            de.patient_id as started_on_drugs,\n"
		        + "            de.date_started,\n"
		        + "            timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "            join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "            join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "            left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and de.date_started <= date_sub(date(:endDate) , interval 12 MONTH)\n"
		        + "            left outer JOIN\n"
		        + "                (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                group by patient_id\n"
		        + "                ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15\n"
		        + "            )\n" + "        ) order by date_started desc\n" + "    ) t limit 10";
		return qry;
		
	}
	
	public static String currentOnARTWithPresumptiveTBQuery() {
		String qry = "select t.patient_id from (\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "            greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "            greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "            greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "            d.patient_id as disc_patient,\n"
		        + "            d.effective_disc_date as effective_disc_date,\n"
		        + "            max(d.visit_date) as date_discontinued,\n"
		        + "            de.patient_id as started_on_drugs,\n"
		        + "            de.date_started,\n"
		        + "            timestampdiff(YEAR ,p.dob,date(:endDate )) as age,\n"
		        + "            mid(max(concat(fup.visit_date,fup.tb_status)),11) as tbStatus\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "            join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "            join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "            left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV'\n"
		        + "            left outer JOIN\n"
		        + "                (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                where date(visit_date) <= date(:endDate ) and program_name='HIV'\n"
		        + "                group by patient_id\n"
		        + "                ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate )\n"
		        + "    group by patient_id\n"
		        + "    having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate )) <= 30) and ((date(d.effective_disc_date) > date(:endDate ) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15 and tbStatus = 142177\n"
		        + "            )        ) order by date_started desc    ) t limit 10\n" + "\n" + "\n";
		return qry;
	}
	
	public static String pedsCurrentlyOnArtMoreThan12MonthsQuery() {
		String qry = "select t.patient_id from (\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "            greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "            greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "            greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "            d.patient_id as disc_patient,\n"
		        + "            d.effective_disc_date as effective_disc_date,\n"
		        + "            max(d.visit_date) as date_discontinued,\n"
		        + "            de.patient_id as started_on_drugs,\n"
		        + "            de.date_started,\n"
		        + "            timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "            join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "            join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "            left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and de.date_started <= date_sub(date(:endDate) , interval 12 MONTH)\n"
		        + "            left outer JOIN\n"
		        + "                (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                group by patient_id\n"
		        + "                ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age <15\n"
		        + "            )\n" + "        ) order by date_started desc\n" + "    ) t limit 10";
		return qry;
		
	}
}
