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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbPatientSourceDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbTreatmentOutcomeAtDeathDataDefinition;
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
 * TB Treatment outcome at death Evaluator
 */
@Handler(supports = TbTreatmentOutcomeAtDeathDataDefinition.class, order = 50)
public class TbTreatmentOutcomeAtDeathDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n" + "  (case discontinuation_reason when 160031 then 'Defaulted'\n"
		        + "                                 when 160737 then 'Active on treatment'\n"
		        + "                                 when 160035 then 'Treatment Completed(no smear result)'\n"
		        + "                                 when 159791 then 'Cured(Smear- Negative)'\n"
		        + "                                 when 159874 then 'Failure(Smear- Positive)'\n"
		        + "                                 when 160034 then 'Dead'\n"
		        + "                                 when 5240 then 'Lost to followup'\n"
		        + "                                 when 159492 then 'Transferred Out' else '' end) as treatment_outcome\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation disc\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = disc.patient_id\n"
		        + "group by disc.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
