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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.HeiMotherDateArtInitiatedDataDefinition;
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
 * Evaluates Hei Mother Date Art Initiated DataDefinition
 */
@Handler(supports = HeiMotherDateArtInitiatedDataDefinition.class, order = 50)
public class HeiMotherDateArtInitiatedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select distinct r.person_a,\n"
		        + "  coalesce(date(de.date_started),date(v.date_given_haart),date(ld.visit_date),date(pnc.visit_date)) as date_initiated_art\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "  inner join relationship r on d.patient_id = r.person_b\n"
		        + "  inner join relationship_type t on r.relationship = t.relationship_type_id and t.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'\n"
		        + "  inner join kenyaemr_etl.etl_mch_enrollment mch on mch.patient_id = d.patient_id\n"
		        + "  left  join kenyaemr_etl.etl_drug_event de on de.patient_id = d.patient_id\n"
		        + "  left join kenyaemr_etl.etl_mch_antenatal_visit v on v.patient_id = d.patient_id\n"
		        + "  left join kenyaemr_etl.etl_mchs_delivery ld on ld.patient_id = d.patient_id and ld.mother_started_haart_at_maternity = 1065\n"
		        + "  left join kenyaemr_etl.etl_mch_postnatal_visit pnc on pnc.patient_id = d.patient_id and pnc.mother_haart_given = 1065;\n";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
