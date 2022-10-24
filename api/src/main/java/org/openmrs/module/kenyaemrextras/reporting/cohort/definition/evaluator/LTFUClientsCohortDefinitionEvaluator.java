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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.LTFUClientsCohortDefinition;
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
 * Evaluator for LTFUClientsDaysCohortDefinition
 */
@Handler(supports = { LTFUClientsCohortDefinition.class })
public class LTFUClientsCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		LTFUClientsCohortDefinition definition = (LTFUClientsCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select a.patient_id\n"
		        + "from (select r.patient_id,\n"
		        + "             r.return_date,\n"
		        + "             r.next_appointment_date,\n"
		        + "             r.app_visit,\n"
		        + "             d.disc_patient as disc_patient,\n"
		        + "             d.disc_date\n"
		        + "      from (\n"
		        + "               -- Never Returned\n"
		        + "               select f0.patient_id  as patient_id,\n"
		        + "                      f0.visit_date  as app_visit,\n"
		        + "                      f7.return_date as return_date,\n"
		        + "                      f0.next_appointment_date\n"
		        + "               from kenyaemr_etl.etl_patient_hiv_followup f0\n"
		        + "                        inner join kenyaemr_etl.etl_hiv_enrollment e on e.patient_id = f0.patient_id\n"
		        + "                        left join (select f7.patient_id,\n"
		        + "                                          f7.visit_date,\n"
		        + "                                          f7.next_appointment_date,\n"
		        + "                                          max(f7.visit_date)                                            as return_date,\n"
		        + "                                          mid(max(concat(f7.visit_date, f7.next_appointment_date)), 11) as latest_appointment\n"
		        + "                                   from kenyaemr_etl.etl_patient_hiv_followup f7\n"
		        + "                                   group by f7.patient_id) f7\n"
		        + "                                  on f0.patient_id = f7.patient_id\n"
		        + "               where f0.next_appointment_date between date(:startDate) and date(:endDate)\n"
		        + "                 and f0.next_appointment_date = latest_appointment\n"
		        + "                 and return_date = f0.visit_date)r\n"
		        + "                                left outer JOIN\n"
		        + "           (select patient_id                                as                               disc_patient,\n"
		        + "                   coalesce(max(date(effective_discontinuation_date)), max(date(visit_date))) visit_date,\n"
		        + "                   max(date(effective_discontinuation_date)) as                               disc_date,\n"
		        + "                   discontinuation_reason\n"
		        + "            from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "            where date(visit_date) <= date(:endDate)\n" + "              and program_name = 'HIV'\n"
		        + "            group by patient_id\n" + "           ) d on d.disc_patient = r.patient_id\n"
		        + "      where (return_date > date(disc_date) or disc_patient is null or disc_date >= date(:endDate))\n"
		        + "          and (next_appointment_date > return_date and return_date = app_visit))a;";
		
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
