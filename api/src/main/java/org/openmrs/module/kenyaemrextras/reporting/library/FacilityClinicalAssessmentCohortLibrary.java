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
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.RevisedDatim.DatimCohortLibrary;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.publicHealthActionReport.PublicHealthActionCohortLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.kenyaemr.calculation.library.tb.InTbProgramCalculation;
import org.openmrs.module.kenyaemr.calculation.library.ActiveInMCHProgramCalculation;
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
	 * TX_NEW
	 * 
	 * @return
	 */
	public CohortDefinition txNew() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txNew", ReportUtils.map(datimCohortLibrary.txNew(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txNew");
		return cd;
	}
	
	/**
	 * TX_ML
	 * 
	 * @return
	 */
	public CohortDefinition txml() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addSearch("txml", ReportUtils.map(txml(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txml");
		return cd;
	}
	
	/**
	 * TX_RTT
	 * 
	 * @return
	 */
	public CohortDefinition txRTT() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txRTT", ReportUtils.map(txRTT(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txRTT");
		return cd;
	}
	
	/**
	 * VL within the last 3 months
	 * 
	 * @return
	 */
	public CohortDefinition vlWithinLast3Months() {
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_laboratory_extract where lab_test in (1305,856) and visit_date between date_sub(:endDate,interval 3 MONTH) and date(:endDate);";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlWithinLast3Months");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("VL Within last 3 months");
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
		cd.addSearch("onTreatmentAtleast6Months",
		    ReportUtils.map(datimCohortLibrary.patientInTXAtleast6Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("vlWithinLast3Months",
		    ReportUtils.map(vlWithinLast3Months(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND onTreatmentAtleast6Months AND vlWithinLast3Months");
		return cd;
	}
	
	/**
	 * TX_CURR with MMD (Multi-month dispense) > 1 month
	 * 
	 * @return
	 */
	public CohortDefinition moreThan1MonthMMD() {
		String sqlQuery = "select f.patient_id from (select f.patient_id,timestampdiff(MONTH,max(f.visit_date),mid(max(concat(f.visit_date,f.next_appointment_date)),11)) \n"
		        + "    months_tca from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "      where f.visit_date <=date(:endDate) and f.next_appointment_date is not null group by f.patient_id having months_tca > 1)f;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("moreThan1MonthMMD");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("TX_CURR with MMD (Multi-month dispense) > 1 month");
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
		cd.addSearch("moreThan1MonthMMD", ReportUtils.map(moreThan1MonthMMD(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND moreThan1MonthMMD");
		return cd;
	}
	
	/**
	 * TX_CURR aged between 10-19 years
	 * 
	 * @return
	 */
	public CohortDefinition otz() {
		CalculationCohortDefinition inOtz = new CalculationCohortDefinition(new OnOTZProgramCalculation());
		inOtz.setName("Clients in OTZ");
		inOtz.addParameter(new Parameter("startDate", "Start Date", Date.class));
		inOtz.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inOtz", ReportUtils.map(inOtz, "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND inOtz");
		return cd;
	}
	
	/**
	 * TX_CURR aged between 0-17 years
	 * 
	 * @return
	 */
	public CohortDefinition ovc() {
		CalculationCohortDefinition inOVC = new CalculationCohortDefinition(new OnOVCProgramCalculation());
		inOVC.setName("Clients in OVC");
		inOVC.addParameter(new Parameter("startDate", "Start Date", Date.class));
		inOVC.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inOVC", ReportUtils.map(inOVC, "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND inOVC");
		return cd;
	}
	
	/**
	 * Clients aged 15+ years as of reporting date
	 * 
	 * @return
	 */
	public CohortDefinition covidVaccineAgeCohort() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate))>= 15;";
		cd.setName("covidVaccineAgeCohort");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("covidVaccineAgeCohort");
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
		cd.addSearch("currentlyOnArt",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("partiallyVaccinated",
		    ReportUtils.map(carCohorts.partiallyVaccinated(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("fullyVaccinated",
		    ReportUtils.map(carCohorts.fullyVaccinated(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("covidVaccineAgeCohort",
		    ReportUtils.map(carCohorts.covidVaccineAgeCohort(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("(currentlyOnArt AND covidVaccineAgeCohort) AND NOT (partiallyVaccinated OR fullyVaccinated)");
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
		String sqlQuery = "select e.patient_id from kenyaemr_etl.etl_prep_enrolment e where e.voided =0 group by e.patient_id having max(date(e.visit_date)) between date(:startDate) and date(:endDate);";
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
	public CohortDefinition kpClientsDatim() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select kp.patient_id from (select fup.patient_id from kenyaemr_etl.etl_patient_hiv_followup fup where fup.visit_date <= date(:endDate)\n"
		        + "group by fup.patient_id)kp;";
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
	public CohortDefinition kpClientsMonthly() {
		SqlCohortDefinition cd = new SqlCohortDefinition();
		String sqlQuery = "select e.client_id from kenyaemr_etl.etl_client_enrollment e inner join kenyaemr_etl.etl_contact c on e.client_id = c.client_id\n"
		        + "         where e.voided = 0 group by e.client_id having max(date(e.visit_date)) <= DATE(:endDate);";
		cd.setName("kpClientsMonthly");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of KP clients monthly");
		return cd;
	}
	
	/**
	 * TX_CURR who are active in PMTCT
	 * 
	 * @return
	 */
	public CohortDefinition activeInMCH() {
		CalculationCohortDefinition inMCH = new CalculationCohortDefinition(new ActiveInMCHProgramCalculation());
		inMCH.setName("Clients in MCH");
		inMCH.addParameter(new Parameter("startDate", "Start Date", Date.class));
		inMCH.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inMCH", ReportUtils.map(inMCH, "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurr AND inMCH");
		return cd;
	}
	
	/**
	 * TX_CURR who are active in TB Treatment
	 * 
	 * @return
	 */
	public CohortDefinition activeInTBRx() {
		CalculationCohortDefinition inTBTreatment = new CalculationCohortDefinition(new InTbProgramCalculation());
		inTBTreatment.setName("Clients in TB Treatment");
		inTBTreatment.addParameter(new Parameter("startDate", "Start Date", Date.class));
		inTBTreatment.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurr",
		    ReportUtils.map(datimCohortLibrary.currentlyOnArt(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("inTBTreatment", ReportUtils.map(inTBTreatment, "startDate=${startDate},endDate=${endDate}"));
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
		String sqlQuery = "select patient_id from kenyaemr_etl.etl_patient_demographics where timestampdiff(YEAR ,dob,date(:endDate))>= 185;";
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
	 * Clients with national ID
	 * 
	 * @return
	 */
	public CohortDefinition clientsWithNationalId() {
		String sqlQuery = "select d.patient_id from kenyaemr_etl.etl_patient_demographics d where d.national_id_no is not null;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("clientsWithNationalId");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Clients with NUPI");
		return cd;
	}
	
	/**
	 * TX_CURR adults (18+ years) with national ID number captured
	 * 
	 * @return
	 */
	public CohortDefinition artVerifiedAdults() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurrAdults", ReportUtils.map(txCurrAdults(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("clientsWithNationalId",
		    ReportUtils.map(clientsWithNationalId(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurrAdults AND clientsWithNationalId");
		return cd;
	}
	
	/**
	 * Clients with birth Certificate no.
	 * 
	 * @return
	 */
	public CohortDefinition clientsWithBirthCertNo() {
		String sqlQuery = "select d.patient_id from kenyaemr_etl.etl_patient_demographics d where d.birth_certificate_no is not null;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("clientsWithBirthCertNo");
		cd.setQuery(sqlQuery);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Clients with birth Certificate no.");
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
	 * TX_CURR (peds, <18) with Birth cert no. captured
	 * 
	 * @return
	 */
	public CohortDefinition artVerifiedPeds() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.addSearch("txCurrPeds", ReportUtils.map(txCurrPeds(), "startDate=${startDate},endDate=${endDate}"));
		cd.addSearch("clientsWithBirthCertNo",
		    ReportUtils.map(clientsWithBirthCertNo(), "startDate=${startDate},endDate=${endDate}"));
		cd.setCompositionString("txCurrPeds AND clientsWithBirthCertNo");
		return cd;
	}
	
	/**
	 * Number of VL tests ordered
	 * 
	 * @return
	 */
	public CohortDefinition vlTestsOrdered() {
		String queryString = "select od.patient_id from\n" +
				"    kenyaemr_order_entry_lab_manifest_order o\n" +
				"        inner join orders od on od.order_id = o.order_id\n" +
				"        inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = od.patient_id\n" +
				"where o.sample_collection_date between date(:startDate) and date(:endDate);";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlTestsOrdered");
		cd.setQuery(queryString);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of VL tests ordered");
		return cd;
	}
	
	/**
	 * Number of VL tests with results
	 * 
	 * @return
	 */
	public CohortDefinition vlTestsWithResults() {
		String queryString = "select od.patient_id from\n" +
				"kenyaemr_order_entry_lab_manifest_order o\n" +
				"    inner join orders od on od.order_id = o.order_id\n" +
				"    inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = od.patient_id\n" +
				"where o.sample_collection_date between date(:startDate) and date(:endDate)\n" +
				"and result is not null;";
		SqlCohortDefinition cd = new SqlCohortDefinition();
		cd.setName("vlTestsWithResults");
		cd.setQuery(queryString);
		cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cd.setDescription("Number of VL tests with results");
		return cd;
	}
	
}
