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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.DiagnosedTBWithin12MonthsToDeathDataDefinition;
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
 * Evaluates DiagnosedTBWithin12MonthsToDeathDataDefinition
 */
@Handler(supports = DiagnosedTBWithin12MonthsToDeathDataDefinition.class, order = 50)
public class DiagnosedTBWithin12MonthsToDeathDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "       if(timestampdiff(MONTH, s.tb_diagnosis_date, x.date_died) <= 12 or\n"
		        + "          timestampdiff(MONTH, l.tb_test_date, x.date_died) <= 12, 'Yes',\n"
		        + "          'No') as diagnosed_with_tb_within_12_months_to_death\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "         inner join\n"
		        + "     (select x.patient_id,\n"
		        + "             coalesce(date(x.date_died),\n"
		        + "                      date(x.effective_discontinuation_date),\n"
		        + "                      date(x.visit_date)) as date_died\n"
		        + "      from kenyaemr_etl.etl_patient_program_discontinuation x\n"
		        + "      where x.discontinuation_reason = 160034 and x.program_name in ('HIV','TB')\n"
		        + "        and coalesce(date(x.date_died),\n"
		        + "                     date(x.effective_discontinuation_date),\n"
		        + "                     date(x.visit_date)) between date(:startDate) and date(:endDate)) x on d.patient_id = x.patient_id\n"
		        + "         left join (select s.patient_id,\n"
		        + "                           left(max(concat(s.visit_date, s.resulting_tb_status)), 10) as tb_diagnosis_date\n"
		        + "                    from kenyaemr_etl.etl_tb_screening s\n"
		        + "                    where s.resulting_tb_status = 1662\n"
		        + "                      and date(s.visit_date) >= date(:startDate)\n"
		        + "                    group by s.patient_id) s\n" + "                   on d.patient_id = s.patient_id\n"
		        + "         left join (select l.patient_id,\n"
		        + "                           coalesce(left(max(concat(l.date_test_requested, l.lab_test)), 10),\n"
		        + "                                    left(max(concat(l.visit_date, l.lab_test)), 10)) as tb_test_date\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract l\n"
		        + "                    where l.lab_test in (162202, 307, 1465)\n"
		        + "                    group by l.patient_id) l\n" + "                   on d.patient_id = l.patient_id;";
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
