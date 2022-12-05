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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQABaselineCD4DataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQANupiDataDefinition;
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
 * Evaluates Clients has baseline CD4 within first two weeks Data Definition
 */
@Handler(supports = DQABaselineCD4DataDefinition.class, order = 50)
public class DQABaselineCD4DataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id,baselineCD4\n"
		        + "  from (      select d.patient_id,\n"
		        + "                              mid(min(concat(enr.visit_date, enr.date_confirmed_hiv_positive)), 11)  as dateConfirmedHiv,\n"
		        + "                              mid(min(concat(l.visit_date, l.test_result)), 11)  as baselineCD4,\n"
		        + "                              mid(min(concat(l.visit_date, l.date_test_requested)), 11)  as dateTestOrdered\n"
		        + "                       from kenyaemr_etl.etl_patient_demographics d\n"
		        + "                         left join (\n"
		        + "                                     select x.patient_id, x.visit_date ,x.test_result, x.date_test_requested\n"
		        + "                                     from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                                     where  lab_test in (5497)\n"
		        + "                                     GROUP BY  x.patient_id\n"
		        + "                                   ) l on d.patient_id = l.patient_id\n"
		        + "                         left join (\n"
		        + "                                     select e.patient_id, e.visit_date ,e.date_confirmed_hiv_positive\n"
		        + "                                     from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "                                     GROUP BY  e.patient_id\n"
		        + "                                   ) enr on d.patient_id = enr.patient_id\n"
		        + "                       group by d.patient_id\n" + "                     )t\n"
		        + "    where baselineCD4 is not null and (timestampdiff(WEEK,dateConfirmedHiv,dateTestOrdered))< 2;";
		
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
