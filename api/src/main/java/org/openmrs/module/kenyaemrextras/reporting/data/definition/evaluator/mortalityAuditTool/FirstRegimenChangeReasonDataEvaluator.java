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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.FirstRegimenChangeReasonDataDefinition;
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
 * Evaluates FirstRegimenChangeReasonDataDefinition
 */
@Handler(supports = FirstRegimenChangeReasonDataDefinition.class, order = 50)
public class FirstRegimenChangeReasonDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id,\n"
		        + "        mid(min(concat(date(date_started),coalesce((case reason_discontinued when 102 then \"Drug toxicity\" when 160567 then \"New diagnosis of Tuberculosis\"  when 160569 then \"Virologic failure\"\n"
		        + "                                                                         when 159598 then \"Non-compliance with treatment or therapy\" when 1754 then \"Medications unavailable\"\n"
		        + "                                                                         when 1434 then \"Currently pregnant\"  when 1253 then \"Completed PMTCT\"  when 843 then \"Regimen failure\"\n"
		        + "                                                                         when 5622 then \"Other\" when 160559 then \"Risk of pregnancy\" when 160561 then \"New drug available\" else \"\" end),reason_discontinued_other))), 11) as discontinuation_reason\n"
		        + "   from kenyaemr_etl.etl_drug_event where program = 'HIV' GROUP BY patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
