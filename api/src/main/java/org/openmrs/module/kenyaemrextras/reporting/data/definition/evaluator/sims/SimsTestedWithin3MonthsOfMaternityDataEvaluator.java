/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHivDiagnosisDateDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTestedWithin3MonthsOfMaternityDataDefinition;
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
 * Tested within 3 months of delivery Delivery + PMTCT-MAT entry point
 */
@Handler(supports = SimsTestedWithin3MonthsOfMaternityDataDefinition.class, order = 50)
public class SimsTestedWithin3MonthsOfMaternityDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select p.patient_id ,if((p.entry_point = 160456 or p.maternity_test_results is not null) is not null,'Y','N')\n"
		        + "    from (\n"
		        + "                 select d.patient_id,entry_point,maternity_test_results\n"
		        + "              from kenyaemr_etl.etl_patient_demographics d\n"
		        + "                 left join (select t.patient_id,\n"
		        + "                             max(t.visit_date)     as latest_hts_test_date,\n"
		        + "                             mid(max(concat(t.visit_date, t.hts_entry_point)), 11) as entry_point\n"
		        + "                          from kenyaemr_etl.etl_hts_test t\n"
		        + "                            where   t.visit_date between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate) and t.hts_entry_point = 160456\n"
		        + "                          group by t.patient_id) ht on ht.patient_id = d.patient_id\n"
		        + "                 left join (select ld.patient_id,\n"
		        + "                                                 max(ld.visit_date)     as latest_mat_visit_date,\n"
		        + "                                                 mid(max(concat(ld.visit_date, ld.final_test_result)), 11) as maternity_test_results\n"
		        + "                                              from kenyaemr_etl.etl_mchs_delivery ld\n"
		        + "                                              where ld.visit_date between date_sub(date(:endDate), INTERVAL 3 MONTH) and date(:endDate)\n"
		        + "                                              group by ld.patient_id) mat on mat.patient_id = d.patient_id\n"
		        + "                 where  (ht.patient_id is not null or mat.patient_id is not null)\n"
		        + "                 group by d.patient_id) p;";
		
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
