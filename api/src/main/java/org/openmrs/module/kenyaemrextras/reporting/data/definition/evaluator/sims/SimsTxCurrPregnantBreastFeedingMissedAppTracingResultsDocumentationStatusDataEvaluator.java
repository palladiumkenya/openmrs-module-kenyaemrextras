/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrPregnantBreastFeedingMissedAppTracingResultsDocumentationStatusDataDefinition;
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
 * Evaluates whether Tx_new pregnant or breastfeeding mothers who missed their last appointment and
 * traced had tracing results documented
 */
@Handler(supports = SimsTxCurrPregnantBreastFeedingMissedAppTracingResultsDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrPregnantBreastFeedingMissedAppTracingResultsDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(a.tracking_date > a.latest_app_date and a.tracing_outcome <> '', 'Y', 'N')\n"
		        + "from (select f.patient_id,\n"
		        + "             max(date(f.visit_date))                                     as latest_fup_visit,\n"
		        + "             mid(max(concat(f.visit_date, f.next_appointment_date)), 11) as latest_app_date,\n"
		        + "             t.tracking_date,\n"
		        + "             t.tracing_outcome\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "               left join (select t.patient_id,\n"
		        + "                                 max(t.visit_date)                                     as tracking_date,\n"
		        + "                                 mid(max(concat(t.visit_date, t.tracing_outcome)), 11) as tracing_outcome\n"
		        + "                          from kenyaemr_etl.etl_ccc_defaulter_tracing t\n"
		        + "                          where t.visit_date <= date(:endDate)\n"
		        + "                          group by t.patient_id) t on f.patient_id = t.patient_id\n"
		        + "      where f.visit_date <= date(:endDate)\n" + "      group by f.patient_id) a;";
		
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
