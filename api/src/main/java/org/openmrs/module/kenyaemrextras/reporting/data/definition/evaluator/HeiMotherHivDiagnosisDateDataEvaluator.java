/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiMotherHivDiagnosisDateDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates Diagnosis Date
 */
@Handler(supports = HeiMotherHivDiagnosisDateDataDefinition.class, order = 50)
public class HeiMotherHivDiagnosisDateDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select distinct r.person_a,\n"
		        + "  coalesce(date(en.date_confirmed_hiv_positive),date(ht.visit_date),date(mch.hiv_test_date)) as hiv_diagnosis_date\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join openmrs.relationship r on d.patient_id = r.person_b\n"
		        + "  inner join openmrs.relationship_type t on r.relationship = t.relationship_type_id and t.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'\n"
		        + "  inner join kenyaemr_etl.etl_hiv_enrollment en on en.patient_id = d.patient_id\n"
		        + "  left join kenyaemr_etl.etl_mch_enrollment mch on mch.patient_id = d.patient_id and mch.hiv_status = 703\n"
		        + "  left join kenyaemr_etl.etl_hts_test ht on ht.patient_id = d.patient_id and ht.final_test_result = 'Positive';";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
