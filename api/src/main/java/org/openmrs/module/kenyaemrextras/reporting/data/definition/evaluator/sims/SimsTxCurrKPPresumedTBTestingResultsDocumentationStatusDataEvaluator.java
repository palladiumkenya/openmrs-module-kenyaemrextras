/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrKPPresumedTBTestingDocumentationStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrKPPresumedTBTestingResultsDocumentationStatusDataDefinition;
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
 * Evaluates whether TX_curr KPs with presumed TB, had TB testing done and received results
 */
@Handler(supports = SimsTxCurrKPPresumedTBTestingResultsDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrKPPresumedTBTestingResultsDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if(tb_status != 142177 or tb_status is null,'NA',if(a.geneXpert_result in (664, 162203, 162204, 164104) or a.smear_microscopy_result in (664, 703)\n"
		        + "              or a.chest_xRay_result in (1115,152526) or (lab_test in (307, 162202, 1465) and\n"
		        + "                  lab_test_result in (664, 162203, 162204, 164104, 163611, 1138, 1364, 1362, 1363, 159985, 703)), 'Y',\n"
		        + "          'N')) as tb_results_documented\n"
		        + "from (select e.patient_id,\n"
		        + "             f.fup_date,\n"
		        + "             f.smear_microscopy_result as smear_microscopy_result,\n"
		        + "             f.geneXpert_result        as geneXpert_result,\n"
		        + "             f.chest_xRay_result        as chest_xRay_result,\n"
		        + "             f.tb_status               as tb_status,\n"
		        + "             x.test_date,\n"
		        + "             x.lab_test,\n"
		        + "             x.lab_test_result\n"
		        + "      from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "               inner join (select f.patient_id,\n"
		        + "                                  max(f.visit_date)                                            as fup_date,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.tb_status)), 11)        as tb_status,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.genexpert_result)), 11) as geneXpert_result,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.spatum_smear_result)),\n"
		        + "                                      11)                                                      as smear_microscopy_result,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.chest_xray_result)),\n"
		        + "                                      11) as chest_xRay_result\n"
		        + "                           from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                           where f.visit_date between date(:startDate) and date(:endDate)\n"
		        + "                           group by f.patient_id\n"
		        + "      ) f on f.patient_id = e.patient_id\n"
		        + "               left join (select x.patient_id,\n"
		        + "                                 max(x.date_test_requested)                                       as test_date,\n"
		        + "                                 mid(max(concat(date(x.visit_date), x.lab_test)), 11)    as lab_test,\n"
		        + "                                 mid(max(concat(date(x.visit_date), x.test_result)), 11) as lab_test_result\n"
		        + "                          from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                          where x.visit_date between date(:startDate) and date(:endDate)\n"
		        + "                          group by x.patient_id\n"
		        + "                          having mid(max(concat(date(x.visit_date), x.lab_test)), 11) in (307, 162202, 1465)) x\n"
		        + "                         on x.patient_id = e.patient_id\n" + "      group by patient_id) a;";
		
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
