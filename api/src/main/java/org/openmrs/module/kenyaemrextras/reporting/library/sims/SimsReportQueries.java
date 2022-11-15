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
		        + " from kenyaemr_etl.etl_hiv_enrollment e \n"
		        + " join kenyaemr_etl.etl_patient_demographics p on p.patient_id=e.patient_id\n"
		        + " left join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = e.patient_id and fup.visit_date between date(:startDate) and date(:endDate)\n"
		        + " where date(e.date_confirmed_hiv_positive) between date(:startDate) and date(:endDate) and timestampdiff(YEAR ,p.dob,date(:endDate)) >= 15 ";
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
		        + "                            on t.patient_id = vl.patient_id where vl_result >= 1000)a limit 10";
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
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "    greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "    greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "    greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "    d.patient_id as disc_patient,\n"
		        + "    d.effective_disc_date as effective_disc_date,\n"
		        + "    max(d.visit_date) as date_discontinued,\n"
		        + "    de.patient_id as started_on_drugs,\n"
		        + "    de.date_started,\n"
		        + "    timestampdiff(YEAR ,p.dob,date(:endDate)) as age,\n"
		        + "    mid(max(concat(fup.visit_date,fup.tb_status)),11) as tbStatus\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "    join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "    join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "    left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV'\n"
		        + "    left outer JOIN\n"
		        + "        (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "        where date(visit_date) <= date(:endDate ) and program_name='HIV'\n"
		        + "        group by patient_id\n"
		        + "        ) d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate )\n"
		        + "group by patient_id\n"
		        + "having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "(\n"
		        + "    ((timestampdiff(DAY,date(latest_tca),date(:endDate )) <= 30) and ((date(d.effective_disc_date) > date(:endDate ) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "        and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15 and tbStatus =142177 \n"
		        + "    )        ) order by date_started desc ) t limit 10";
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
	
	public static String pedMissedAppointment() {
		String qry = "select t.patient_id from(\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "d.patient_id as disc_patient,\n"
		        + "d.effective_disc_date as effective_disc_date,\n"
		        + "max(d.visit_date) as date_discontinued,\n"
		        + "d.discontinuation_reason,\n"
		        + "de.patient_id as started_on_drugs,\n"
		        + "timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
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
		        + "and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null) and age <15\n"
		        + ") ) t limit 10;";
		return qry;
	}
	
	public static String pedNewlyDiagnosedQuery() {
		String qry = "select e.patient_id\n"
		        + " from kenyaemr_etl.etl_hiv_enrollment e \n"
		        + " join kenyaemr_etl.etl_patient_demographics p on p.patient_id=e.patient_id\n"
		        + " left join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = e.patient_id and fup.visit_date between date(:startDate) and date(:endDate)\n"
		        + " where date(e.date_confirmed_hiv_positive) between date(:startDate) and date(:endDate) and timestampdiff(YEAR ,p.dob,date(:endDate)) < 15 ";
		return qry;
	}
	
	public static String adultsOnArtScreenedForCervicalCancerQuery() {
		String qry = "\n"
		        + "select t.patient_id from (\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "        greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "        greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "        greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "        d.patient_id as disc_patient,\n"
		        + "        d.effective_disc_date as effective_disc_date,\n"
		        + "        max(d.visit_date) as date_discontinued,\n"
		        + "        de.patient_id as started_on_drugs,\n"
		        + "        de.date_started,\n"
		        + "        timestampdiff(YEAR ,p.dob,date(:endDate)) as age,\n"
		        + "        p.gender as gender,\n"
		        + "        mid(max(concat(cs.visit_date, cs.screening_result)), 11)  as screening_result\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "        join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "        join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "        left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV'\n"
		        + "        left outer JOIN\n"
		        + "            (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "            where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "            group by patient_id\n"
		        + "            ) d on d.patient_id = fup.patient_id\n"
		        + "    join kenyaemr_etl.etl_cervical_cancer_screening cs on e.patient_id = cs.patient_id  and cs.visit_date between date_sub(date(:endDate) , interval 90 DAY) and date(:endDate)\n"
		        + "\n"
		        + "where fup.visit_date <= date(:endDate)\n"
		        + "group by patient_id\n"
		        + "having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "    (\n"
		        + "        ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "            and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15 and gender='F' and screening_result ='Positive'\n"
		        + "        )   ) order by date_started desc ) t limit 10 ";
		return qry;
	}
	
	/**
	 * TX_CURR KPs with clinical encounters within the last 12 months
	 * 
	 * @return
	 */
	public static String txCurrKPsWithVisitsLast12Months() {
		String qry = "select t.patient_id\n"
		        + "        from(\n"
		        + "            select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "                   greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "                   greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "                   greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "                   d.patient_id as disc_patient,\n"
		        + "                   d.effective_disc_date as effective_disc_date,\n"
		        + "                   max(d.visit_date) as date_discontinued,\n"
		        + "                   de.patient_id as started_on_drugs\n"
		        + "            from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "                   join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "                   join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "                   join (select v.client_id\n"
		        + "                      from kenyaemr_etl.etl_clinical_visit v\n"
		        + "                      where v.visit_date between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 12 MONTH)\n"
		        + "                                and date(:endDate)) cv on fup.patient_id = cv.client_id\n"
		        + "                   left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "                   left outer JOIN\n"
		        + "                     (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                      where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                      group by patient_id\n"
		        + "                     ) d on d.patient_id = fup.patient_id\n"
		        + "            where fup.visit_date <= date(:endDate)\n"
		        + "            group by patient_id\n"
		        + "            having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "                (\n"
		        + "                    ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                      and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "                    ))) t;";
		return qry;
	}
	
	/**
	 * TX_CURR KPs with clinical encounters within the last 3 months
	 * 
	 * @return
	 */
	public static String txCurrKPsWithVisitsLast3Months() {
		String qry = "select t.patient_id\n"
		        + "from(\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "d.patient_id as disc_patient,\n"
		        + "d.effective_disc_date as effective_disc_date,\n"
		        + "max(d.visit_date) as date_discontinued,\n"
		        + "de.patient_id as started_on_drugs\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "join (select v.client_id\n"
		        + "from kenyaemr_etl.etl_clinical_visit v\n"
		        + "where v.visit_date between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 3 MONTH)\n"
		        + "       and date(:endDate)) cv on fup.patient_id = cv.client_id\n"
		        + "left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate)\n"
		        + "group by patient_id\n"
		        + "having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "(\n"
		        + "((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "))) t;";
		return qry;
	}
	
	/**
	 * TX_CURR KPs aged at least 15 years newly started on ART
	 * 
	 * @return
	 */
	public static String txCurrKPsAgedAtleast15NewOnART() {
		String qry = "select t.patient_id\n"
		        + "        from(\n"
		        + "        select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "        greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "        greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "        greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "        d.patient_id as disc_patient,\n"
		        + "        d.effective_disc_date as effective_disc_date,\n"
		        + "        max(d.visit_date) as date_discontinued,\n"
		        + "        de.patient_id as started_on_drugs,\n"
		        + "               max(if(e.date_started_art_at_transferring_facility is not null and e.facility_transferred_from is not null, 1, 0)) as TI_on_art,\n"
		        + "        timestampdiff(YEAR, p.DOB, date(:endDate)) as age,\n"
		        + "               de.date_started\n"
		        + "        from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "        join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "        join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "        join (select c.client_id\n"
		        + "        from kenyaemr_etl.etl_contact c\n"
		        + "        where c.visit_date <= date(:endDate)) c on fup.patient_id = c.client_id\n"
		        + "        left outer join (select de.patient_id,min(date(de.date_started)) as date_started, de.program as program from kenyaemr_etl.etl_drug_event de group by de.patient_id) de\n"
		        + "          on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "        left outer JOIN\n"
		        + "        (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "        where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "        group by patient_id\n"
		        + "        ) d on d.patient_id = fup.patient_id\n"
		        + "        where fup.visit_date <= date(:endDate)\n"
		        + "        group by patient_id\n"
		        + "        having (started_on_drugs is not null and started_on_drugs <> '' and timestampdiff(MONTH, date_started, date(:endDate)) <= 3) and (\n"
		        + "        (\n"
		        + "        ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "        and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "        ) and age >= 15) and TI_on_art = 0) t;";
		return qry;
	}
	
	/**
	 * Missed Appointment KPs
	 * 
	 * @return
	 */
	public static String missedAppKPs() {
		String query = "select t.patient_id\n"
		        + "from (\n"
		        + "         select fup.visit_date,\n"
		        + "                fup.patient_id,\n"
		        + "                max(e.visit_date)                                                                as enroll_date,\n"
		        + "                greatest(max(e.visit_date),\n"
		        + "                         ifnull(max(date(e.transfer_in_date)), '0000-00-00'))                    as latest_enrolment_date,\n"
		        + "                greatest(max(fup.visit_date), ifnull(max(d.visit_date), '0000-00-00'))           as latest_vis_date,\n"
		        + "                greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),\n"
		        + "                         ifnull(max(d.visit_date), '0000-00-00'))                                as latest_tca,\n"
		        + "                d.patient_id                                                                     as disc_patient,\n"
		        + "                d.effective_disc_date                                                            as effective_disc_date,\n"
		        + "                max(d.visit_date)                                                                as date_discontinued,\n"
		        + "                d.discontinuation_reason,\n"
		        + "                de.patient_id                                                                    as started_on_drugs,\n"
		        + "                c.latest_kp_enrolment\n"
		        + "         from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "                  join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id\n"
		        + "                  join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id = e.patient_id\n"
		        + "                  join(\n"
		        + "             select c.client_id, max(c.visit_date) as latest_kp_enrolment\n"
		        + "             from kenyaemr_etl.etl_contact c\n"
		        + "             where date(c.visit_date) <= date(:endDate)\n"
		        + "             group by c.client_id\n"
		        + "         ) c on fup.patient_id = c.client_id\n"
		        + "                  left outer join kenyaemr_etl.etl_drug_event de\n"
		        + "                                  on e.patient_id = de.patient_id and de.program = 'HIV' and\n"
		        + "                                     date(date_started) <= date(curdate())\n"
		        + "                  left outer JOIN\n"
		        + "              (select patient_id,\n"
		        + "                      coalesce(date(effective_discontinuation_date), visit_date) visit_date,\n"
		        + "                      max(date(effective_discontinuation_date)) as               effective_disc_date,\n"
		        + "                      discontinuation_reason\n"
		        + "               from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "               where date(visit_date) <= date(curdate())\n"
		        + "                 and program_name = 'HIV'\n"
		        + "               group by patient_id\n"
		        + "              ) d on d.patient_id = fup.patient_id\n"
		        + "         where fup.visit_date <= date(curdate())\n"
		        + "         group by patient_id\n"
		        + "         having (\n"
		        + "                        (timestampdiff(DAY, date(latest_tca), date(curdate())) between 1 and 30) and\n"
		        + "                        ((date(d.effective_disc_date) > date(curdate()) or\n"
		        + "                          date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "                        and (date(latest_vis_date) > date(date_discontinued) and\n"
		        + "                             date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + "                    )) t;";
		return query;
	}
	
	/**
	 * Cohort definition :S_03_11_Q3 In KP program In HIV program In art >= 12 months Age 15 years
	 * and above
	 * 
	 * @return
	 */
	public static String txCurrKpMoreThan12MonthsOnArtQuery() {
		String qry = "select t.patient_id\n"
		        + "    from(\n"
		        + "        select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "               greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "               greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "               greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "               d.patient_id as disc_patient,\n"
		        + "               d.effective_disc_date as effective_disc_date,\n"
		        + "               max(d.visit_date) as date_discontinued,\n"
		        + "               de.patient_id as started_on_drugs,\n"
		        + "     timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
		        + "        from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "               join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "               join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "join(\n"
		        + "select c.client_id from kenyaemr_etl.etl_contact c\n"
		        + "left join (select p.client_id from kenyaemr_etl.etl_peer_calendar p where p.voided = 0 group by p.client_id having max(p.visit_date) between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 12 MONTH)\n"
		        + "and date(:endDate)) cp on c.client_id=cp.client_id\n"
		        + "left join (select v.client_id from kenyaemr_etl.etl_clinical_visit v where v.voided = 0 group by v.client_id having max(v.visit_date) between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 12 MONTH)\n"
		        + "and date(:endDate)) cv on c.client_id=cv.client_id\n"
		        + "left join (select d.patient_id, max(d.visit_date) latest_visit from kenyaemr_etl.etl_patient_program_discontinuation d where d.program_name='KP' group by d.patient_id) d on c.client_id = d.patient_id\n"
		        + "where (d.patient_id is null or d.latest_visit > date(:endDate)) and c.voided = 0  and (cp.client_id is not null or cv.client_id is not null) group by c.client_id\n"
		        + ") kp on kp.client_id = fup.patient_id\n"
		        + "           join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV'\n"
		        + " and de.date_started <= date(:endDate) and timestampdiff(MONTH,date(de.date_started), date(:endDate)) >=12\n"
		        + "           left outer JOIN\n"
		        + "                 (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                  where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "                  group by patient_id\n"
		        + "                 ) d on d.patient_id = fup.patient_id\n"
		        + "        where fup.visit_date <= date(:endDate)\n"
		        + "        group by patient_id\n"
		        + "        having\n"
		        + "            (\n"
		        + "                ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "                  and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15\n"
		        + "                )order by de.date_started desc) t limit 10;";
		return qry;
	}
	
	/**
	 * Cohort definition :S_03_12_Q3 In KP program In HIV program In art >= 12 months Age 15 years
	 * and above Who had ≥1000 copies/mL in the most recent viral load
	 * 
	 * @return
	 */
	public static String KpUnSupressedVLQuery() {
		String qry = "select t.patient_id\n"
		        + "from(\n"
		        + "select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "d.patient_id as disc_patient,\n"
		        + "d.effective_disc_date as effective_disc_date,\n"
		        + "max(d.visit_date) as date_discontinued,\n"
		        + "de.patient_id as started_on_drugs,\n"
		        + "timestampdiff(YEAR ,p.dob,date(:endDate)) as age\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "join(\n"
		        + "select c.client_id from kenyaemr_etl.etl_contact c\n"
		        + "left join (select p.client_id from kenyaemr_etl.etl_peer_calendar p where p.voided = 0 group by p.client_id having max(p.visit_date) between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 12 MONTH)\n"
		        + "and date(:endDate)) cp on c.client_id=cp.client_id\n"
		        + "left join (select v.client_id from kenyaemr_etl.etl_clinical_visit v where v.voided = 0 group by v.client_id having max(v.visit_date) between date_sub(date_add(date(:endDate), INTERVAL 1 DAY), INTERVAL 12 MONTH)\n"
		        + "and date(:endDate)) cv on c.client_id=cv.client_id\n"
		        + "left join (select d.patient_id, max(d.visit_date) latest_visit from kenyaemr_etl.etl_patient_program_discontinuation d where d.program_name='KP' group by d.patient_id) d on c.client_id = d.patient_id\n"
		        + "where (d.patient_id is null or d.latest_visit > date(:endDate)) and c.voided = 0  and (cp.client_id is not null or cv.client_id is not null) group by c.client_id\n"
		        + ") kp on kp.client_id = fup.patient_id\n"
		        + "join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV'\n"
		        + " and de.date_started <= date(:endDate) and timestampdiff(MONTH,date(de.date_started), date(:endDate)) >=12\n"
		        + "join (\n"
		        + " select b.patient_id,max(b.visit_date) as vl_date, date_sub(date(:endDate) , interval 12 MONTH),mid(max(concat(b.visit_date,b.lab_test)),11) as lab_test,\n"
		        + " if(mid(max(concat(b.visit_date,b.lab_test)),11) = 856, mid(max(concat(b.visit_date,b.test_result)),11), if(mid(max(concat(b.visit_date,b.lab_test)),11)=1305 and mid(max(concat(visit_date,test_result)),11) = 1302, 'LDL','')) as vl_result,\n"
		        + " mid(max(concat(b.visit_date,b.urgency)),11) as urgency\n"
		        + " from (select x.patient_id as patient_id,x.visit_date as visit_date,x.lab_test as lab_test, x.test_result as test_result,urgency as urgency\n"
		        + " from kenyaemr_etl.etl_laboratory_extract x where x.lab_test in (1305,856)\n"
		        + " group by x.patient_id,x.visit_date order by visit_date desc)b group by patient_id\n"
		        + " having max(visit_date) between date_sub(date(:endDate) , interval 12 MONTH) and date(:endDate)\n"
		        + " )vl  on fup.patient_id = vl.patient_id\n"
		        + "left outer JOIN\n"
		        + "(select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = fup.patient_id\n"
		        + "where fup.visit_date <= date(:endDate) and vl.vl_result >= 1000\n"
		        + "group by patient_id\n"
		        + "having\n"
		        + "(\n"
		        + "((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null) and age >=15\n"
		        + ")order by de.date_started desc) t limit 10;";
		return qry;
	}
	
}
