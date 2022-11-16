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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxNewKPRetestDocumentationStatusDataDefinition;
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
 * Evaluates whether TX_curr KPs with presumed TB had TB testing done
 */
@Handler(supports = SimsTxCurrKPPresumedTBTestingDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrKPPresumedTBTestingDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(tb_status != 142177 or tb_status is null, 'NA',\n"
		        + "       if(lab_test in (307, 162202, 1465) or geneXpert_ordered = 162202 or smear_microscopy_ordered = 307 or chest_xray_ordered = 12,'Y','N'))as tb_test_documented\n"
		        + "from (select e.patient_id,\n"
		        + "             f.fup_date,\n"
		        + "             f.smear_microscopy_ordered as smear_microscopy_ordered,\n"
		        + "             f.geneXpert_ordered        as geneXpert_ordered,\n"
		        + "             f.chest_xray_ordered        as chest_xray_ordered,\n"
		        + "             f.tb_status               as tb_status,\n"
		        + "             x.test_date,\n"
		        + "             x.lab_test,\n"
		        + "             x.lab_test_result\n"
		        + "      from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "               inner join (select f.patient_id,\n"
		        + "                                  max(f.visit_date)                                            as fup_date,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.tb_status)), 11)        as tb_status,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.genexpert_ordered)), 11) as geneXpert_ordered,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.spatum_smear_ordered)),11) as smear_microscopy_ordered,\n"
		        + "                                  mid(max(concat(date(f.visit_date), f.chest_xray_ordered)),11) as chest_xray_ordered\n"
		        + "                           from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                           where f.visit_date <= date(:endDate)\n"
		        + "                           group by f.patient_id\n"
		        + "      ) f on f.patient_id = e.patient_id\n"
		        + "               left join (select x.patient_id,\n"
		        + "                                 max(x.date_test_requested)                                       as test_date,\n"
		        + "                                 mid(max(concat(date(x.visit_date), x.lab_test)), 11)    as lab_test,\n"
		        + "                                 mid(max(concat(date(x.visit_date), x.test_result)), 11) as lab_test_result\n"
		        + "                          from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                          where x.date_test_requested <= date(:endDate)\n"
		        + "                          group by x.patient_id\n"
		        + "                          having mid(max(concat(date(x.visit_date), x.lab_test)), 11) in (307, 162202, 1465)) x\n"
		        + "                         on f.patient_id = e.patient_id\n" + "      group by patient_id) a;";
		
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
