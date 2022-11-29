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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsKpRecentVLTestOrderedDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsPedsRecentVLTestOrderedDataDefinition;
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
 * national guidelines for KPs
 */
@Handler(supports = SimsKpRecentVLTestOrderedDataDefinition.class, order = 50)
public class SimsKpRecentVLTestOrderedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id,\n"
		        + "IF(((lastVL is not null and (timestampdiff(YEAR,dob,:endDate))<25 and  timestampdiff(MONTH,dateTestOrdered, :endDate) <=6)\n"
		        + "or  (lastVL is not null  and (timestampdiff(YEAR,dob,:endDate))>24 and  timestampdiff(MONTH,dateTestOrdered, :endDate) <=12)\n"
		        + "), 'Y',\n"
		        + "if((lastVL is not null and (timestampdiff(YEAR,dob,:endDate))<25 and  timestampdiff(MONTH,dateTestOrdered, :endDate) > 6)\n"
		        + "or  (lastVL is not null  and (timestampdiff(YEAR,dob,:endDate))>24 and  timestampdiff(MONTH,dateTestOrdered, :endDate) > 12), 'N', 'N')) AS vl_eligibility\n"
		        + "from (\n" + "select d.patient_id, d.dob as dob,\n"
		        + "mid(max(concat(l.visit_date, l.test_result)), 11)  as lastVL,\n"
		        + "mid(max(concat(l.visit_date, l.date_test_requested)), 11)  as dateTestOrdered\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n" + "left join (\n"
		        + "select x.patient_id, x.visit_date ,x.test_result, x.date_test_requested\n"
		        + " from kenyaemr_etl.etl_laboratory_extract x\n" + " where  lab_test in (856, 1305)\n"
		        + " GROUP BY  x.patient_id\n" + ") l on d.patient_id = l.patient_id\n" + "group by d.patient_id\n" + ")t;\n";
		
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
