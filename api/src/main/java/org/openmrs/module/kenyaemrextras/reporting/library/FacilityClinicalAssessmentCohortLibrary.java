/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.library;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731Greencard.ETLMoh731GreenCardCohortLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.RevisedDatim.DatimCohortLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.publicHealthActionReport.PublicHealthActionCohortLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.kenyaemr.calculation.library.tb.InTbProgramCalculation;
import org.openmrs.module.kenyaemr.calculation.library.otz.OnOTZProgramCalculation;
import org.openmrs.module.kenyaemr.calculation.library.ovc.OnOVCProgramCalculation;

import java.util.Date;

/**
 * Library of cohort definitions for public health action
 */
@Component
public class FacilityClinicalAssessmentCohortLibrary {
	
	@Autowired
	private DatimCohortLibrary datimCohortLibrary;
	
	@Autowired
	private PublicHealthActionCohortLibrary carCohorts;
	
	@Autowired
	private PublicHealthActionCohortLibrary publicHealthActionCohortLibrary;
	
	@Autowired
	private ETLMoh731GreenCardCohortLibrary moh731Cohorts;
	
	/**
	 * TX_CURR
	 * 
	 * @return
	 */
	public CohortDefinition currentlyOnArt() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("currentlyOnArt",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("currentlyOnArt");
		return cd;
	}
	
	/**
	 * TX_ML
	 * 
	 * @return
	 */
	public CohortDefinition txML() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txMLIITUnder3MonthsInTx",
		    ReportUtils.map(datimCohortLibrary.txMLIITUnder3MonthsInTx(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("txMLIIT3To5MonthsInTx",
		    ReportUtils.map(datimCohortLibrary.txMLIIT3To5MonthsInTx(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("txMLIITAtleast6MonthsInTx",
		    ReportUtils.map(datimCohortLibrary.txMLIITAtleast6MonthsInTx(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txMLIITUnder3MonthsInTx OR txMLIIT3To5MonthsInTx OR txMLIITAtleast6MonthsInTx");
		return cd;
	}
	
	/**
	 * Had VL test within the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition vlWithinReportingPeriod() {
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_laboratory_extract where lab_test in (1305,856) and visit_date between date(:startDate) and date(:endDate) group by patient_id;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlWithinReportingPeriod");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("VL Within last 3 months");
		return cd;
	}
	
	/**
	 * Suppressed VL within reporting period < 1000 cps/ml or LDL
	 * 
	 * @return
	 */
	public CohortDefinition suppressedVLWithinReportingPeriod() {
		String sqlQuery = "select a.patient_id\n"
		        + "from (select b.patient_id,\n"
		        + "             b.visit_date as vl_date,\n"
		        + "             if( b.lab_test= 856, b.test_result,\n"
		        + "                if(b.lab_test = 1305 and\n"
		        + "                   test_result = 1302, 'LDL', '')) as vl_result\n"
		        + "      from (select x.patient_id   as patient_id,\n"
		        + "                   x.visit_date   as visit_date,                    x.lab_test     as lab_test,\n"
		        + "                   x.test_result  as test_result\n"
		        + "            from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "            where x.lab_test in (1305, 856) and visit_date between date(:startDate) and date(:endDate)       --    group by x.patient_id, x.visit_date\n"
		        + "            order by visit_date desc) b\n" + "      group by patient_id\n"
		        + "         having (vl_result < 1000 or vl_result = 'LDL'))a;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("suppressedVLWithinReportingPeriod");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Suppressed VL Within reporting period");
		return cd;
	}
	
	/**
	 * TX_CURR who have been on treatment for at least six months and eligible for vl test
	 * 
	 * @return
	 */
	public CohortDefinition eligibleForVlTest() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("onTreatmentAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND onTreatmentAtleast6Months");
		return cd;
	}
	
	/**
	 * TX_CURR who have been on treatment for at least six months
	 * 
	 * @return
	 */
	public CohortDefinition vlUptake() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("onTreatmentAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("vlWithinReportingPeriod",
		    ReportUtils.map(vlWithinReportingPeriod(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND onTreatmentAtleast6Months AND vlWithinReportingPeriod");
		return cd;
	}
	
	/**
	 * Suppressed VL
	 * 
	 * @return
	 */
	public CohortDefinition suppressedVL() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("onTreatmentAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("suppressedVLWithinReportingPeriod",
		    ReportUtils.map(suppressedVLWithinReportingPeriod(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND onTreatmentAtleast6Months AND suppressedVLWithinReportingPeriod");
		return cd;
	}
	
	/**
	 * TX_CURR with MMD (Multi-month dispense)
	 * 
	 * @return
	 */
	public CohortDefinition dsd() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("currentlyOnART3To5MonthsMMD",
		    ReportUtils.map(datimCohortLibrary.currentlyOnART3To5MonthsMMD(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("currentlyOnART6MonthsAndAboveMMD", ReportUtils.map(
		    datimCohortLibrary.currentlyOnART6MonthsAndAboveMMD(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND (currentlyOnART3To5MonthsMMD OR currentlyOnART6MonthsAndAboveMMD)");
		return cd;
	}
	
	/**
	 * Clients active in OTZ
	 * 
	 * @return
	 */
	public CohortDefinition inOtz() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select pp.patient_id from patient_program pp\n"
		        + "        inner join program p on p.program_id = pp.program_id\n"
		        + "      where date(pp.date_completed) is null and p.name ='OTZ'\n" + "      group by pp.patient_id\n"
		        + "      having max(date(pp.date_enrolled)) <= date(:endDate);";
		cd.setName("inOtz");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("inOtz");
		return cd;
	}
	
	/**
	 * TX_CURR aged between 10-19 years
	 * 
	 * @return
	 */
	public CohortDefinition otz() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inOtz", ReportUtils.map(inOtz(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND inOtz");
		return cd;
	}
	
	/**
	 * Clients active in OVC
	 * 
	 * @return
	 */
	public CohortDefinition clientsActiveInOVC() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select pp.patient_id from patient_program pp\n"
		        + "        inner join program p on p.program_id = pp.program_id\n"
		        + "      where date(pp.date_completed) is null and p.name ='OVC'\n" + "      group by pp.patient_id\n"
		        + "      having max(date(pp.date_enrolled)) <= date(:endDate);";
		cd.setName("clientsActiveInOVC");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("clientsActiveInOVC");
		return cd;
	}
	
	/**
	 * TX_CURR aged between 0-17 years
	 * 
	 * @return
	 */
	public CohortDefinition ovc() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("clientsActiveInOVC",
		    ReportUtils.map(clientsActiveInOVC(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND clientsActiveInOVC");
		return cd;
	}
	
	/**
	 * eHTS clients within the period.
	 * 
	 * @return
	 */
	public CohortDefinition eHTS() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("htsAllNumberTested",
		    ReportUtils.map(moh731Cohorts.htsAllNumberTested(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("htsAllNumberTested");
		return cd;
	}
	
	/**
	 * Age cohort eligible for reporting in this context
	 * 
	 * @return
	 */
	public CohortDefinition covidVaccineAgeCohort() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate))>= 15;\n";
		cd.setName("covidVaccineAgeCohort");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("covidVaccineAgeCohort");
		
		return cd;
	}
	
	/**
	 * Fully vaccinated clients
	 * 
	 * @return
	 */
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
	
	/**
	 * TX_CURR aged 15+ years who are vaccinated against Covid-19
	 * 
	 * @return
	 */
	public CohortDefinition covidVaccination() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("covidVaccineAgeCohort",
		    ReportUtils.map(covidVaccineAgeCohort(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("currentlyOnArt", ReportUtils.map(currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("fullyVaccinated", ReportUtils.map(fullyVaccinated(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("covidVaccineAgeCohort AND currentlyOnArt AND fullyVaccinated");
		return cd;
	}
	
	/**
	 * Number of tests captured in eHTS from the HTS Register
	 * 
	 * @return
	 */
	public CohortDefinition htsNumberTested() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select e.patient_id from kenyaemr_etl.etl_prep_enrolment e where e.voided =0 group by e.patient_id having max(date(e.visit_date)) between date(:startDate) and date(:endDate);";
		cd.setName("htsNumberTested");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of tests captured in eHTS from the HTS Register");
		return cd;
	}
	
	/**
	 * Newly initiated PrEP - From PrEP register
	 * 
	 * @return
	 */
	public CohortDefinition newlyEnrolledInPrEPRegister() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select e.patient_id from kenyaemr_etl.etl_prep_enrolment e where e.voided =0 group by e.patient_id\n"
		        + "having min(date(e.visit_date)) between date(:startDate) and date(:endDate);";
		cd.setName("newlyEnrolledInPrEPRegister");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Newly initiated PrEP - From PrEP register");
		return cd;
	}
	
	/**
	 * Number of KP clients in Datim
	 * 
	 * @return
	 */
	public CohortDefinition kpClientsQuarterly() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select c.client_id\n"
		        + "from kenyaemr_etl.etl_contact c\n"
		        + "         left join (select p.client_id\n"
		        + "                    from kenyaemr_etl.etl_peer_calendar p\n"
		        + "                    where p.voided = 0\n"
		        + "                    group by p.client_id\n"
		        + "                    having max(p.visit_date) between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate)) cp\n"
		        + "                   on c.client_id = cp.client_id\n"
		        + "         left join (select v.client_id\n"
		        + "                    from kenyaemr_etl.etl_clinical_visit v\n"
		        + "                    where v.voided = 0\n"
		        + "                    group by v.client_id\n"
		        + "                    having max(v.visit_date) between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate)) cv\n"
		        + "                   on c.client_id = cv.client_id\n"
		        + "         left join (select d.patient_id, max(d.visit_date) latest_visit\n"
		        + "                    from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                    where d.program_name = 'KP') d on c.client_id = d.patient_id\n"
		        + "where (d.patient_id is null or d.latest_visit > date(:endDate)) and c.voided = 0\n"
		        + "    and cp.client_id is not null\n" + "   or cv.client_id is not null\n" + "group by c.client_id;";
		cd.setName("kpClientsDatim");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of KP clients Datim");
		return cd;
	}
	
	/**
	 * Number of KP clients monthly
	 * 
	 * @return
	 */
	public CohortDefinition kpClientsNumerator() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select c.client_id\n"
		        + "from kenyaemr_etl.etl_contact c\n"
		        + "         left join (select p.client_id\n"
		        + "                    from kenyaemr_etl.etl_peer_calendar p\n"
		        + "                    where p.voided = 0\n"
		        + "                    group by p.client_id\n"
		        + "                    having max(p.visit_date) between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate)) cp\n"
		        + "                   on c.client_id = cp.client_id\n"
		        + "         left join (select v.client_id\n"
		        + "                    from kenyaemr_etl.etl_clinical_visit v\n"
		        + "                    where v.voided = 0\n"
		        + "                    group by v.client_id\n"
		        + "                    having max(v.visit_date) between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate)) cv\n"
		        + "                   on c.client_id = cv.client_id\n"
		        + "         left join (select d.patient_id, max(d.visit_date) latest_visit\n"
		        + "                    from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                    where d.program_name = 'KP') d on c.client_id = d.patient_id\n"
		        + "where (d.patient_id is null or d.latest_visit > date(:endDate)) and c.voided = 0\n"
		        + "    and cp.client_id is not null\n" + "   or cv.client_id is not null\n" + "group by c.client_id;";
		cd.setName("kpClientsMonthly");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of KP clients monthly");
		return cd;
	}
	
	/**
	 * Returns cohort def for clients active in MCH
	 * 
	 * @return
	 */
	public CohortDefinition clientsActiveInMCH() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select pp.patient_id from patient_program pp\n"
		        + "               inner join program p on p.program_id = pp.program_id\n"
		        + "             where date(pp.date_completed) is null and p.name in ('MCH - Child Services','MCH - Mother Services')\n"
		        + "             group by pp.patient_id\n"
		        + "             having max(date(pp.date_enrolled)) <= date(:endDate);";
		cd.setName("clientsActiveInMCH");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("clientsActiveInMCH");
		return cd;
	}
	
	/**
	 * TX_CURR who are active in PMTCT
	 * 
	 * @return
	 */
	public CohortDefinition activeInMCHAndTXCurr() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("clientsActiveInMCH",
		    ReportUtils.map(clientsActiveInMCH(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND clientsActiveInMCH");
		return cd;
	}
	
	/**
	 * Clients active in TB treatment
	 * 
	 * @return
	 */
	public CohortDefinition inTBTreatment() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select d.patient_id\n"
		        + "        from kenyaemr_etl.etl_patient_demographics d\n"
		        + "                 left join (select pp.patient_id as program_client, date(pp.date_completed) as date_completed\n"
		        + "                            from patient_program pp\n"
		        + "                                     inner join program p on p.program_id = pp.program_id and p.name = 'TB' and\n"
		        + "                                                             date(pp.date_enrolled) <= date(:endDate)\n"
		        + "                            where date(pp.date_completed) is null) v on d.patient_id = v.program_client\n"
		        + "                 left join (select v.patient_id                                                 as hiv_client,\n"
		        + "                                   max(date(v.visit_date)) as last_fup_visit,\n"
		        + "                                   mid(max(concat(date(v.visit_date), v.on_anti_tb_drugs)), 11) as on_tb_drugs\n"
		        + "                            from kenyaemr_etl.etl_patient_hiv_followup v\n"
		        + "                            where date(v.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "                              and v.on_anti_tb_drugs = 1065\n"
		        + "                            group by v.patient_id having max(date(visit_date)) <= date(:endDate)) c on d.patient_id = c.hiv_client\n"
		        + "            where v.program_client is not null or (c.hiv_client is not null and c.last_fup_visit > v.date_completed);";
		cd.setName("inTBTreatment");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("inTBTreatment");
		return cd;
	}
	
	/**
	 * TX_CURR who are active in TB Treatment
	 * 
	 * @return
	 */
	public CohortDefinition activeInTBRx() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inTBTreatment", ReportUtils.map(inTBTreatment(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND inTBTreatment");
		return cd;
	}
	
	/**
	 * Aged 18+ years
	 * 
	 * @return
	 */
	public CohortDefinition agedAtleast18() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate))>= 18;";
		cd.setName("agedAtleast18");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("agedAtleast18");
		return cd;
	}
	
	/**
	 * TX_CURR adults (18+ years)
	 * 
	 * @return
	 */
	public CohortDefinition txCurrAdults() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("agedAtleast18", ReportUtils.map(agedAtleast18(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND agedAtleast18");
		return cd;
	}
	
	/**
	 * Clients with NUPI
	 * 
	 * @return
	 */
	public CohortDefinition artClientsWithNUPI() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("clientsWithNUPI",
		    ReportUtils.map(publicHealthActionCohortLibrary.clientsWithNUPI(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND clientsWithNUPI");
		return cd;
	}
	
	/**
	 * TX_CURR adults (18+ years) with NUPI captured
	 * 
	 * @return
	 */
	public CohortDefinition artVerifiedAdults() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("agedAtleast18", ReportUtils.map(agedAtleast18(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("artClientsWithNUPI",
		    ReportUtils.map(artClientsWithNUPI(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("agedAtleast18 AND artClientsWithNUPI");
		return cd;
	}
	
	/**
	 * TX_CURR Peds (< 18 years old)
	 * 
	 * @return
	 */
	public CohortDefinition txCurrPeds() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("txCurrAdults", ReportUtils.map(txCurrAdults(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND NOT txCurrAdults");
		return cd;
	}
	
	/**
	 * TX_CURR (peds, <18) with NUPI captured
	 * 
	 * @return
	 */
	public CohortDefinition artVerifiedPeds() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurrPeds", ReportUtils.map(txCurrPeds(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("artClientsWithNUPI",
		    ReportUtils.map(artClientsWithNUPI(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurrPeds AND artClientsWithNUPI");
		return cd;
	}
	
	/**
	 * Number of VL tests ordered Looks at 9-month period
	 * 
	 * @return
	 */
	public CohortDefinition vlTestsOrdered() {
		String queryString = "select od.patient_id from kenyaemr_order_entry_lab_manifest_order o\n"
		        + "          inner join orders od on od.order_id = o.order_id\n"
		        + "          inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = od.patient_id\n"
		        + "where o.sample_collection_date between date_sub(date(:startDate), INTERVAL 6 MONTH) and date(:endDate)\n"
		        + "and o.status != 'Pending';";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlTestsOrdered");
		cd.setQuery(queryString);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of VL tests ordered");
		return cd;
	}
	
	/**
	 * Number of VL tests with results Looks at 9-month period
	 * 
	 * @return
	 */
	public CohortDefinition vlTestsWithResults() {
		String queryString = "select od.patient_id from kenyaemr_order_entry_lab_manifest_order o\n"
		        + "       inner join orders od on od.order_id = o.order_id\n"
		        + "       inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = od.patient_id\n"
		        + "where o.sample_collection_date between date_sub(date(:startDate), INTERVAL 6 MONTH) and date(:endDate) and o.status = 'Complete';";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlTestsWithResults");
		cd.setQuery(queryString);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of VL tests with results");
		return cd;
	}
	
	/**
	 * Clients with unsuppressed repeat VL within the period
	 * 
	 * @return
	 */
	public CohortDefinition unsuppressedRepeatVL() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id\n"
		        + "from (select b.patient_id,\n"
		        + "             max(b.visit_date)                                                       as vl_date,\n"
		        + "             mid(max(concat(b.visit_date, b.lab_test)), 11)                          as lab_test,\n"
		        + "             mid(max(concat(b.visit_date, b.order_reason)), 11)                      as order_reason,\n"
		        + "             if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 856, mid(max(concat(b.visit_date, b.test_result)), 11),\n"
		        + "                if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 1305 and\n"
		        + "                   mid(max(concat(visit_date, test_result)), 11) = 1302, 'LDL', '')) as vl_result\n"
		        + "      from (select x.patient_id   as patient_id,\n"
		        + "                   x.visit_date   as visit_date,                    x.lab_test     as lab_test,\n"
		        + "                   x.test_result  as test_result,\n"
		        + "                   x.order_reason as order_reason\n"
		        + "            from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "            where x.lab_test in (1305, 856)             group by x.patient_id, x.visit_date\n"
		        + "            order by visit_date desc) b       group by patient_id\n" + "      having vl_date between\n"
		        + "          date_sub(date(:endDate), interval 12 MONTH) and date(:endDate)\n"
		        + "         and vl_result >= 1000          and order_reason = 843)a;";
		cd.setName("unsuppressedRepeatVL");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("unsuppressedRepeatVL");
		return cd;
	}
	
	/**
	 * Clients with enhanced adherence encounter within the reporting period
	 * 
	 * @return
	 */
	public CohortDefinition enhancedAdherence() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select a.patient_id from kenyaemr_etl.etl_enhanced_adherence a where date(a.visit_date) between date_sub(date(:endDate), interval 12 MONTH) and date(:endDate);";
		cd.setName("enhancedAdherence");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("enhancedAdherence");
		return cd;
	}
	
	/**
	 * Children (<18 years) with unsuppressed VL Must be on Treatement for atleast 6 months
	 * 
	 * @return
	 */
	public CohortDefinition unsuppressedVLPeds() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurrPeds", ReportUtils.map(txCurrPeds(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("patientInTXAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("enhancedAdherence", ReportUtils.map(enhancedAdherence(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("unsuppressedRepeatVL",
		    ReportUtils.map(unsuppressedRepeatVL(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurrPeds AND patientInTXAtleast6Months AND enhancedAdherence AND unsuppressedRepeatVL");
		return cd;
	}
	
	/**
	 * Adults (18+ years) with unsuppressed VL Must be on Treatement for atleast 6 months
	 * 
	 * @return
	 */
	public CohortDefinition unsuppressedVLAdults() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurrAdults", ReportUtils.map(txCurrAdults(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("patientInTXAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("enhancedAdherence", ReportUtils.map(enhancedAdherence(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("unsuppressedRepeatVL",
		    ReportUtils.map(unsuppressedRepeatVL(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurrAdults AND patientInTXAtleast6Months AND enhancedAdherence AND unsuppressedRepeatVL");
		return cd;
	}
}
