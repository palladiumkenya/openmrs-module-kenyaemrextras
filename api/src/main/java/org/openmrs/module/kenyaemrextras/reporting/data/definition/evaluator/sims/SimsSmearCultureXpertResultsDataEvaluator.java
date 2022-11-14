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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsFollowUpVLTakenDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsSmearCultureXpertResultsDataDefinition;
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
 * Evaluates whether there is documentation of smear microscopy, culture or Xpert MTB/RIF results
 */
@Handler(supports = SimsSmearCultureXpertResultsDataDefinition.class, order = 50)
public class SimsSmearCultureXpertResultsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select t.patient_id, if((testResult in (162203,162204,162104,703,1362,1363,1364) or genexpert_result in (162204,664) or spatum_smear_result in(664, 703)),'Y','N'  ) from (\n"
		        + "select fup.patient_id, l.testResult, \n"
		        + "mid(max(concat(fup.visit_date,fup.genexpert_result)),11) as genexpert_result,\n"
		        + "mid(max(concat(fup.visit_date,fup.spatum_smear_result )),11) as spatum_smear_result,\n"
		        + "mid(max(concat(fup.visit_date,fup.tb_status )),11) as tbStatus    \n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "left join (\n"
		        + "select patient_id, mid(max(concat(x.visit_date,x.test_result)),11) as testResult from kenyaemr_etl.etl_laboratory_extract x \n"
		        + "where x.lab_test in (162202,1465,307) and x.test_result in (162203,162204,162104,703,1362,1363,1364) and x.visit_date <= date(:endDate)\n"
		        + ") l on fup.patient_id = l.patient_id\n"
		        + "where fup.visit_date <= date(:endDate)\n"
		        + "  GROUP BY fup.patient_id\n"
		        + "  having genexpert_result in (162204,664) or  spatum_smear_result in(664, 703) )t\n";
		
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
