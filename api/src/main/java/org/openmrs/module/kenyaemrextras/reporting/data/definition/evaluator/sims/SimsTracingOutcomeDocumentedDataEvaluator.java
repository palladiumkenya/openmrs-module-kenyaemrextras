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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTracingOutcomeDocumentedDataDefinition;
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
 * Evaluates tracing outcome documented during a reporting period
 */
@Handler(supports = SimsTracingOutcomeDocumentedDataDefinition.class, order = 50)
public class SimsTracingOutcomeDocumentedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,if(tracing_outcome is not null and tracing_outcome > 0 and numberOfTracing > 1 ,'Y','N') from (\n"
		        + "    select e.patient_id,t.tracing_outcome,t.tracingDate, t.numberOfTracing from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "    left outer join (\n"
		        + "        select trace.patient_id as patient_id,count(trace.visit_date) as numberOfTracing,\n"
		        + "        mid(max(concat(trace.visit_date, trace.true_status)), 11) as tracing_outcome,\n"
		        + "        max(trace.visit_date) as tracingDate,v.latest_app_date from kenyaemr_etl.etl_ccc_defaulter_tracing trace \n"
		        + "        join (\n"
		        + "        select patient_id, mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11) as latest_app_date\n"
		        + "        from kenyaemr_etl.etl_patient_hiv_followup fup    where fup.visit_date <= date(:endDate) \n"
		        + "        group by fup.patient_id   ) v on trace.patient_id = v.patient_id\n"
		        + "        where  date(trace.visit_date) between latest_app_date and date(:endDate) \n"
		        + "        GROUP BY trace.patient_id ) t on e.patient_id = t.patient_id\n"
		        + "    where e.visit_date <= date(:endDate) group by e.patient_id )a ";
		
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
