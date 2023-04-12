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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.ClinicAppointmentsSyncWithCareGiversDataDefinition;
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
 * Evaluates ClinicAppointmentsSyncWithCareGiversDataDefinition
 */
@Handler(supports = ClinicAppointmentsSyncWithCareGiversDataDefinition.class, order = 50)
public class ClinicAppointmentsSyncWithCareGiversDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.child,\n"
		        + "       if(a.child_next_appointment_date = a.caregiver_next_appointment_date or\n"
		        + "          a.child_last_visit = a.caregiver_last_visit, 'Yes', 'No') as appointments_sync\n"
		        + "from (select fr.care_giver,\n"
		        + "             fr.caregiver_last_visit,\n"
		        + "             fr.caregiver_next_appointment_date,\n"
		        + "             fr.child,\n"
		        + "             fr1.child_last_visit,\n"
		        + "             fr1.child_next_appointment_date\n"
		        + "      from kenyaemr_etl.etl_patient_demographics d\n"
		        + "               left join (select r.person_a              as care_giver,\n"
		        + "                                 f.patient_id,\n"
		        + "                                 r.person_b              as child,\n"
		        + "                                 f.latest_visit          as caregiver_last_visit,\n"
		        + "                                 f.next_appointment_date as caregiver_next_appointment_date\n"
		        + "                          from openmrs.relationship r\n"
		        + "                                   inner join openmrs.relationship_type t on r.relationship = t.relationship_type_id\n"
		        + "                              and t.uuid in\n"
		        + "                                  ('3667e52f-8653-40e1-b227-a7278d474020', '8d91a210-c2cc-11de-8d13-0010c6dffd0f',\n"
		        + "                                   '5f115f62-68b7-11e3-94ee-6bef9086de92',\n"
		        + "                                   'a8058424-5ddf-4ce2-a5ee-6e08d01b5960')\n"
		        + "                                   inner join (select f.patient_id,\n"
		        + "                                                      left(max(concat(f.visit_date, f.next_appointment_date)), 10) as latest_visit,\n"
		        + "                                                      mid(max(concat(f.visit_date, f.next_appointment_date)), 11)  as next_appointment_date\n"
		        + "                                               from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                                               group by f.patient_id) f\n"
		        + "                                              on r.person_a = f.patient_id) fr on d.patient_id = fr.care_giver\n"
		        + "               left join (select r.person_b              as child,\n"
		        + "                                 f.patient_id,\n"
		        + "                                 f.latest_visit          as child_last_visit,\n"
		        + "                                 f.next_appointment_date as child_next_appointment_date\n"
		        + "                          from openmrs.relationship r\n"
		        + "                                   inner join openmrs.relationship_type t on r.relationship = t.relationship_type_id\n"
		        + "                              and t.uuid in\n"
		        + "                                  ('3667e52f-8653-40e1-b227-a7278d474020', '8d91a210-c2cc-11de-8d13-0010c6dffd0f',\n"
		        + "                                   '5f115f62-68b7-11e3-94ee-6bef9086de92',\n"
		        + "                                   'a8058424-5ddf-4ce2-a5ee-6e08d01b5960')\n"
		        + "                                   inner join (select f.patient_id,\n"
		        + "                                                      left(max(concat(f.visit_date, f.next_appointment_date)), 10) as latest_visit,\n"
		        + "                                                      mid(max(concat(f.visit_date, f.next_appointment_date)), 11)  as next_appointment_date\n"
		        + "                                               from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                                               group by f.patient_id) f\n"
		        + "                                              on r.person_b = f.patient_id) fr1 on fr.child = fr1.child\n"
		        + "      where fr.care_giver is not null\n" + "         or fr1.child is not null) a;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
