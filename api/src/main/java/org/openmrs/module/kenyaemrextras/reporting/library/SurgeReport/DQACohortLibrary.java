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

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.RevisedDatim.DatimCohortLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Library of cohort definitions for Covid-19 vaccinations
 */
@Component
public class DQACohortLibrary {
	
	@Autowired
	private DatimCohortLibrary datimCohortLibrary;
	
	public CohortDefinition fullyVaccinated() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_covid19_assessment a where a.final_vaccination_status = 5585 and a.visit_date <= date(:endDate)";
		cd.setName("fullyVaccinated");
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setQuery(sqlQuery);
		cd.setDescription("fullyVaccinated");
		
		return cd;
	}
	
	public CohortDefinition partiallyVaccinated() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_covid19_assessment group by patient_id\n"
		        + "        having mid(max(concat(visit_date,final_vaccination_status)),11) = 166192\n"
		        + "        and max(visit_date) <= date(:endDate);";
		cd.setName("partiallyVaccinated;");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("partiallyVaccinated");
		
		return cd;
	}
	
	/**
	 * Cohort definition for adults
	 * 
	 * @return
	 */
	public CohortDefinition aged15AndAbove() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate))>= 15;\n";
		cd.setName("aged18andAbove");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("aged18andAbove");
		
		return cd;
	}
	
	/**
	 * Cohort definition for peads
	 * 
	 * @return
	 */
	public CohortDefinition aged14AndBelow() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate)) <= 14;\n";
		cd.setName("aged18andAbove");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("aged18andAbove");
		
		return cd;
	}
	
	/**
	 * Patients OnArt and partially vaccinated
	 * 
	 * @return the cohort definition
	 */
	public CohortDefinition onArtPartiallyVaccinated() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txcurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("partiallyVaccinated",
		    ReportUtils.map(partiallyVaccinated(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("aged18AndAbove", ReportUtils.map(aged15AndAbove(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txcurr AND aged18AndAbove AND partiallyVaccinated");
		return cd;
	}
	
	/**
	 * Patients OnArt and 18 years and above
	 * 
	 * @return the cohort definition
	 */
	public CohortDefinition onArtAged18AndAbove() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txcurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("aged18andAbove", ReportUtils.map(aged15AndAbove(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txcurr AND aged18andAbove");
		return cd;
	}
	
	/**
	 * Returns Peds (14 years and below) by weight taken during their last visit
	 * 
	 * @param weightBand
	 * @return
	 */
	public CohortDefinition aged14AndBelowWeights(String weightBand) {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n" + "from (select t.patient_id,\n"
		        + "             left(max(concat(t.visit_date, t.weight)), 10) as visit_date,\n"
		        + "             mid(max(concat(t.visit_date, t.weight)), 11)     weight\n"
		        + "      from kenyaemr_etl.etl_patient_triage t\n" + "      where date(t.visit_date) <= date(:endDate)\n"
		        + "      GROUP BY t.patient_id) a\n"
		        + "inner join kenyaemr_etl.etl_patient_demographics d on a.patient_id = d.patient_id\n"
		        + "where a.weight between " + weightBand + " \n"
		        + "and timestampdiff(YEAR, date(d.DOB), date(:endDate)) <= 14;";
		cd.setName("aged14AndBelowWeights");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("aged14AndBelowWeights");
		return cd;
	}
	
	/**
	 * Returns clients on DTG as of the current date
	 * 
	 * @param
	 * @return
	 */
	public CohortDefinition clientsOnDTG() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select de.patient_id, mid(max(concat(de.visit_date, de.regimen_name)), 11) as current_regimen\n"
		        + "      from kenyaemr_etl.etl_drug_event de\n" + "      where de.program = 'HIV'\n"
		        + "        and de.discontinued is null\n" + "        and de.date_started <= date(curdate())\n"
		        + "      group by de.patient_id) a\n" + "where a.current_regimen like ('%DTG%');";
		cd.setName("clientsOnDTG");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("clientsOnDTG");
		
		return cd;
	}
	
	/**
	 * TX_CURR based on current date (specific to 2022 DTG)
	 * 
	 * @return
	 */
	public CohortDefinition dtg22TxCurrBasedOnCurrDate() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "           d.patient_id as disc_patient,\n"
		        + "           d.effective_disc_date as effective_disc_date,\n"
		        + "           max(d.visit_date) as date_discontinued,\n"
		        + "           de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "           left join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(curdate())\n"
		        + "           left outer JOIN\n"
		        + "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "              where date(visit_date) <= date(curdate()) and program_name='HIV'\n"
		        + "              group by patient_id\n"
		        + "             ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(curdate())\n"
		        + "    group by patient_id\n"
		        + "    having (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(curdate()) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "            )\n" + "        )\n" + "    ) t;";
		cd.setName("dtg22TxCurrBasedOnCurrDate");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("dtg22TxCurrBasedOnCurrDate");
		
		return cd;
	}
	
	/**
	 * Returns Peds on ART with DTG
	 * 
	 * @param ageBand
	 * @return
	 */
	public CohortDefinition artPedsOnDTG(String ageBand) {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txcurrDTG222",
		    ReportUtils.map(dtg22TxCurrBasedOnCurrDate(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("aged14AndBelowWeights",
		    ReportUtils.map(aged14AndBelowWeights(ageBand), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("clientsOnDTG", ReportUtils.map(clientsOnDTG(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txcurrDTG222 AND aged14AndBelowWeights AND clientsOnDTG");
		return cd;
	}
	
	/**
	 * Returns total unique clients visits within period
	 * 
	 * @param
	 * @return
	 */
	public CohortDefinition totalVisits() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select fup.patient_id from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=fup.patient_id\n"
		        + "where fup.visit_date between '2022-06-01' and '2022-09-30'\n" + "group by fup.patient_id;";
		cd.setName("totalARTClinicalVisits");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("totalARTClinicalVisits");
		
		return cd;
	}
	
	/**
	 * Returns total unique clients visits within period and verified
	 * 
	 * @param
	 * @return
	 */
	public CohortDefinition totalVerified() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select fup.patient_id from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=fup.patient_id\n"
		        + "where fup.visit_date between '2022-06-01' and '2022-09-30'\n"
		        + "      and (d.national_unique_patient_identifier is not null or d.national_unique_patient_identifier <> '')\n"
		        + "group by fup.patient_id;";
		cd.setName("totalARTVerified");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("totalARTVerified");
		
		return cd;
	}
	
	/**
	 * Returns total unique clients visits within period and unverified
	 * 
	 * @param
	 * @return
	 */
	public CohortDefinition totalUnverified() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select fup.patient_id from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=fup.patient_id\n"
		        + "where fup.visit_date between '2022-06-01' and '2022-09-30'\n"
		        + "      and (d.national_unique_patient_identifier is null or d.national_unique_patient_identifier = '')\n"
		        + "group by fup.patient_id;";
		cd.setName("totalARTUnverified");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("totalARTUnverified");
		
		return cd;
	}
}
