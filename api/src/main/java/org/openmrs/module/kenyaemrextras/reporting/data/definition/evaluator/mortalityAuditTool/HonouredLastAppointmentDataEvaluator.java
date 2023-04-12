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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.HonouredLastAppointmentDataDefinition;
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
 * Evaluates HonouredLastAppointmentDataDefinition
 */
@Handler(supports = HonouredLastAppointmentDataDefinition.class, order = 50)
public class HonouredLastAppointmentDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select b.patient_id,\n"
		        + "       if(latest_visit_date <= next_appointment_before_last_visit, 'Yes',if(latest_visit_date > next_appointment_before_last_visit,'No','Unknown')) as kept_last_appointment\n"
		        + "from (select a.patient_id,\n"
		        + "             a.latest_visit_date,\n"
		        + "             a.next_appointment_date,\n"
		        + "             a.visit_date_before_last_visit,\n"
		        + "             a.next_appointment_before_last_visit,\n"
		        + "             d.date_died\n"
		        + "      from (select f.patient_id,\n"
		        + "                   mid(max(concat(f.visit_date, f.next_appointment_date)), 11)    as next_appointment_date,\n"
		        + "                   left(max(concat(f.visit_date, f.next_appointment_date)), 10)   as latest_visit_date,\n"
		        + "                   mid(max(concat(f1.visit_date, f1.next_appointment_date)), 11)  as next_appointment_before_last_visit,\n"
		        + "                   left(max(concat(f1.visit_date, f1.next_appointment_date)), 10) as visit_date_before_last_visit\n"
		        + "            from kenyaemr_etl.etl_patient_hiv_followup f\n" + "                     left join\n"
		        + "                 kenyaemr_etl.etl_patient_hiv_followup f1\n"
		        + "                 on f.patient_id = f1.patient_id\n" + "            where f.visit_date > f1.visit_date\n"
		        + "            group by f.patient_id) a\n" + "               inner join (select d.patient_id,\n"
		        + "                                  date(coalesce(d.date_died, d.effective_discontinuation_date,\n"
		        + "                                                d.visit_date)) as date_died\n"
		        + "                           from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                           where d.discontinuation_reason = 160034) d on a.patient_id = d.patient_id\n"
		        + "      where next_appointment_before_last_visit <= date_died) b;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
