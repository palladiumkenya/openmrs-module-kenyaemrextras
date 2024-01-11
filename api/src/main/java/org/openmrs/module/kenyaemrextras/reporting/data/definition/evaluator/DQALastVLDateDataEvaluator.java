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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQALastVLDateDataDefinition;
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
 * Evaluates last VL date Data Definition
 */
@Handler(supports = DQALastVLDateDataDefinition.class, order = 50)
public class DQALastVLDateDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select x.patient_id,\n" +
				"       if(x.visit_date is not null and timestampdiff(DAY,enr.visit_date,date(:endDate)) >= 90,'Yes',\n" +
				"          if(x.visit_date is null and timestampdiff(DAY,enr.visit_date,date(:endDate)) >= 90,'No',\n" +
				"             if(timestampdiff(DAY,enr.visit_date,date(:endDate))  < 90,'NA', ''))) as vl_result_validity\n" +
				"from kenyaemr_etl.etl_laboratory_extract x\n" +
				"         inner join kenyaemr_etl.etl_hiv_enrollment enr on enr.patient_id= x.patient_id\n" +
				"where x.lab_test in (1305,856)\n" +
				"  and date(x.visit_date) between date_sub(date(:endDate) , interval 12 MONTH) and date(:endDate)\n" +
				"GROUP BY x.patient_id;";
		
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
