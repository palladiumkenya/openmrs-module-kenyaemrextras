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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TBInvestigationsDoneAfterPresumedTBDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TBInvestigationsDoneDataDefinition;
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
 * Evaluates TB Investigations Data Definition
 */
@Handler(supports = TBInvestigationsDoneAfterPresumedTBDataDefinition.class, order = 50)
public class TBInvestigationsDoneAfterPresumedTBDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select s.patient_id,\n"
		        + "       if(tb_results = 142177 and l.patient_id is not null and l.tb_test_date >= s.tb_screening_date, 'Yes',\n"
		        + "          'No') as tb_investigations_done\n"
		        + "from (select a.patient_id, a.tb_results, a.max_visit as tb_screening_date\n"
		        + "      from (select max(tb.visit_date)                                                           as max_visit,\n"
		        + "                   tb.patient_id,\n"
		        + "                   mid(max(concat(date(tb.visit_date), ifnull(tb.resulting_tb_status, 0))), 11) as tb_results,\n"
		        + "                   mid(max(concat(date(tb.visit_date), ifnull(tb.person_present, 0))), 11)      as person_present\n"
		        + "            from kenyaemr_etl.etl_tb_screening tb\n"
		        + "            group by tb.patient_id) a\n"
		        + "      where a.person_present != 161642) s\n"
		        + "         left join (select l.patient_id, coalesce(max(l.date_test_requested), max(l.visit_date)) as tb_test_date\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract l\n"
		        + "                    where l.lab_test in (162202, 307, 1465)\n"
		        + "                    group by l.patient_id) l on s.patient_id = l.patient_id;";
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
