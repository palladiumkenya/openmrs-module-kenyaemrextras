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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQABaselineScreeningCrAGDataDefinition;
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
 * Evaluates Clients has  baseline CrAG after CD4 < 200 results Data Definition Evaluator
 */
@Handler(supports = DQABaselineScreeningCrAGDataDefinition.class, order = 50)
public class DQABaselineScreeningCrAGDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n" +
				"       if(a.baseline_cd4 = '<=200'  or a.baseline_cd4 <= 200 and a.crag_results_date >= a.baseline_cd4_results_date,'Yes',\n" +
				"          if(a.baseline_cd4 = '<=200'  or a.baseline_cd4 <= 200 and a.crag_results_date <= a.baseline_cd4_results_date,'No',\n" +
				"             if(a.baseline_cd4 = '<=200'  or a.baseline_cd4 <= 200 and a.crag_results_date is null,'No',\n" +
				"                if(a.baseline_cd4 = '>200'  or a.baseline_cd4 > 200 and a.crag_results_date is null,'NA',\n" +
				"                   if(a.baseline_cd4 is null,'No CD4 documented',''))))) as crag_test_done\n" +
				"\n" +
				"         from (select f.patient_id               as patient_id,\n" +
				"             min(f.visit_date)                   as fup_date,\n" +
				"             cd4.baseline_cd4_results_date,\n" +
				"             cd4.baseline_cd4,\n" +
				"             crag.crag_results_date,\n" +
				"             crag.patient_id as crag_patient,\n" +
				"             crag.crag_test_results\n" +
				"      from kenyaemr_etl.etl_patient_hiv_followup f\n" +
				"               left join\n" +
				"           (select patient_id,\n" +
				"                   left(min(concat(coalesce(date(date_test_result_received), date(visit_date)),\n" +
				"                                   if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n" +
				"                                                                       if(lab_test = 167718 and test_result = 167717, '<=200', ''))),\n" +
				"                                   '')),\n" +
				"                        10) as baseline_cd4_results_date,\n" +
				"                   mid(min(concat(coalesce(date(date_test_requested), date(visit_date)),\n" +
				"                                  if(lab_test = 5497, test_result, if(lab_test = 167718 and test_result = 1254, '>200',\n" +
				"                                                                      if(lab_test = 167718 and test_result = 167717,\n" +
				"                                                                         '<=200',\n" +
				"                                                                         if(lab_test = 730, concat(test_result, '%'), '')))),\n" +
				"                                  '')),\n" +
				"                       11)  as baseline_cd4\n" +
				"            from kenyaemr_etl.etl_laboratory_extract\n" +
				"            where lab_test in (167718, 5497)\n" +
				"            GROUP BY patient_id) cd4 on f.patient_id = cd4.patient_id\n" +
				"      left join (select patient_id,\n" +
				"                        left(min(concat(coalesce(date(date_test_result_received), date(visit_date)),test_result)),10) as crag_results_date,\n" +
				"                        mid(min(concat(coalesce(date(date_test_requested), date(visit_date)),test_result)),11)  as crag_test_results\n" +
				"                 from kenyaemr_etl.etl_laboratory_extract\n" +
				"                 where lab_test = 167452                GROUP BY patient_id\n" +
				"      ) crag on f.patient_id = crag.patient_id     group by f.patient_id) a;\n";
		
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
