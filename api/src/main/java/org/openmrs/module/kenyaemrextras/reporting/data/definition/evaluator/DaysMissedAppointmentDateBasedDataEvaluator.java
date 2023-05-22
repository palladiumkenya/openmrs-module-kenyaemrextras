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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.DaysMissedAppointmentDateBasedDataDefinition;
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
 * Evaluates a DaysMissedAppointmentDateBasedDataDefinition TODO: Refactor this query. A lot of
 * unnecessary statements and conditions.
 */
@Handler(supports = DaysMissedAppointmentDateBasedDataDefinition.class, order = 50)
public class DaysMissedAppointmentDateBasedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,days_missed\n"
		        + "         from (select r.patient_id,\n"
		        + "                      r.app_visit,\n"
		        + "                      r.return_date,\n"
		        + "                      r.next_appointment_date as next_appointment_date,\n"
		        + "                      d.patient_id as disc_patient,\n"
		        + "                      d.visit_date as disc_date,\n"
		        + "                      if(r.app_visit = r.return_date,timestampdiff(DAY,r.next_appointment_date,date(:endDate)),\n"
		        + "                                                   if(return_date > app_visit and return_date < r.next_appointment_date,'-'\n"
		        + "                                                       ,timestampdiff(DAY,r.next_appointment_date,r.return_date))) as days_missed\n"
		        + "               from (\n"
		        + "                        -- Returned after next appointment date\n"
		        + "                                select f4.patient_id      as patient_id,\n"
		        + "                                       f6.app_date           app_visit,\n"
		        + "                                       min(f4.visit_date) as return_date,\n"
		        + "                                       f6.tca             as next_appointment_date\n"
		        + "                                from kenyaemr_etl.etl_patient_hiv_followup f4\n"
		        + "                                         left join (select f5.patient_id,\n"
		        + "                                                           max(f5.visit_date)                                            as app_date,\n"
		        + "                                                           mid(max(concat(f5.visit_date, f5.next_appointment_date)), 11) as tca\n"
		        + "                                                    from kenyaemr_etl.etl_patient_hiv_followup f5\n"
		        + "                                                             left join kenyaemr_etl.etl_patient_hiv_followup f on f5.patient_id = f.patient_id\n"
		        + "                                                    where f5.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                                                    group by f5.patient_id) f6 on f4.patient_id = f6.patient_id\n"
		        + "                                where f4.visit_date > f6.app_date\n"
		        + "                                group by f6.patient_id\n"
		        + "                        union\n"
		        + "                        -- Never Returned\n"
		        + "                        select f0.patient_id  as patient_id,\n"
		        + "                               f0.visit_date  as app_visit,\n"
		        + "                               f7.return_date as return_date,\n"
		        + "                               f7.latest_appointment\n"
		        + "                        from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                                 left join (select f7.patient_id,\n"
		        + "                                                   f7.visit_date,\n"
		        + "                                                   f7.next_appointment_date,\n"
		        + "                                                   max(f7.visit_date)                                            as return_date,\n"
		        + "                                                   mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                            from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                            group by f7.patient_id) f7\n"
		        + "                                           on f0.patient_id = f7.patient_id\n"
		        + "                        where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                          and f7.return_date = f0.visit_date\n"
		        + "                        group by f7.patient_id\n"
		        + "                        having latest_appointment between date(:startDate) and date(:endDate)\n"
		        + "                    ) r\n"
		        + "                        inner join kenyaemr_etl.etl_hiv_enrollment e on r.patient_id = e.patient_id\n"
		        + "                        left outer join (select patient_id,\n"
		        + "                                                coalesce(max(date(effective_discontinuation_date)),\n"
		        + "                                                         max(date(visit_date)))              visit_date,\n"
		        + "                                                max(date(effective_discontinuation_date)) as effective_disc_date,\n"
		        + "                                                discontinuation_reason\n"
		        + "                                         from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                         where date(visit_date) <= date(:endDate)\n"
		        + "                                           and program_name = 'HIV'\n"
		        + "                                         group by patient_id      ) d on d.patient_id = r.patient_id\n"
		        + "               group by r.patient_id\n"
		        + "               having (max(e.visit_date) >= date(d.visit_date) or d.patient_id is null or\n"
		        + "                       date(d.visit_date) >= date(:endDate))) a;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		queryBuilder.addParameter("startDate", startDate);
		queryBuilder.addParameter("endDate", endDate);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
