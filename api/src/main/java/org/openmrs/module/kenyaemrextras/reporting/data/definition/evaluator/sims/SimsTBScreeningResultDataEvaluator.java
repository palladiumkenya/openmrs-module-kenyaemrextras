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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTBScreeningResultDataDefinition;
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
 * Evaluates whether TB screening results documented at the last clinical assessment
 */
@Handler(supports = SimsTBScreeningResultDataDefinition.class, order = 50)
public class SimsTBScreeningResultDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "SELECT patient_id, mid(max(concat(visit_date, (case lastTBStatus when 1660 then \"No TB Signs\" when 142177 then \"Presumed TB\" when 1662 then \"TB Confirmed\" when 160737 then \"TB Screening Not Done\"  else null end ))), 11) as lastTBStatus\n"
		        + "                from\n"
		        + "            (\n"
		        + "                SELECT\n"
		        + "                    f.patient_id,\n"
		        + "                    f.visit_date,\n"
		        + "                    coalesce(f.tb_status, s.resulting_tb_status) AS lastTBStatus\n"
		        + "                FROM kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                    LEFT OUTER JOIN kenyaemr_etl.etl_tb_screening s\n"
		        + "                        ON s.patient_id = f.patient_id AND date(s.visit_date) = date(f.visit_date)\n"
		        + "                GROUP BY f.patient_id\n" + "            ) v\n" + "            GROUP BY patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
