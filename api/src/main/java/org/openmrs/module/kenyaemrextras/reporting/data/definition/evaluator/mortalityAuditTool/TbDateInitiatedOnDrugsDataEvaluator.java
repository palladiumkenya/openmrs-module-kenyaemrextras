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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbDateInitiatedOnDrugsDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbInitiatedOnDrugsDataDefinition;
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
 * TB Date Initiated on On Anti TB Evaluator
 */
@Handler(supports = TbDateInitiatedOnDrugsDataDefinition.class, order = 50)
public class TbDateInitiatedOnDrugsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "  coalesce(date(de.date_started) ,date(tb.visit_date)) as date_started_anti_TB\n"
		        + "from  kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = d.patient_id\n"
		        + "                                                                      and  disc.discontinuation_reason in (160432,160034)\n"
		        + "  left join kenyaemr_etl.etl_tb_screening tb on d.patient_id = tb.patient_id and tb.started_anti_TB = 1065\n"
		        + "  left join kenyaemr_etl.etl_drug_event de on de.patient_id = d.patient_id where de.program='TB'\n"
		        + "group by d.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
