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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsFollowUpVLTakenDataDefinition;
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
 * Evaluates whether a patient had follow up vl taken where the most recent vl is >= 1000 copies
 */
@Handler(supports = SimsFollowUpVLTakenDataDefinition.class, order = 50)
public class SimsFollowUpVLTakenDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = " select t.patient_id,if ((timestampdiff(MONTH,t.dateResultReceived,t.date_test_requested) <=6), 'Y', 'N') from (   \n"
		        + "       select x.patient_id as patient_id,x.visit_date as visit_date,an.order_reason ,an.date_test_requested,\n"
		        + "       x.lab_test as lab_test, \n"
		        + "       mid(max(concat(x.visit_date,x.test_result)),11) as test_result,\n"
		        + "\t   mid(max(concat(x.visit_date,x.date_test_result_received)),11) as dateResultReceived\n"
		        + "       from kenyaemr_etl.etl_laboratory_extract x \n"
		        + "       left join (\n"
		        + "        select a.patient_id as patientId,date(max(a.visit_date)) as visitDate, \n"
		        + "        a.order_reason,\n"
		        + "        \t   mid(max(concat(a.visit_date,a.date_test_requested)),11) as date_test_requested\n"
		        + "        from kenyaemr_etl.etl_laboratory_extract a \n"
		        + "        where a.visit_date <= date(:endDate) and a.order_reason = 843\n"
		        + "        GROUP BY a.patient_id\n"
		        + "       ) an on x.patient_id = an.patientId\n"
		        + "       where x.lab_test in (856,1305) \n"
		        + "       group by x.patient_id,x.visit_date \n"
		        + "       order by visit_date desc ) t\n" + "       where date(t.dateResultReceived) <= date(:endDate) ";
		
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
