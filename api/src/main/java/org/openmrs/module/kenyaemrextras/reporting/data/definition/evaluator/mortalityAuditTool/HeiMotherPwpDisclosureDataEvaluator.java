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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.HeiMotherPwpDisclosureDataDefinition;
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
 * Evaluates Pwp Disclosure
 */
@Handler(supports = HeiMotherPwpDisclosureDataDefinition.class, order = 50)
public class HeiMotherPwpDisclosureDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select distinct r.person_a,\n"
		        + "  (case max(fup.pwp_disclosure) when 1065 then 'Disclosed' when 1066 then 'Not Disclosed' when 1067 then 'Unknown' else \"\" end) as mother_disclosure_status\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join openmrs.relationship r on d.patient_id = r.person_b\n"
		        + "  inner join openmrs.relationship_type t on r.relationship = t.relationship_type_id and t.uuid in ('8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n"
		        + "  inner join kenyaemr_etl.etl_mch_enrollment mch on mch.patient_id = d.patient_id and mch.hiv_status = 703\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = r.person_a\n"
		        + "                                                                      and  disc.program_name in ('MCH Child HEI','MCH Child') and disc.discontinuation_reason = 160432\n"
		        + "  left  join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = d.patient_id and  fup.visit_date <= coalesce(date(disc.date_died),date(disc.visit_date),date(disc.effective_discontinuation_date))\n"
		        + "group by d.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
