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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.HeiCareGiverOccupationDataDefinition;
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
 * Evaluates a HeiCareGiverOccupationDataDefinition
 */
@Handler(supports = HeiCareGiverOccupationDataDefinition.class, order = 50)
public class HeiCareGiverOccupationDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select distinct r.person_a,\n"
		        + "  d.occupation as careGiverRelationship\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join openmrs.relationship r on d.patient_id = r.person_b\n"
		        + "  inner join openmrs.relationship_type t on r.relationship = t.relationship_type_id\n"
		        + "                                            and t.uuid in ('3667e52f-8653-40e1-b227-a7278d474020','8d91a210-c2cc-11de-8d13-0010c6dffd0f','5f115f62-68b7-11e3-94ee-6bef9086de92','a8058424-5ddf-4ce2-a5ee-6e08d01b5960');";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
