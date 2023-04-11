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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.DurationOnARTDataDefinition;
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
 * Evaluates DurationOnARTDataDefinition
 */
@Handler(supports = DurationOnARTDataDefinition.class, order = 50)
public class DurationOnARTDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "\n"
		        + "select d.patient_id,\n"
		        + "        if(TIMESTAMPDIFF(MONTH, date(de.art_start_date), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))) < 6,\n"
		        + "          '<6 months',\n"
		        + "           if(TIMESTAMPDIFF(MONTH, date(de.art_start_date), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))) between 6 and 12,'6-12 months',\n"
		        + "               if(TIMESTAMPDIFF(MONTH, date(de.art_start_date), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))) > 12,'>1 year',null)))\n"
		        + "           as duration_of_art\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "         inner join kenyaemr_etl.etl_patient_program_discontinuation x\n"
		        + "                    on d.patient_id = x.patient_id and x.discontinuation_reason = 160034\n"
		        + "inner join (select de.patient_id,\n"
		        + "                   coalesce(mid(min(concat(date(enr.visit_date), date(enr.date_started_art_at_transferring_facility))), 11),\n"
		        + "                            mid(min(concat(date(de.visit_date), date(de.date_started))), 11)) as art_start_date\n"
		        + "            from kenyaemr_etl.etl_drug_event de\n"
		        + "                     left outer join kenyaemr_etl.etl_hiv_enrollment enr on enr.patient_id = de.patient_id\n"
		        + "            where de.program = 'HIV'\n"
		        + "            GROUP BY de.patient_id)de on d.patient_id = de.patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
