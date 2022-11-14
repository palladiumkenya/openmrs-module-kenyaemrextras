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
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.defaulterTracing.ReturnToCareDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.ReturnToCareDateBasedDateDataDefinition;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.evaluator.EncounterDataEvaluator;
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
 * Evaluates return to care date TODO: Refactor this query. A lot of unnecessary statements and
 * conditions.
 */
@Handler(supports = ReturnToCareDateBasedDateDataDefinition.class, order = 50)
public class ReturnToCareDateBasedDateDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select r.patient_id,return_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id as patient_id,f4.visit_date as app_visit,min(f5.visit_date) as return_date,f4.next_appointment_date from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select f5.patient_id,f5.visit_date,f5.next_appointment_date from kenyaemr_etl.etl_patient_hiv_followup f5)f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate) and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,f2.visit_date as app_visit,min(f23.visit_date) as return_date,f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id,f5.visit_date,f5.next_appointment_date from kenyaemr_etl.etl_patient_hiv_followup f5)f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate) and return_date < f2.next_appointment_date and return_date != app_visit\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,f2.visit_date as app_visit,max(f24.visit_date) as return_date,f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id,f5.visit_date,f5.next_appointment_date from kenyaemr_etl.etl_patient_hiv_followup f5)f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate) and return_date > app_visit and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select f2.patient_id,f2.visit_date as app_visit,f2.next_appointment_date, min(f25.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join\n"
		        + "(select f5.patient_id,f5.visit_date,f5.next_appointment_date ,f6.visit_date as min_visit from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "   left join (select f6.patient_id,f6.visit_date,f6.next_appointment_date from kenyaemr_etl.etl_patient_hiv_followup f6)f6\n"
		        + "             on f5.patient_id = f6.patient_id)f25\n"
		        + "on f2.patient_id = f25.patient_id\n"
		        + "where f25.visit_date >= date(:startDate) and min_visit >= f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having min(app_visit) >= date(:startDate) and min(app_visit) < f2.next_appointment_date and return_date >= f2.next_appointment_date\n"
		        + ")r group by r.patient_id;";
		
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
