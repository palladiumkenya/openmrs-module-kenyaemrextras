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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHEIDocumentedLinkageToTreatmentDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsHEIStartedCtxBy8WeeksDataDefinition;
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
 * Evaluates whether HEI has documented linkage to treatment
 */
@Handler(supports = SimsHEIDocumentedLinkageToTreatmentDataDefinition.class, order = 50)
public class SimsHEIDocumentedLinkageToTreatmentDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id,\n"
		        + "  if((enr.patient_id is not null or de.patient_id is not null) ,'Y','N')\n"
		        + "from kenyaemr_etl.etl_hei_enrollment e\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = e.patient_id\n"
		        + "  left join (select hv.patient_id,hv.visit_date  from kenyaemr_etl.etl_hiv_enrollment hv\n"
		        + "        where hv.visit_date <= date(:endDate) group by hv.patient_id) enr on e.patient_id = enr.patient_id\n"
		        + "  left join (select dr.patient_id,dr.visit_date  from kenyaemr_etl.etl_drug_event dr\n"
		        + "       where dr.visit_date <= date(:endDate) group by dr.patient_id) de on e.patient_id = de.patient_id\n"
		        + "group by e.patient_id;";
		
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
