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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsNegativeTBResultsAndEverOnTPTDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTBResultDoumentedDataDefinition;
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
 * Evaluates Does the chart document that HIV patients who screened negative for active TB were ever
 * initiated on TPT/IPT
 */
@Handler(supports = SimsNegativeTBResultsAndEverOnTPTDataDefinition.class, order = 50)
public class SimsNegativeTBResultsAndEverOnTPTDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patientId, if (patientId =onTbpatient_id , 'N/A' ,if( (tbStatus = 1660  and everOnIPT = 1065),'Y','N')) from (\n"
		        + "select fup.patient_id as patientId,\n"
		        + "mid(max(concat(fup.visit_date, fup.tb_status )), 11) as tbStatus,\n"
		        + "mid(max(concat(fup.visit_date, fup.ever_on_ipt )), 11) as everOnIPT,\n"
		        + "fup.visit_date as visitDate,\n"
		        + "onTbpatient_id\n"
		        + "from kenyaemr_etl.etl_patient_hiv_followup fup \n"
		        + " left join (\n"
		        + "    select \n"
		        + "\tc.patient_id  as onTbpatient_id\n"
		        + "    from kenyaemr_etl.etl_tb_enrollment c\n"
		        + "     ) b on fup.patient_id = b.onTbpatient_id\n"
		        + "GROUP BY fup.patient_id ) t\n"
		        + "where  date(visitDate) <=  date(:endDate)";
		
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
