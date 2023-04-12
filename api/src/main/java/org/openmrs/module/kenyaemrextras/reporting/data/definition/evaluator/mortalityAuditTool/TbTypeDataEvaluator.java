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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbDateOfDiagnosisDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbTypeDataDefinition;
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
 * TB Type Evaluator
 */
@Handler(supports = TbTypeDataDefinition.class, order = 50)
public class TbTypeDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select en.patient_id,\n"
		        + "  (case disease_classification when 42 then \"Pulmonary TB\"\n"
		        + "                               when 5042 then \"Extra-Pulmonary TB\" else \"\" end) as tb_type\n"
		        + "from  kenyaemr_etl.etl_tb_enrollment en\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = en.patient_id\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = en.patient_id\n"
		        + "                                                                      and  disc.discontinuation_reason in (160432,160034)\n"
		        + "group by en.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
