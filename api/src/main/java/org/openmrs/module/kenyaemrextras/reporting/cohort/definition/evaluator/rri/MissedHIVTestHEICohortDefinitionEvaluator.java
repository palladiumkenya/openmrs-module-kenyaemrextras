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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.rri.MissedHIVTestHEICohortDefinition;
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
 * Evaluator for HEIs who missed EID tests
 */
@Handler(supports = { MissedHIVTestHEICohortDefinition.class })
public class MissedHIVTestHEICohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MissedHIVTestHEICohortDefinition definition = (MissedHIVTestHEICohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select e.patient_id\n"
		        + "from kenyaemr_etl.etl_hei_enrollment e\n"
		        + "         inner join kenyaemr_etl.etl_patient_demographics d on e.patient_id = d.patient_id\n"
		        + "         left join kenyaemr_etl.etl_hiv_enrollment hiv on e.patient_id = hiv.patient_id\n"
		        + "         left join (select p.patient_id\n"
		        + "                    from kenyaemr_etl.etl_patient_program_discontinuation p\n"
		        + "                    where p.program_name = 'MCH Child HEI') p\n"
		        + "                   on e.patient_id = p.patient_id\n"
		        + "         left join (select x.patient_id week6pcr, x.test_result as week6results\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.lab_test = 1030\n"
		        + "                      and x.order_reason = 1040) a on e.patient_id = a.week6pcr\n"
		        + "         left join (select x.patient_id month6pcr, x.test_result as month6results\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.lab_test = 1030\n"
		        + "                      and x.order_reason = 1326) b on e.patient_id = b.month6pcr\n"
		        + "         left join (select x.patient_id month12pcr, x.test_result as month12results\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.lab_test = 1030\n"
		        + "                      and x.order_reason = 844) c on e.patient_id = c.month12pcr\n"
		        + "         left join (select x.patient_id month18AB, x.test_result as month18results\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                    where x.lab_test = 163722\n"
		        + "                      and x.order_reason = 164860) f on e.patient_id = f.month18AB\n"
		        + "         left join (select f.patient_id                                                as ceased_bf_hei,\n"
		        + "                           left(max(concat(date(f.visit_date), f.infant_feeding)), 10) as bf_date,\n"
		        + "                           mid(max(concat(date(f.visit_date), f.infant_feeding)), 11)  as latest_infant_feeding,\n"
		        + "                           x.patient_id                                                as cessation_bf_tested_hei\n"
		        + "                    from kenyaemr_etl.etl_hei_follow_up_visit f\n"
		        + "                             left join (select x.patient_id\n"
		        + "                                        from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                                        where x.lab_test = 163722\n"
		        + "                                          and x.order_reason = 164460) x\n"
		        + "                                       on x.patient_id = f.patient_id\n"
		        + "                    group by f.patient_id) g\n"
		        + "                   on e.patient_id = g.ceased_bf_hei\n"
		        + "         left join (select x0.patient_id positive_hei,\n"
		        + "                           x0.visit_date date_tested_postive,\n"
		        + "                           x.confirmed_hei,\n"
		        + "                           x.confirmatoryresults,\n"
		        + "                           x.confirmatory_results\n"
		        + "                    from kenyaemr_etl.etl_laboratory_extract x0\n"
		        + "                             left join\n"
		        + "                         (select x.patient_id             confirmed_hei,\n"
		        + "                                 x.date_test_requested as confirmatory_results,\n"
		        + "                                 x.test_result         as confirmatoryresults\n"
		        + "                          from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                          where x.lab_test = 163722\n"
		        + "                            and x.order_reason = 164860) x on x0.patient_id = x.confirmed_hei\n"
		        + "                    where x0.lab_test in (1030, 163722)\n"
		        + "                      and x0.order_reason in (1040, 1326, 844, 164860)\n"
		        + "                      and x0.test_result = 703) i on e.patient_id = g.ceased_bf_hei\n"
		        + "where date(e.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "  and timestampdiff(MONTH, date(d.DOB), date(:endDate)) <= 24\n"
		        + "  and ((timestampdiff(WEEK, date(d.dob), date(:endDate)) >= 6 and a.week6pcr is null) or\n"
		        + "       (timestampdiff(MONTH, date(d.dob), date(:endDate)) >= 6 and b.month6pcr is null) or\n"
		        + "       (timestampdiff(MONTH, date(d.dob), date(:endDate)) >= 12 and c.month12pcr is null)\n"
		        + "    or (timestampdiff(MONTH, date(d.dob), date(:endDate)) >= 18 and f.month18AB is null) or\n"
		        + "       (timestampdiff(WEEK, date(g.bf_date), date(:endDate)) >= 6 and g.latest_infant_feeding = 164478 and\n"
		        + "        g.cessation_bf_tested_hei is null)\n"
		        + "    or (i.positive_hei is not null and i.confirmed_hei is null))\n" + "  and p.patient_id is null\n"
		        + "  and hiv.patient_id is null\n" + "group by e.patient_id;";
		
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
