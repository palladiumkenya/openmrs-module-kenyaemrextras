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
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.rri.MissedHAARTCohortDefinition;
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
 * Evaluator for pregnant or breastfeeding mothers who missed HAART
 */
@Handler(supports = { MissedHAARTCohortDefinition.class })
public class MissedHAARTCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		MissedHAARTCohortDefinition definition = (MissedHAARTCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "select e.patient_id\n"
		        + "from kenyaemr_etl.etl_mch_enrollment e\n"
		        + "         left join (select d.patient_id, d.date_started, d.regimen\n"
		        + "                    from kenyaemr_etl.etl_drug_event d\n"
		        + "                    where d.program = 'HIV') d on e.patient_id = d.patient_id\n"
		        + "         left join (select t.patient_id, t.final_test_result\n"
		        + "                    from kenyaemr_etl.etl_hts_test t\n"
		        + "                    where t.final_test_result = 'Positive') t\n"
		        + "                   on e.patient_id = t.patient_id\n"
		        + "         left join (select h.patient_id from kenyaemr_etl.etl_hiv_enrollment h) h\n"
		        + "                   on h.patient_id = e.patient_id\n"
		        + "         left join(select a.patient_id, a.visit_date as anc_visit_date\n"
		        + "                   from kenyaemr_etl.etl_mch_antenatal_visit a\n"
		        + "                   where a.final_test_result = 'Positive') a on e.patient_id = a.patient_id\n"
		        + "         left join(select m.patient_id, m.visit_date as mat_visit_date\n"
		        + "                   from kenyaemr_etl.etl_mchs_delivery m\n"
		        + "                   where m.final_test_result = 'Positive') m on e.patient_id = m.patient_id\n"
		        + "         left join(select p.patient_id, p.visit_date as pnc_visit_date\n"
		        + "                   from kenyaemr_etl.etl_mch_postnatal_visit p\n"
		        + "                   where p.final_test_result = 'Positive') p on e.patient_id = p.patient_id\n"
		        + "where date(e.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "  and d.patient_id is null\n"
		        + "  and (e.hiv_status = 703 or t.patient_id is not null or h.patient_id is not null or a.patient_id is not null or\n"
		        + "       m.patient_id is not null or p.patient_id is not null);";
		
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
