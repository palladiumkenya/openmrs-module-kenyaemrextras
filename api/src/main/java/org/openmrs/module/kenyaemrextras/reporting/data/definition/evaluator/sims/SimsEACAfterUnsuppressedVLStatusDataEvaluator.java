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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsEACAfterUnsuppressedVLStatusDataDefinition;
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
 * non-suppression have documentation of at least 1 EAC session after the date of virologic
 * non-suppression.
 * KHP3-3818: Updated query to exclude orders whose result is ldl and checking for first EAC after Last unsuppressed VL result. Removed end date cap for EACs.
 */
@Handler(supports = SimsEACAfterUnsuppressedVLStatusDataDefinition.class, order = 50)
public class SimsEACAfterUnsuppressedVLStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(vl_result > 1000 and vl_result_date <= eac_date, 'Y', 'N') as valid_eac\n" +
				"from (select b.patient_id,\n" +
				"             max(b.visit_date)                                                       as vl_date,\n" +
				"             mid(max(concat(b.visit_date, b.lab_test)), 11)                          as lab_test,\n" +
				"             mid(max(concat(b.visit_date, b.order_reason)), 11)                      as order_reason,\n" +
				"             if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 856, mid(max(concat(b.visit_date, b.test_result)), 11),\n" +
				"                if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 1305 and\n" +
				"                   mid(max(concat(visit_date, test_result)), 11) = 1302, 'LDL', '')) as vl_result,\n" +
				"             mid(max(concat(date(visit_date), date(vl_results_date))), 11)           as vl_result_date,\n" +
				"             e.patient_id                                                            as eac_patient,\n" +
				"             e.eac_date                                                              as eac_date\n" +
				"      from (select x.patient_id                as patient_id,\n" +
				"                   x.visit_date                as visit_date,\n" +
				"                   x.lab_test                  as lab_test,\n" +
				"                   x.test_result               as test_result,\n" +
				"                   x.order_reason              as order_reason,\n" +
				"                   x.date_test_result_received as vl_results_date\n" +
				"            from kenyaemr_etl.etl_laboratory_extract x\n" +
				"            where x.lab_test = 856\n" +
				"              and order_reason != 843\n" +
				"            group by x.patient_id, x.visit_date\n" +
				"            order by visit_date desc) b\n" +
				"               left join (select e.patient_id, e.visit_date as eac_date\n" +
				"                          from kenyaemr_etl.etl_enhanced_adherence e) e\n" +
				"                         on b.patient_id = e.patient_id and b.vl_results_date <= e.eac_date\n" +
				"      group by patient_id\n" +
				"      having vl_date between\n" +
				"                 date_sub(date(:endDate), interval 12 MONTH) and date(:endDate)) a\n" +
				"where a.vl_result >= 1000\n" +
				"  and (a.eac_date >= vl_result_date or eac_date is null)\n" +
				"group by patient_id;";
		
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
