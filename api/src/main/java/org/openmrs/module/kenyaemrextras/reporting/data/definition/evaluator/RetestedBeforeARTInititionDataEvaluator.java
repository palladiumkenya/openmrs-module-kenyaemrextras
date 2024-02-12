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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQATBScreeningLastVisitOutcomeDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.RetestedBeforeARTInititionDataDefinition;
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
 * Evaluates Retested Before ART Initition Data Definition
 */
@Handler(supports = RetestedBeforeARTInititionDataDefinition.class, order = 50)
public class RetestedBeforeARTInititionDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if (retestDate is null or hts_test_result = 'Negative','NA', if (test_type = 2 and hts_test_result = 'Positive' and retestDate <= enr_date ,'Y','N' )) as retested_before_enrollment from (\n"
		        + "select a.patient_id, x.enr_date,h.retestDate,h.hts_test_result,h.test_type from kenyaemr_etl.etl_patient_demographics a\n"
		        + "left outer join (\n"
		        + "select t.patient_id, max(t.visit_date) as retestDate, mid(max(concat(date (t.visit_date),t.final_test_result)),11) as hts_test_result,\n"
		        + "mid(max(concat(date (t.visit_date),t.test_type)),11) as test_type from kenyaemr_etl.etl_hts_test t where date (t.visit_date) <= date (:endDate)\n"
		        + "GROUP BY t.patient_id\n"
		        + ") h on a.patient_id = h.patient_id left outer join (\n"
		        + "select d.patient_id, min(d.visit_date) as enr_date from kenyaemr_etl.etl_hiv_enrollment d\n"
		        + "GROUP BY d.patient_id ) x on a.patient_id = x.patient_id GROUP BY a.patient_id) a\n"
		        + "group by patient_id;\n";
		
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
