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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrKPsTypologyDocumentationStatusDataDefinition;
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
 * Evaluates whether Tx_Curr KPS who visited the facility within the last 3 months have their KP
 * typology documented
 */
@Handler(supports = SimsTxCurrKPsTypologyDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrKpsTypologyDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id, if(f.patient_id is null and c.client_id is null, 'NA', coalesce(f.kp_typology_documented, c.kp_typology_documented))\n" +
				"from kenyaemr_etl.etl_patient_demographics d\n" +
				"         left join (select f.patient_id,\n" +
				"                           if(mid(max(concat(date(f.visit_date), f.population_type)), 11) != 164929, 'NA', if(\n" +
				"                                           mid(max(concat(date(f.visit_date), f.population_type)), 11) = 164929 and\n" +
				"                                           mid(max(concat(date(f.visit_date), f.key_population_type)), 11) in\n" +
				"                                           (105, 165100, 160578, 160579, 162277), 'Y', 'N')) as kp_typology_documented\n" +
				"                    from kenyaemr_etl.etl_patient_hiv_followup f\n" +
				"                    where date(f.visit_date) <= date(:endDate)\n" +
				"                      and f.person_present = 978\n" +
				"                    group by f.patient_id) f on d.patient_id = f.patient_id\n" +
				"         left join (select c.client_id,\n" +
				"                           if(mid(max(concat(date(c.visit_date), c.key_population_type)), 11) in\n" +
				"                              ('People in prison and other closed settings', 'Transgender', 'PWID', 'PWUD', 'MSW',\n" +
				"                               'MSM', 'FSW'), 'Y', 'N') as kp_typology_documented\n" +
				"                    from kenyaemr_etl.etl_contact c\n" +
				"                    where date(c.visit_date) <= date(:endDate)\n" +
				"                    group by c.client_id) c on d.patient_id = c.client_id;";
		
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
