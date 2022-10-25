/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MissedAppointmentsRTCOver30DaysCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Evaluator for MissedAppointmentsRTCOver30DaysCohortDefinition Includes patients who are active on
 * ART.
 */
@Handler(supports = { MissedAppointmentsRTCOver30DaysCohortDefinition.class })
public class MissedAppointmentsRTCOver30DaysCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MissedAppointmentsRTCOver30DaysCohortDefinition definition = (MissedAppointmentsRTCOver30DaysCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select a.patient_id from (select r.patient_id,r.return_date,r.next_appointment_date,d.patient_id as disc_patient,d.visit_date as disc_date from(\n"
		        + "-- Returned on or after next appointment date\n"
		        + "select f4.patient_id      as patient_id,\n"
		        + "f4.visit_date      as app_visit,\n"
		        + "min(f5.visit_date) as return_date,\n"
		        + "f4.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "left join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = f4.patient_id\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f5\n"
		        + "on f4.patient_id = f5.patient_id\n"
		        + "where f4.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f5.visit_date >= date(:startDate)\n"
		        + "and f5.visit_date >= f4.next_appointment_date\n"
		        + "group by f5.patient_id\n"
		        + "having return_date >= f4.next_appointment_date\n"
		        + "union\n"
		        + "-- Appointments between start and end date and returned early although after start date\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "min(f23.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f23\n"
		        + "on f2.patient_id = f23.patient_id\n"
		        + "where f23.visit_date >= date(:startDate)\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date >= date(:startDate)\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "and return_date != app_visit\n"
		        + "union\n"
		        + "-- Never Returned\n"
		        + "select f0.patient_id  as patient_id,\n"
		        + "f0.visit_date  as app_visit,\n"
		        + "f7.return_date as return_date,\n"
		        + "f0.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "left join (select f7.patient_id,\n"
		        + "f7.visit_date,\n"
		        + "f7.next_appointment_date,\n"
		        + "max(f7.visit_date)                                            as return_date,\n"
		        + "mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "group by f7.patient_id) f7\n"
		        + "on f0.patient_id = f7.patient_id\n"
		        + "where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "and f0.next_appointment_date = latest_appointment\n"
		        + "and return_date = f0.visit_date\n"
		        + "-- Returned before appointment date and before reporting start date\n"
		        + "union\n"
		        + "select f2.patient_id,\n"
		        + "f2.visit_date       as app_visit,\n"
		        + "max(f24.visit_date) as return_date,\n"
		        + "f2.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f2\n"
		        + "left join (select f5.patient_id, f5.visit_date, f5.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5) f24\n"
		        + "on f2.patient_id = f24.patient_id\n"
		        + "where f24.visit_date < f2.next_appointment_date\n"
		        + "and f2.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by f2.patient_id\n"
		        + "having return_date < date(:startDate)\n"
		        + "and return_date > app_visit\n"
		        + "and return_date < f2.next_appointment_date\n"
		        + "union\n"
		        + "-- Multiple Appointments between start and end date and returned on or after app date although after start date. Picking First Appointment\n"
		        + "select fa.patient_id,\n"
		        + "fa.visit_date     as app_visit,\n"
		        + "fa.next_appointment_date,\n"
		        + "min(fb.min_visit) as return_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fa\n"
		        + "left join\n"
		        + "(select f5.patient_id,\n"
		        + "f5.visit_date,\n"
		        + "f5.next_appointment_date,\n"
		        + "f6.visit_date as min_visit\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "left join (select f6.patient_id, f6.visit_date, f6.next_appointment_date\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup f6) f6\n"
		        + "on f5.patient_id = f6.patient_id) fb\n"
		        + "on fa.patient_id = fb.patient_id\n"
		        + "where fb.visit_date >= date(:startDate)\n"
		        + "and min_visit >= fa.next_appointment_date\n"
		        + "and fa.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "group by fa.patient_id             having min(app_visit) >= date(:startDate)\n"
		        + "and min(app_visit) < fa.next_appointment_date\n"
		        + "and return_date >= fa.next_appointment_date         ) r\n"
		        + "inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "left outer join (select patient_id,\n"
		        + "coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "max(date(effective_discontinuation_date)) as                               effective_disc_date,\n"
		        + "discontinuation_reason\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "where date(visit_date) <= date(:endDate)\n"
		        + "and program_name = 'HIV'\n"
		        + "group by patient_id\n"
		        + ") d on d.patient_id = r.patient_id\n"
		        + "where timestampdiff(DAY, next_appointment_date, return_date) > 30\n"
		        + "group by r.patient_id\n"
		        + "having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or date(d.visit_date) >= date(:endDate))) a;";
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("startDate", startDate);
		builder.addParameter("endDate", endDate);
		
		List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);
		newCohort.setMemberIds(new HashSet<Integer>(ptIds));
		return new EvaluatedCohort(newCohort, definition, context);
	}
}
