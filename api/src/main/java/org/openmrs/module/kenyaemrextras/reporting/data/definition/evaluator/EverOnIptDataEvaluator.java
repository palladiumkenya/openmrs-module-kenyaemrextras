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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.EverOnIPTDataDefinition;
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
 * Evaluates ever on IPT Data Definition
 */
@Handler(supports = EverOnIPTDataDefinition.class, order = 50)
public class EverOnIptDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if(a.on_tb = 1065, 'NA', if(a.ipt_patient is not null, 'Yes', 'No')) as tpt_status\n"
		        + "from (select fup.patient_id,\n"
		        + "             i.patient_id                                                     as ipt_patient,\n"
		        + "             mid(max(concat(date(fup.visit_date), fup.on_anti_tb_drugs)), 11) as on_tb\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "               left join (select i.patient_id\n"
		        + "                          from kenyaemr_etl.etl_ipt_initiation i\n"
		        + "                          where i.visit_date <= date(:endDate)) i on fup.patient_id = i.patient_id\n"
		        + "      where fup.visit_date <= date(:endDate)\n" + "      group by fup.patient_id) a;";
		
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
