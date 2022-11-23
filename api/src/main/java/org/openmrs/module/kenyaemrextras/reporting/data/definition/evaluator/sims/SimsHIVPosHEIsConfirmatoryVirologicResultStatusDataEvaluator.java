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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHIVPosHEIsConfirmatoryVirologicResultStatusDataDefinition;
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
 * Evaluates whether New HIV Positive HEIs with an initial positive virologic test result had a
 * documentation that confirmatory virologic test was collected
 */
@Handler(supports = SimsHIVPosHEIsConfirmatoryVirologicResultStatusDataDefinition.class, order = 50)
public class SimsHIVPosHEIsConfirmatoryVirologicResultStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id,if(x.patient_id is not null or v.patient_id is not null,'Y','N')\n"
		        + "from kenyaemr_etl.etl_hei_enrollment e\n" + "         left join (select x.patient_id, x.visit_date\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.order_reason = 162082\n" + "                      and x.test_result = 703\n"
		        + "                      and date(x.visit_date) <= date(:endDate)) x on e.patient_id = x.patient_id\n"
		        + "         left join (select v.patient_id, v.dna_pcr_result, v.first_antibody_result\n"
		        + "                    from kenyaemr_etl.etl_hei_follow_up_visit v\n"
		        + "                    where v.visit_date <= date(:endDate)\n"
		        + "                      and (v.dna_pcr_result = 703 and v.dna_pcr_contextual_status = 162082)\n"
		        + "                      and date(v.visit_date) <= date(:endDate)) v\n"
		        + "                   on e.patient_id = v.patient_id;";
		
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
