/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.mortalityAuditTool;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.AgeAtDeathPatientDataDefinition;
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
 * Evaluates Age at Death Data Definition
 */
@Handler(supports = AgeAtDeathPatientDataDefinition.class, order = 50)
public class AgeAtDeathPatientDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "       if(TIMESTAMPDIFF(MONTH, date(d.dob), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))) < 2,\n"
		        + "          TIMESTAMPDIFF(MONTH, date(d.dob), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))),\n"
		        + "          TIMESTAMPDIFF(YEAR, date(d.dob), coalesce(date(x.date_died),date(x.effective_discontinuation_date), date(x.visit_date))))\n"
		        + "           as age_at_death\n" + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "         inner join kenyaemr_etl.etl_patient_program_discontinuation x\n"
		        + "                    on d.patient_id = x.patient_id and x.discontinuation_reason in (160432,160034);;";
		
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
