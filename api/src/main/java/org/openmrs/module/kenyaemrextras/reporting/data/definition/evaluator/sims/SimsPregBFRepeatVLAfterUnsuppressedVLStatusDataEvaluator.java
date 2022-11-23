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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsPregBFRepeatVLAfterUnsuppressedVLStatusDataDefinition;
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
 * virologic non-suppression
 */
@Handler(supports = SimsPregBFRepeatVLAfterUnsuppressedVLStatusDataDefinition.class, order = 50)
public class SimsPregBFRepeatVLAfterUnsuppressedVLStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,d.a,if(IFNULL(d.rpt_vl_result_date,'0000-00-00') > IFNULL(d.high_vl_date,'0000-00-00'),'Y','N') from (select c.patient_id,a.patient_id a, a.rpt_vl_result_date as rpt_vl_result_date,c.vl_result_date as high_vl_date\n"
		        + "       from\n"
		        + "                   (select c.patient_id,\n"
		        + "                           max(c.visit_date)                                                       as vl_date,\n"
		        + "                           mid(max(concat(c.visit_date, c.lab_test)), 11)                          as lab_test,\n"
		        + "                           mid(max(concat(c.visit_date, c.order_reason)), 11)                      as order_reason,\n"
		        + "                           if(mid(max(concat(c.visit_date, c.lab_test)), 11) = 856, mid(max(concat(c.visit_date, c.test_result)), 11),\n"
		        + "                              if(mid(max(concat(c.visit_date, c.lab_test)), 11) = 1305 and\n"
		        + "                                 mid(max(concat(visit_date, test_result)), 11) = 1302, 'LDL', '')) as vl_result,\n"
		        + "                           mid(max(concat(date(visit_date), date(vl_results_date))), 11)  as vl_result_date\n"
		        + "                    from (select x.patient_id   as patient_id,\n"
		        + "                                 x.visit_date   as visit_date,\n"
		        + "                                 x.lab_test     as lab_test,\n"
		        + "                                 x.test_result  as test_result,\n"
		        + "                                 x.order_reason as order_reason,\n"
		        + "                                 x.date_test_result_received as vl_results_date\n"
		        + "                          from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                              /* where x.lab_test in (1305, 856) and order_reason != 843\n"
		        + "                         and x.test_result >= 1000*/\n"
		        + "                          group by x.patient_id, x.visit_date\n"
		        + "                          order by visit_date desc) c\n"
		        + "                    where c.lab_test in (1305, 856) and c.order_reason != 843\n"
		        + "                      and c.test_result >= 1000\n"
		        + "                    group by patient_id\n"
		        + "                    having vl_result_date between\n"
		        + "                               date_sub(date(:endDate), interval 12 MONTH) and date(:endDate)\n"
		        + "                   )c  left join\n"
		        + "            (select b.patient_id,\n"
		        + "                    max(b.visit_date)                                                       as vl_date,\n"
		        + "                    mid(max(concat(b.visit_date, b.lab_test)), 11)                          as lab_test,\n"
		        + "                    mid(max(concat(b.visit_date, b.order_reason)), 11)                      as order_reason,\n"
		        + "                    if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 856, mid(max(concat(b.visit_date, b.test_result)), 11),\n"
		        + "                       if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 1305 and\n"
		        + "                          mid(max(concat(visit_date, test_result)), 11) = 1302, 'LDL', '')) as vl_result,\n"
		        + "                          mid(max(concat(visit_date, test_result_date)), 11) as rpt_vl_result_date\n"
		        + "             from (select x.patient_id   as patient_id,\n"
		        + "                          x.visit_date   as visit_date,                    x.lab_test     as lab_test,\n"
		        + "                          x.test_result  as test_result,\n"
		        + "                          x.order_reason as order_reason,\n"
		        + "                          x.date_test_result_received as test_result_date\n"
		        + "                   from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                   where x.lab_test in (1305, 856)             group by x.patient_id, x.visit_date\n"
		        + "                   order by visit_date desc) b       group by patient_id      having rpt_vl_result_date between\n"
		        + "                 date_sub(date(:endDate), interval 12 MONTH) and date(:endDate)\n"
		        + "                  and order_reason = 843)a\n"
		        + "                    on c.patient_id = a.patient_id\n"
		        + "    )d;";
		
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
