/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsRecentVLTestOrderedDataDefinition;
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
 * Evaluates whether most recent viral load test was ordered within the appropriate interval per the
 * national guidelines
 */
@Handler(supports = SimsRecentVLTestOrderedDataDefinition.class, order = 50)
public class SimsRecentVLTestOrderedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id, if((lastVL is not null  and (lastVL > 1000 and lastVL!=1302 and timestampdiff(MONTH,dateTestOrdered, :endDate) <= 3)\n"
		        + "  or  lastVL is not null  and (lastVL < 1000 or lastVL=1302) and (timestampdiff(YEAR,dob,:endDate))<25 and  timestampdiff(MONTH,dateTestOrdered, :endDate) <6\n"
		        + "  or  lastVL is not null  and (lastVL < 1000 or lastVL=1302) and (timestampdiff(YEAR,dob,:endDate))>25 and  (timestampdiff(MONTH,dateTestOrdered, :endDate) <= 12)),'Y','N') from (\n"
		        + "select d.patient_id, d.dob as dob,\n"
		        + "mid(max(concat(l.visit_date, l.test_result)), 11)  as lastVL,\n"
		        + "mid(max(concat(l.visit_date, l.date_test_requested)), 11)  as dateTestOrdered\n"
		        + "\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "left join (\n"
		        + "select x.patient_id, x.visit_date ,x.test_result, x.date_test_requested\n"
		        + "  from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "  where  lab_test in (856, 1305)\n"
		        + "  GROUP BY  x.patient_id\n"
		        + "\n"
		        + ") l on d.patient_id = l.patient_id\n"
		        + "group by d.patient_id\n"
		        + ")t\n"
		        + "where (\n"
		        + "  t.lastVL is not null  and (lastVL > 1000 and lastVL!=1302 and timestampdiff(MONTH,t.dateTestOrdered, :endDate) <= 3)\n"
		        + "  or  t.lastVL is not null  and (lastVL < 1000 or lastVL=1302) and (timestampdiff(YEAR,t.dob,:endDate))<25 and  timestampdiff(MONTH,t.dateTestOrdered, :endDate) <6\n"
		        + "  or  t.lastVL is not null  and (lastVL < 1000 or lastVL=1302) and (timestampdiff(YEAR,t.dob,:endDate))>25 and  (timestampdiff(MONTH,t.dateTestOrdered, :endDate) <= 12)\n"
		        + "\n" + ")";
		
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
