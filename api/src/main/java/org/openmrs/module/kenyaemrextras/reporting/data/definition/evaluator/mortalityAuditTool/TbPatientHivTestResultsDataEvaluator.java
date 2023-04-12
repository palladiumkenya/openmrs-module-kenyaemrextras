/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.mortalityAuditTool;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbPatientHivTestDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbPatientHivTestResultsDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * TB Patient Hiv Test Results Evaluator
 */
@Handler(supports = TbPatientHivTestResultsDataDefinition.class, order = 50)
public class TbPatientHivTestResultsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "  if(tf.hiv_status is not null,(case when tf.hiv_status = 703 then 'Positive'\n"
		        + "                                     when tf.hiv_status = 664 then 'Negative'\n"
		        + "                                     when tf.hiv_status = 664 then 'Negative'else '' end),\n"
		        + "     if(ht.patient_id is not null,ht.final_test_result,'')) as hiv_results\n"
		        + "from  kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = d.patient_id\n"
		        + "                                                                      and  disc.discontinuation_reason in (160432,160034)\n"
		        + "  left join kenyaemr_etl.etl_tb_follow_up_visit tf on tf.patient_id = d.patient_id\n"
		        + "  left join kenyaemr_etl.etl_hts_test ht on ht.patient_id = d.patient_id\n" + "group by d.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
