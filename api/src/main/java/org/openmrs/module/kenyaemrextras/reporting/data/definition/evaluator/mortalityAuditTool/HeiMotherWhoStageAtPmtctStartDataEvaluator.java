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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.HeiMotherWhoStageAtPmtctStartDataDefinition;
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
 * Evaluates Gestation Age
 */
@Handler(supports = HeiMotherWhoStageAtPmtctStartDataDefinition.class, order = 50)
public class HeiMotherWhoStageAtPmtctStartDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select distinct r.person_a,\n"
		        + "  (case coalesce(v.who_stage,fup.who_stage) when 1204 then \"WHO Stage1\" when 1205 then \"WHO Stage2\" when 1206 then \"WHO Stage3\" when 1207 then \"WHO Stage4\" else \"\" end)  as who_stage\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join relationship r on d.patient_id = r.person_b\n"
		        + "  inner join relationship_type t on r.relationship = t.relationship_type_id and t.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'\n"
		        + "  inner join kenyaemr_etl.etl_mch_enrollment mch on mch.patient_id = d.patient_id and mch.hiv_status = 703\n"
		        + "  left  join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = d.patient_id and date(fup.visit_date) = date(mch.visit_date)\n"
		        + "  left join kenyaemr_etl.etl_mch_antenatal_visit v on v.patient_id = d.patient_id and v.anc_visit_number = 1;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
