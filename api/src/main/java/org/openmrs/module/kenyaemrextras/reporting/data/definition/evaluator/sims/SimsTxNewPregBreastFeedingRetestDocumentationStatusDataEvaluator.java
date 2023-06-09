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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxNewPregBreastFeedingRetestDocumentationStatusDataDefinition;
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
 * Evaluates whether Tx_new pregnant or breastfeeding mothers were retested prior to or before
 * initiation to ART
 */
@Handler(supports = SimsTxNewPregBreastFeedingRetestDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxNewPregBreastFeedingRetestDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "       if(de.date_started is not null, if(date(rt.retest_date) <= date(de.date_started), 'Y', 'N'), 'NA')\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "         left join (select de.patient_id, min(date(de.date_started)) as date_started\n"
		        + "                    from kenyaemr_etl.etl_drug_event de\n"
		        + "                    where de.program = 'HIV'\n"
		        + "                      and date(de.date_started) <= date(:endDate)\n"
		        + "                    group by de.patient_id) de\n"
		        + "                   on d.patient_id = de.patient_id\n"
		        + "         left join (select rt.patient_id, rt.visit_date as retest_date\n"
		        + "                    from kenyaemr_etl.etl_hts_test rt\n" + "                    where rt.test_type = 2\n"
		        + "                      and rt.final_test_result = 'Positive'\n"
		        + "                      and date(rt.visit_date) <= date(:endDate)) rt\n"
		        + "                   on d.patient_id = rt.patient_id;";
		
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
