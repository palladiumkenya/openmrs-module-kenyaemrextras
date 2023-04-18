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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.RecentInvalidVLResultDataDefinition;
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
 * Recent invalid VL result Data Definition
 */
@Handler(supports = RecentInvalidVLResultDataDefinition.class, order = 50)
public class RecentInvalidVLResultDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if(a.vl_result >= 1000, '>=1000',if(a.vl_result = 'LDL' or a.vl_result < 1000,'<1000','')) as invalid_recent_vl_result\n"
		        + "from (select b.patient_id,\n"
		        + "             b.latest_visit_date,\n"
		        + "             b.vl_result,\n"
		        + "             b.urgency as urgency,\n"
		        + "             b.death_date,\n"
		        + "             b.vl_result_date\n"
		        + "      from (select x.patient_id                                   as patient_id,\n"
		        + "                   max(x.visit_date)                              as latest_visit_date,\n"
		        + "                   mid(max(concat(x.visit_date, x.lab_test)), 11) as lab_test,\n"
		        + "                   mid(max(concat(x.visit_date, x.urgency)), 11)  as urgency,\n"
		        + "                   if(mid(max(concat(x.visit_date, x.lab_test)), 11) = 856,\n"
		        + "                      mid(max(concat(x.visit_date, x.test_result)), 11) , if(\n"
		        + "                                      mid(max(concat(x.visit_date, x.lab_test)), 11) = 1305 and\n"
		        + "                                      mid(max(concat(x.visit_date, x.test_result)), 11) = 1302, 'LDL',\n"
		        + "                                      ''))                        as vl_result,\n"
		        + "                   if(mid(max(concat(x.visit_date, x.lab_test)), 11) in (856,1305),\n"
		        + "                      left(max(concat(coalesce(x.date_test_result_received,x.visit_date), x.test_result)), 10),\n"
		        + "                      '')                        as vl_result_date,\n"
		        + "                   date(coalesce(d.date_died, d.effective_discontinuation_date,\n"
		        + "                                 d.visit_date))                   as death_date\n"
		        + "            from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                     inner join kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                                on x.patient_id = d.patient_id and d.discontinuation_reason = 160034\n"
		        + "            where x.lab_test in (1305, 856)\n" + "            group by x.patient_id) b\n"
		        + "      where death_date between date(:startDate) and date(:endDate)\n" + "      group by b.patient_id\n"
		        + "      having timestampdiff(MONTH, b.latest_visit_date, b.death_date) > 12) a;";
		
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
