/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.mortalityAuditTool;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.MoriskyMedicationAdherenceDataDefinition;
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
 * Evaluates MoriskyMedicationAdherenceDataDefinition
 */
@Handler(supports = MoriskyMedicationAdherenceDataDefinition.class, order = 50)
public class MoriskyMedicationAdherenceDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,coalesce(f.last_fup_visit_mmas,a.last_adherence_mmas) as mmas4\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "         left join (select f.patient_id,\n"
		        + "                           left(max(concat(f.visit_date, f.arv_adherence)), 10) as last_fup_visit_mmas_date,\n"
		        + "                           (case mid(max(concat(f.visit_date, f.arv_adherence)), 11)\n"
		        + "                                when 159405 then 'Good'\n"
		        + "                                when 163794 then 'Inadequate'\n"
		        + "                                when 159407 then 'Poor' end)                    as last_fup_visit_mmas\n"
		        + "                    from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "                    group by f.patient_id) f on d.patient_id = f.patient_id\n"
		        + "         left join (select a.patient_id,\n"
		        + "                           left(max(concat(a.visit_date, a.arv_adherence)), 10) as last_adherence_mmas_date,\n"
		        + "                           mid(max(concat(a.visit_date, a.arv_adherence)), 11)  as last_adherence_mmas\n"
		        + "                    from kenyaemr_etl.etl_enhanced_adherence a) a on d.patient_id = a.patient_id;\n";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
