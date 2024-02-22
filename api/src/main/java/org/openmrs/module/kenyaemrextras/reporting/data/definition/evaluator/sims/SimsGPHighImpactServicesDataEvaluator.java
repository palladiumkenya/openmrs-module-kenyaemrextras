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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsGPHighImpactServicesDataDefinition;
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
 * Evaluates High impact services data definition
 */
@Handler(supports = SimsGPHighImpactServicesDataDefinition.class, order = 50)
public class SimsGPHighImpactServicesDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n"
		        + "        if(cacx_client is not null or vmmc_client is not null or sti_client is not null, 'Y',\n"
		        + "           'N') as received_high_impact_service\n"
		        + " from kenyaemr_etl.etl_patient_demographics d\n"
		        + "          left join (select cacx.patient_id as cacx_client, cacx.visit_date as date_screened\n"
		        + "                     from kenyaemr_etl.etl_cervical_cancer_screening cacx\n"
		        + "                     where (cacx.via_vili_screening_result in ('Positive', 'Suspicious for Cancer') or cacx.pap_smear_screening_result in ('High grade lesion','Invasive Cancer','Atypical squamous cells(ASC-US/ASC-H)','AGUS'))\n"
		        + "                       and (((cacx.pap_smear_screening_method is not null and cacx.pap_smear_screening_method is not null != 'None') or (cacx.via_vili_treatment_method is not null and cacx.via_vili_treatment_method !='None')) or cacx.referred_out = 'Yes')\n"
		        + "                       and date(cacx.visit_date) between DATE_SUB(DATE(:endDate), INTERVAL 3 MONTH) and date(:endDate)) cacx\n"
		        + "                    on d.patient_id = cacx.cacx_client\n"
		        + "          left join (select v.patient_id as vmmc_client, v.visit_date\n"
		        + "                     from kenyaemr_etl.etl_vmmc_circumcision_procedure v\n"
		        + "                     where date(v.visit_date) between DATE_SUB(DATE(:endDate), INTERVAL 3 MONTH) and date(:endDate)) v\n"
		        + "                    on d.patient_id = v.vmmc_client\n"
		        + "          left join (select f.patient_id as sti_client, f.visit_date, f.screened_for_sti\n"
		        + "                     from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                     where f.screened_for_sti in (664, 703)\n"
		        + "                       and date(f.visit_date) between DATE_SUB(DATE(:endDate), INTERVAL 3 MONTH) and date(:endDate)) f\n"
		        + "                    on d.patient_id = f.sti_client;\n";
		
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
