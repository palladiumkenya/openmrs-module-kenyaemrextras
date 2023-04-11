/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiImmunizationStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiNutritionalAssessmentDataDefinition;
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
 * Evaluates Nutritional Assessment DataDefinition
 */
@Handler(supports = HeiNutritionalAssessmentDataDefinition.class, order = 50)
public class HeiNutritionalAssessmentDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select  hf.patient_id,\n"
		        + "  (case max(hf.weight_category) when 123814 then \"Underweight\"\n"
		        + "                        when 126598 then \"Malnourished,\"\n"
		        + "                        when 114413 then \"Overweight\"\n"
		        + "                        when 115115 then \"Overweight\"\n"
		        + "                        when 1115 then \"Normal Growth\" else \"Not done\" end) as nutritional_assessment\n"
		        + "from kenyaemr_etl.etl_hei_follow_up_visit hf\n"
		        + "  inner join  kenyaemr_etl.etl_patient_demographics d on d.patient_id = hf.patient_id\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = hf.patient_id\n"
		        + "where  disc.program_name in ('MCH Child HEI','MCH Child') and disc.discontinuation_reason = 160432\n"
		        + "group by hf.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
