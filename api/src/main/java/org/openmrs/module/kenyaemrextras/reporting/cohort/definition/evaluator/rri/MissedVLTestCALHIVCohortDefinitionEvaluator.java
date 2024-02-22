/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.evaluator.rri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.rri.MissedVLTestCAHLHIVCohortDefinition;
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
 * Evaluator for CALHIV who missed VL tests within the last 6 months
 */
@Handler(supports = { MissedVLTestCAHLHIVCohortDefinition.class })
public class MissedVLTestCALHIVCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MissedVLTestCAHLHIVCohortDefinition definition = (MissedVLTestCAHLHIVCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select a.patient_id from (select t.patient_id,vl.vl_date,vl.patient_id as vl_patient\n"
		        + "from (select fup.visit_date,\n"
		        + "             fup.patient_id,\n"
		        + "             max(e.visit_date)                                                                as enroll_date,\n"
		        + "             greatest(max(fup.visit_date), ifnull(max(d.visit_date), '0000-00-00'))           as latest_vis_date,\n"
		        + "             greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),\n"
		        + "                      ifnull(max(d.visit_date), '0000-00-00'))                                as latest_tca,\n"
		        + "             d.patient_id                                                                     as disc_patient,\n"
		        + "             d.effective_disc_date                                                            as effective_disc_date,\n"
		        + "             max(d.visit_date)                                                                as date_discontinued,\n"
		        + "             de.patient_id                                                                    as started_on_drugs,\n"
		        + "             mid(max(concat(de.visit_date, de.regimen)), 11)                                  as regimen\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "               join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id\n"
		        + "               join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id = e.patient_id\n"
		        + "               left join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program = 'HIV' and\n"
		        + "                                                                 date(date_started) <= date(:endDate)\n"
		        + "               left join (select t.patient_id, mid(max(concat(t.visit_date, t.weight)), 11) as weight\n"
		        + "                          from kenyaemr_etl.etl_patient_triage t\n"
		        + "                          where date(t.visit_date) <= date(:endDate)\n"
		        + "                          group by t.patient_id) t on fup.patient_id = t.patient_id\n"
		        + "               left outer JOIN\n"
		        + "           (select patient_id,\n"
		        + "                   coalesce(date(effective_discontinuation_date), visit_date) visit_date,\n"
		        + "                   max(date(effective_discontinuation_date)) as               effective_disc_date\n"
		        + "            from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "            where date(visit_date) <= date(:endDate)\n"
		        + "              and program_name = 'HIV'\n"
		        + "            group by patient_id) d on d.patient_id = fup.patient_id\n"
		        + "      where fup.visit_date <= date(:endDate)\n"
		        + "        and timestampdiff(MONTH, p.DOB, date(:endDate)) >= 1\n"
		        + "        and timestampdiff(YEAR, p.DOB, date(:endDate)) <= 19\n"
		        + "        and (t.patient_id is null or t.weight >= 3)\n"
		        + "      group by patient_id\n"
		        + "      having (started_on_drugs is not null and started_on_drugs <> '')\n"
		        + "         and (\n"
		        + "          (\n"
		        + "                  (timestampdiff(DAY, date(latest_tca), date(:endDate)) <= 30\n"
		        + "                   ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or\n"
		        + "                    d.effective_disc_date is null))\n"
		        + "                  and\n"
		        + "                  (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or\n"
		        + "                   disc_patient is null)))) t\n"
		        + "         left join (select b.patient_id,\n"
		        + "                            coalesce(max(b.test_date),max(b.visit_date))                            as vl_date,\n"
		        + "                            date_sub(date(:endDate), interval 12 MONTH),\n"
		        + "                            mid(max(concat(b.visit_date, b.lab_test)), 11) as lab_test,\n"
		        + "                            if(mid(max(concat(b.visit_date, b.lab_test)), 11) = 856,\n"
		        + "                               mid(max(concat(b.visit_date, b.test_result)), 11), if(\n"
		        + "                                               mid(max(concat(b.visit_date, b.lab_test)), 11) = 1305 and\n"
		        + "                                               mid(max(concat(visit_date, test_result)), 11) = 1302, 'LDL',\n"
		        + "                                               ''))                        as vl_result,\n"
		        + "                            mid(max(concat(b.visit_date, b.urgency)), 11)  as urgency\n"
		        + "                     from (select x.patient_id  as patient_id,\n"
		        + "                                  x.visit_date  as visit_date,\n"
		        + "                                  x.date_test_requested  as test_date,\n"
		        + "                                  x.lab_test    as lab_test,\n"
		        + "                                  x.test_result as test_result,\n"
		        + "                                  urgency       as urgency\n"
		        + "                           from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                           where x.lab_test in (1305, 856)\n"
		        + "                           group by x.patient_id, x.visit_date\n"
		        + "                           order by visit_date desc) b\n"
		        + "                     group by patient_id) vl\n"
		        + "                    on t.patient_id = vl.patient_id)a\n"
		        + "where timestampdiff(MONTH, date(a.vl_date), date(:endDate)) > 6 or a.vl_patient is null;";
		
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
