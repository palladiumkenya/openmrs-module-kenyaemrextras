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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.CD4RecencyDataDefinition;
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
 * Evaluates CD4 recency Data Definition
 */
@Handler(supports = CD4RecencyDataDefinition.class, order = 50)
public class CD4RecencyDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select l.patient_id,\n"
		        + "       if(l.baseline_cd4 = l.latest_cd4 and l.baseline_cd4_date = l.latest_cd4_date,\n"
		        + "          'No baseline is the most recent CD4',\n"
		        + "          if(l.baseline_cd4 != l.latest_cd4 and l.baseline_cd4_date != l.latest_cd4_date,\n"
		        + "             'Yes, there is a more recent count','')) as has_recent_cd4\n"
		        + "from (select patient_id,\n"
		        + "             mid(min(concat(coalesce(date(date_test_requested), date(visit_date)),\n"
		        + "                            if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n"
		        + "                                                                if(lab_test = 167718 and test_result = 167717, '<=200',\n"
		        + "                                                                   if(lab_test = 730, concat(test_result, '%'), '')))),\n"
		        + "                            '')),\n"
		        + "                 11)  as baseline_cd4,\n"
		        + "             mid(max(concat(coalesce(date(date_test_requested), date(visit_date)),\n"
		        + "                            if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n"
		        + "                                                                if(lab_test = 167718 and test_result = 167717, '<=200',\n"
		        + "                                                                   if(lab_test = 730, concat(test_result, '%'), '')))),\n"
		        + "                            '')),\n"
		        + "                 11)  as latest_cd4,\n"
		        + "             left(min(concat(coalesce(date(date_test_requested), date(visit_date)),\n"
		        + "                             if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n"
		        + "                                                                 if(lab_test = 167718 and test_result = 167717, '<=200',\n"
		        + "                                                                    if(lab_test = 730, concat(test_result, '%'), '')))),\n"
		        + "                             '')),\n"
		        + "                  10) as baseline_cd4_date,\n"
		        + "             left(max(concat(coalesce(date(date_test_requested), date(visit_date)),\n"
		        + "                             if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n"
		        + "                                                                 if(lab_test = 167718 and test_result = 167717, '<=200',\n"
		        + "                                                                    if(lab_test = 730, concat(test_result, '%'), '')))),\n"
		        + "                             '')),\n" + "                  10) as latest_cd4_date\n"
		        + "      from kenyaemr_etl.etl_laboratory_extract\n" + "      where lab_test in (167718, 5497, 730)\n"
		        + "      GROUP BY patient_id) l;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
