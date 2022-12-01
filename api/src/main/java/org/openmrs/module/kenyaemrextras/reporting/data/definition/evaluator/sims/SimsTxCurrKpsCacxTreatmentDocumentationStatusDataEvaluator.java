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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition;
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
 * Evaluates whether Tx_Curr KPS who were screened for cervical cancer and their treatment status
 */
@Handler(supports = SimsTxCurrKPsCacxTreatmentDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrKpsCacxTreatmentDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if(a.kp_scr_patient is null or a.scr_patient is null, 'NA',\n"
		        + "          if((kp_cacx_results = 'Positive' and (a.kp_cacx_treated = 'Yes' or a.kp_cacx_referred = 'Yes'))\n"
		        + "                 or (a.scrn_result = 'Positive' and\n"
		        + "                     (a.scrn_treatment_method in ('Cryotherapy performed', 'LEEP', 'Cold knife cone',\n"
		        + "                                                  'Thermocoagulation', 'Cryotherapy performed (single Visit)',\n"
		        + "                                                  'Hysterectomy', 'Referred for cancer treatment') or\n"
		        + "                      trim(a.scrn_other_treatment_method) is not null)), 'Y', 'N'))\n"
		        + "from (select e.patient_id as patient_id,\n"
		        + "             s.patient_id as scr_patient,\n"
		        + "             v.client_id  as kp_scr_patient,\n"
		        + "             scrn_result,\n"
		        + "             scrn_treatment_method,\n"
		        + "             scrn_other_treatment_method,\n"
		        + "             kp_cacx_screened,\n"
		        + "             kp_cacx_results,\n"
		        + "             kp_cacx_referred,\n"
		        + "             kp_cacx_treated\n"
		        + "      from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "               left join (select s.patient_id,\n"
		        + "                                 mid(max(concat(s.visit_date, s.screening_result)), 11)       as scrn_result,\n"
		        + "                                 mid(max(concat(s.visit_date, s.treatment_method)), 11)       as scrn_treatment_method,\n"
		        + "                                 mid(max(concat(s.visit_date, s.treatment_method_other)), 11) as scrn_other_treatment_method\n"
		        + "                          from kenyaemr_etl.etl_cervical_cancer_screening s\n"
		        + "                          where date(s.visit_date) between date_sub(date(:endDate), INTERVAL 90 DAY) and date(:endDate)\n"
		        + "                          group by s.patient_id) s\n"
		        + "                         on e.patient_id = s.patient_id\n"
		        + "               left join (select v.client_id,\n"
		        + "                                 mid(max(concat(v.visit_date, v.cerv_cancer_screened)), 11) as kp_cacx_screened,\n"
		        + "                                 mid(max(concat(v.visit_date, v.cerv_cancer_results)), 11)  as kp_cacx_results,\n"
		        + "                                 mid(max(concat(v.visit_date, v.cerv_cancer_referred)), 11) as kp_cacx_referred,\n"
		        + "                                 mid(max(concat(v.visit_date, v.cerv_cancer_treated)), 11)  as kp_cacx_treated\n"
		        + "                          from kenyaemr_etl.etl_clinical_visit v\n"
		        + "                          where date(v.visit_date) between date_sub(date(:endDate), INTERVAL 90 DAY) and date(:endDate)\n"
		        + "                          group by v.client_id) v\n"
		        + "                         on e.patient_id = v.client_id\n" + "      group by e.patient_id\n"
		        + "      having max(e.visit_date) <= date(:endDate)) a;";
		
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
