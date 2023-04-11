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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.PresumtiveTBDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates Presumptive TB Data Definition
 */
@Handler(supports = PresumtiveTBDataDefinition.class, order = 50)
public class PresumptiveTBDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(timestampdiff(MONTH, date(a.tb_status_date), date(a.death_date)) <= 12, 'Yes', 'No') as had_presumptive_TB\n"
		        + "from (select d.patient_id,\n"
		        + "             date(coalesce(d.date_died, d.effective_discontinuation_date,\n"
		        + "                           d.visit_date)) death_date,\n"
		        + "             s.tb_status_date\n"
		        + "      from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "               left join\n"
		        + "           (select s.patient_id, s.resulting_tb_status, s.visit_date as tb_status_date\n"
		        + "            from kenyaemr_etl.etl_tb_screening s\n"
		        + "            where s.resulting_tb_status = 142177) s on d.patient_id = s.patient_id\n"
		        + "      where d.discontinuation_reason = 160034) a;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
