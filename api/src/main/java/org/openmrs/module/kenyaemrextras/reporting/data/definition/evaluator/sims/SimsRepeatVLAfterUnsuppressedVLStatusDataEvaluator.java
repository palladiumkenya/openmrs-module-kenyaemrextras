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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition;
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
 * Evaluates whether Pregnant and breastfeeding patients on ART ≥12 months with virologic
 * non-suppression have documentation of a follow-up viral load result after the first result of
 * virologic non-suppression;
 * KHP3:3819 Updated Query to accurately check if repeat VL exists and if so, was it done within 3 months after reporting of previous unsuppressed VL. <= 3 months = 'Y', > 3 months ='No' and missing VL = 'N/A'
 */
@Handler(supports = SimsRepeatVLAfterUnsuppressedVLStatusDataDefinition.class, order = 50)
public class SimsRepeatVLAfterUnsuppressedVLStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n" +
				"       if(d.rpt_vl_result_date is null,'N/A',if(timestampdiff(DAY,d.high_vl_date,d.rpt_vl_result_date) <= 90,\n" +
				"          'Y', 'N')) as repeat_vl_within_3_months\n" +
				"from (select c.patient_id,\n" +
				"             a.visit_date       as rept_vl_date,\n" +
				"             a.test_result_date as rpt_vl_result_date,\n" +
				"             c.vl_result_date   as high_vl_date\n" +
				"      from (select c.patient_id,\n" +
				"                   max(c.visit_date)                                             as vl_date,\n" +
				"                   mid(max(concat(c.visit_date, c.lab_test)), 11)                as lab_test,\n" +
				"                   mid(max(concat(c.visit_date, c.order_reason)), 11)            as order_reason,\n" +
				"                   mid(max(concat(c.visit_date, c.lab_test)), 11)                as vl_result,\n" +
				"                   mid(max(concat(date(visit_date), date(vl_results_date))), 11) as vl_result_date\n" +
				"            from (select x.patient_id                as patient_id,\n" +
				"                         x.visit_date                as visit_date,\n" +
				"                         x.lab_test                  as lab_test,\n" +
				"                         x.test_result               as test_result,\n" +
				"                         x.order_reason              as order_reason,\n" +
				"                         x.date_test_result_received as vl_results_date\n" +
				"                  from kenyaemr_etl.etl_laboratory_extract x\n" +
				"                  group by x.patient_id, x.visit_date\n" +
				"                  order by visit_date desc) c\n" +
				"            where c.lab_test = 856\n" +
				"              and c.order_reason != 843\n" +
				"              and c.test_result >= 1000\n" +
				"            group by patient_id, visit_date\n" +
				"            having vl_result_date between\n" +
				"                       date_sub(date(:endDate), interval 12 MONTH) and date(:endDate)) c\n" +
				"               left join\n" +
				"           (select x.patient_id                                  as patient_id,\n" +
				"                   coalesce(x.date_test_requested, x.visit_date) as visit_date,\n" +
				"                   x.lab_test                                    as lab_test,\n" +
				"                   x.test_result                                 as test_result,\n" +
				"                   x.order_reason                                as order_reason,\n" +
				"                   x.date_test_result_received                   as test_result_date\n" +
				"            from kenyaemr_etl.etl_laboratory_extract x\n" +
				"            where x.lab_test in (856, 1305)\n" +
				"              and order_reason = 843\n" +
				"            group by x.patient_id, x.visit_date) a\n" +
				"           on c.patient_id = a.patient_id and c.vl_result_date <= a.visit_date) d;";
		
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
