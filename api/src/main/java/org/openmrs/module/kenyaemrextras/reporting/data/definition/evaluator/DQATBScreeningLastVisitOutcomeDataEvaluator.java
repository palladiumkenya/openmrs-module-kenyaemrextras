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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQATBScreeningLastVisitDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQATBScreeningLastVisitOutcomeDataDefinition;
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
 * Evaluates TB screening outcome on last visit Data Definition
 */
@Handler(supports = DQATBScreeningLastVisitOutcomeDataDefinition.class, order = 50)
public class DQATBScreeningLastVisitOutcomeDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "    if(person_present = 978 and screened_for_tb = 'Yes',case a.tb_status when 1660 then 'No TB signs' when 1662 then 'TB Confirmed' when 142177 then 'Presumed TB' end,'Missing') as tb_status\n"
		        + "    from (select tb.patient_id,\n"
		        + "    mid(max(concat(date (tb.visit_date), ifnull(tb.tb_status, 0))), 11) as tb_status,\n"
		        + "    mid(max(concat(date (tb.visit_date),tb.person_present)),11) as person_present,\n"
		        + "    mid(max(concat(date (tb.visit_date), tb.screened_for_tb)), 11) as screened_for_tb\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup tb where tb.visit_date <= date (:endDate)\n"
		        + "    group by tb.patient_id) a;";
		
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
