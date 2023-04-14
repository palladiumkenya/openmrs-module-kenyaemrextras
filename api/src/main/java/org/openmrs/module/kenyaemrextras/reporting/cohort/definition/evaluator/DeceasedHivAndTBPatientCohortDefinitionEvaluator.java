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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DeceasedHivAndTBPatientCohortDefinition;
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
 * Evaluator for DeceasedHivAndTBPatientCohortDefinition
 */
@Handler(supports = { DeceasedHivAndTBPatientCohortDefinition.class })
public class DeceasedHivAndTBPatientCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		DeceasedHivAndTBPatientCohortDefinition definition = (DeceasedHivAndTBPatientCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select d.patient_id\n"
		        + "from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "         inner join kenyaemr_etl.etl_patient_demographics m on d.patient_id = m.patient_id\n"
		        + "         inner join kenyaemr_etl.etl_hiv_enrollment e on d.patient_id = e.patient_id\n"
		        + "         left join (select t.patient_id      as tb_patient,\n"
		        + "                           max(t.visit_date) as tb_enrollment_date,\n"
		        + "                           d.patient_id      as disc_tb,\n"
		        + "                           d.disc_date       as tb_disc_date\n"
		        + "                    from kenyaemr_etl.etl_tb_enrollment t\n"
		        + "                             left join (select d.patient_id,\n"
		        + "                                               coalesce(max(d.effective_discontinuation_date),\n"
		        + "                                                        max(d.visit_date)) as disc_date\n"
		        + "                                        from kenyaemr_etl.etl_patient_program_discontinuation d\n"
		        + "                                        where d.program_name = 'TB'\n"
		        + "                                        group by d.patient_id) d on t.patient_id = d.patient_id\n"
		        + "                    group by t.patient_id) tb on d.patient_id = tb.tb_patient\n"
		        + "         left join (select s.patient_id,\n"
		        + "                           max(date(s.visit_date))                                         as tb_status_date,\n"
		        + "                           mid(max(concat(date(s.visit_date), s.resulting_tb_status)), 11) as tb_screening_status,\n"
		        + "                           mid(max(concat(date(s.visit_date), s.started_anti_TB)), 11)     as started_tb_treatment\n"
		        + "                    from kenyaemr_etl.etl_tb_screening s\n"
		        + "                    where s.person_present != 161642\n"
		        + "                    group by s.patient_id\n"
		        + "                    having (tb_screening_status = 1662\n"
		        + "                        or started_tb_treatment = 1065)\n"
		        + "                       and tb_status_date between date(:startDate) and date(:endDate)) s on d.patient_id = s.patient_id\n"
		        + "where coalesce(date(d.date_died), date(d.effective_discontinuation_date), date(d.visit_date)) between\n"
		        + "    date(:startDate) and date(:endDate)\n"
		        + "  and d.discontinuation_reason = 160034\n"
		        + "  and d.program_name in ('HIV', 'TB')\n"
		        + "  and (tb.tb_patient is not null and ((tb.tb_enrollment_date > tb.tb_disc_date and\n"
		        + "                                       timestampdiff(MONTH, tb.tb_enrollment_date, coalesce(date(d.date_died),\n"
		        + "                                                                                            date(d.effective_discontinuation_date),\n"
		        + "                                                                                            date(d.visit_date)) <=\n"
		        + "                                                                                   10) or tb_disc_date is null)\n"
		        + "    ) or s.patient_id is not null\n" + "    )\n" + "group by d.patient_id;";
		
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
