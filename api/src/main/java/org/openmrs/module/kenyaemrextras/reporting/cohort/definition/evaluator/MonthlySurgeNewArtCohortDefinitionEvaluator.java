/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeNewArtCohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MonthlySurgeTxCurrCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Evaluator for patients eligible for New On Art Monthly Surge
 */
@Handler(supports = { MonthlySurgeNewArtCohortDefinition.class })
public class MonthlySurgeNewArtCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MonthlySurgeNewArtCohortDefinition definition = (MonthlySurgeNewArtCohortDefinition) cohortDefinition;
		if (definition == null)
			return null;
		
		String qry = "select net.patient_id   \n"
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
		Cohort newCohort = new Cohort();
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		builder.addParameter("startDate", startDate);
		List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);
		
		newCohort.setMemberIds(new HashSet<Integer>(ptIds));
		
		return new EvaluatedCohort(newCohort, definition, context);
	}
	
}
