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
	
}
