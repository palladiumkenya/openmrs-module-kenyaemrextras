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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxNewKPARTInitiationDocumentationStatusDataDefinition;
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
 * Evaluates whether TX_CURR KPs who are new HIV Positives were initiated to ART immediately or not
 */
@Handler(supports = SimsTxNewKPARTInitiationDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxNewKPARTInitiationDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(a.date_confirmed_hiv_positive = a.date_started_tx, 'Y', 'N') as started_Tx_Same_day\n"
		        + "from (select e.patient_id                                                            as patient_id,\n"
		        + "             mid(max(concat(date(e.visit_date), e.date_confirmed_hiv_positive)), 11) as date_confirmed_hiv_positive,\n"
		        + "             d.date_started                                                          as date_started_tx\n"
		        + "      from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "               left join (select d.patient_id, min(date(d.date_started)) as date_started\n"
		        + "                          from kenyaemr_etl.etl_drug_event d\n"
		        + "                          where d.visit_date <= date(:endDate) and d.program = 'HIV' group by d.patient_id) d on e.patient_id = d.patient_id\n"
		        + "      where date(e.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "      group by e.patient_id) a;";
		
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
