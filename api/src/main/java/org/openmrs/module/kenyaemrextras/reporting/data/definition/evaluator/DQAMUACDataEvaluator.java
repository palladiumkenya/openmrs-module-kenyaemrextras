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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DQAMUACDataDefinition;
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
 * Evaluates MUAC status on last visit Data Definition
 */
@Handler(supports = DQAMUACDataDefinition.class, order = 50)
public class DQAMUACDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
<<<<<<< HEAD
		        + "       if((a.pregnancy_status = 1065 or age <= 5) and muac is NULL, 'Missing',\n"
		        + "          if(((a.pregnancy_status = 1066 or pregnancy_status is null) and age > 5), 'NA', a.muac)) as MUAC_Status\n"
		        + "from (select fup.patient_id,\n"
		        + "             mid(max(concat(date(fup.visit_date), fup.pregnancy_status)), 11) as pregnancy_status,\n"
		        + "             mid(max(concat(date(fup.visit_date), fup.muac)), 11)             as muac,\n"
		        + "             timestampdiff(YEAR, d.DOB, date(:endDate))                       as age\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "               inner join kenyaemr_etl.etl_patient_demographics d on fup.patient_id = d.patient_id\n"
		        + "      where fup.visit_date <= date(:endDate)\n" + "      group by fup.patient_id) a;";
=======
		        + "    if ((muac is null or muac ='') and (weight is null or height is null),'Missing',if((a.pregnancy_status = 1065 or age <= 5) and (muac is not null or muac <> ''), muac, if((a.pregnancy_status is null or a.pregnancy_status = 1066) and (age > 5), ROUND(weight/(height * height),2),\n"
		        + "    'Missing'))) as muac_bmi\n" + "    from (select fup.patient_id,\n"
		        + "    mid(max(concat(date (fup.visit_date), fup.pregnancy_status)), 11) as pregnancy_status,\n"
		        + "    mid(max(concat(date (fup.visit_date), fup.muac)), 11) as muac,\n"
		        + "    mid(max(concat(date (fup.visit_date), fup.weight)), 11) as weight,\n"
		        + "    mid(max(concat(date (fup.visit_date), fup.height)), 11)/100 as height,\n"
		        + "    timestampdiff(YEAR, d.DOB, date (:endDate)) as age\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "    inner join kenyaemr_etl.etl_patient_demographics d on fup.patient_id = d.patient_id\n"
		        + "    where fup.visit_date <= date (:endDate) group by fup.patient_id) a;";
>>>>>>> 7ede41b (Removed ccc 10 digit and format validation.Revised Current on ART regimen as either DTG-based or not,Height values,included BMI in MUAC column, revised TPT status outcomes, added TB screening outcomes, TPT start date and TPT outcome date variables)
		
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
