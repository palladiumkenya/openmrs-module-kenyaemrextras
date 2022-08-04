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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.MissedAppointmentsIITOver30DaysCohortDefinition;
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
 * Evaluator for MissedAppointmentsIITOver30DaysCohortDefinition
 */
@Handler(supports = { MissedAppointmentsIITOver30DaysCohortDefinition.class })
public class MissedAppointmentsIITOver30DaysCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MissedAppointmentsIITOver30DaysCohortDefinition definition = (MissedAppointmentsIITOver30DaysCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select t.patient_id\n"
		        + " from(\n"
		        + "         select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,min(f1.visit_date) as min_f1_visit_date,\n"
		        + "                timestampdiff(DAY,greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')),min(f1.visit_date)) days_missed,\n"
		        + "                greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "                greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "                greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "                d.patient_id as disc_patient,\n"
		        + "                d.effective_disc_date as effective_disc_date,\n"
		        + "                max(d.visit_date) as date_discontinued,\n"
		        + "                d.discontinuation_reason,\n"
		        + "                de.patient_id as started_on_drugs\n"
		        + "         from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "                  join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "                  join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "                  left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "                  left outer JOIN\n"
		        + "              (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date,discontinuation_reason from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "               where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "               group by patient_id\n"
		        + "              ) d on d.patient_id = fup.patient_id\n"
		        + "         left outer join (select f1.patient_id,f1.visit_date from kenyaemr_etl.etl_patient_hiv_followup f1 where f1.visit_date >= date(:startDate)) f1 on fup.patient_id = f1.patient_id\n"
		        + "         where fup.visit_date <= date(:endDate) and f1.visit_date > fup.visit_date or f1.visit_date is null\n"
		        + "         group by patient_id\n"
		        + "         having\n"
		        + "             (((min_f1_visit_date  > latest_tca and days_missed > 30) or (min_f1_visit_date is null and timestampdiff(DAY,date(latest_tca),date(CURRENT_DATE)) > 30)) and latest_tca between date(:startDate) AND date(:endDate)\n"
		        + "                   and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null)\n"
		        + "                 and (date(latest_vis_date) > date(date_discontinued) and date(latest_tca) > date(date_discontinued) or disc_patient is null)\n"
		        + "                 )\n" + "     )t;";
		
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
