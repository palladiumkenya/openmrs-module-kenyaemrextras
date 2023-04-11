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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.BaselineWHOStageDateDataDefinition;
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
 * Evaluates Date of HIV diagnosis Data Definition
 */
@Handler(supports = BaselineWHOStageDateDataDefinition.class, order = 50)
public class BaselineWHOStageDateDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id, if(f.initial_who_stage is not null, f.first_hiv_fup_date, null) as baseline_WHO_date\n"
		        + "from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "         inner join (select f.patient_id,\n"
		        + "                            min(date(f.visit_date))                                                         as first_hiv_fup_date,\n"
		        + "                            mid(min(concat(date(f.visit_date), (case f.who_stage\n"
		        + "                                                                    when 1204 then 'WHO Stage1'\n"
		        + "                                                                    when 1220 then 'WHO Stage1'\n"
		        + "                                                                    when 1205 then 'WHO Stage2'\n"
		        + "                                                                    when 1221 then 'WHO Stage2'\n"
		        + "                                                                    when 1206 then 'WHO Stage3'\n"
		        + "                                                                    when 1222 then 'WHO Stage3'\n"
		        + "                                                                    when 1207 then 'WHO Stage4'\n"
		        + "                                                                    when 1223 then 'WHO Stage4' end))),\n"
		        + "                                11)                                                                         as initial_who_stage\n"
		        + "                     from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                     group by f.patient_id) f on e.patient_id = f.patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Date startDate = (Date) context.getParameterValue("startDate");
		queryBuilder.addParameter("startDate", startDate);
		Date endDate = (Date) context.getParameterValue("endDate");
		queryBuilder.addParameter("endDate", endDate);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
