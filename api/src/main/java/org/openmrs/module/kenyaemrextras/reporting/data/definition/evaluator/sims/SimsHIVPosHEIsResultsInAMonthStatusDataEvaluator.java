/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHIVPosHEIsResultsInAMonthStatusDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates whether a documentation of HIV test result was provided to the caregiver within one
 * month of the specimen's collection for Most recent HIV-infected infants
 */
@Handler(supports = SimsHIVPosHEIsResultsInAMonthStatusDataDefinition.class, order = 50)
public class SimsHIVPosHEIsResultsInAMonthStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id,\n"
		        + "       IF(x.date_test_requested is null and c.sample_coll_date is null, 'NA',if((x.lab_test = 163722 and timestampdiff(MONTH, x.date_test_requested, x.date_test_result_received) <= 1)\n"
		        + "           or(timestampdiff(MONTH, c.sample_coll_date, c.sample_result_date) <= 1), 'Y',\n"
		        + "          'N')) as results_given_within_a_month\n"
		        + "from kenyaemr_etl.etl_hei_enrollment e\n"
		        + "         left join (select x.patient_id,\n"
		        + "                           mid(max(concat(x.visit_date, x.lab_test)), 11)                  as lab_test,\n"
		        + "                           mid(max(concat(x.visit_date, x.date_test_result_received)), 11) as date_test_result_received,\n"
		        + "                           mid(max(concat(x.visit_date, x.date_test_requested)), 11)       as date_test_requested\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.visit_date <= date(:endDate)\n"
		        + "                      and x.lab_test = 163722\n"
		        + "                    group by x.patient_id) x on e.patient_id = x.patient_id\n"
		        + "         left join(select c.patient_id,\n"
		        + "                          coalesce(c.dna_pcr_sample_date, c.first_antibody_sample_date,\n"
		        + "                                   c.final_antibody_sample_date) as sample_coll_date,\n"
		        + "                          coalesce(c.dna_pcr_results_date, c.first_antibody_result_date,\n"
		        + "                                   c.final_antibody_sample_date) as sample_result_date\n"
		        + "                   from kenyaemr_etl.etl_hei_follow_up_visit c) c on e.patient_id = c.patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		queryBuilder.addParameter("endDate", endDate);
		queryBuilder.addParameter("startDate", startDate);
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
