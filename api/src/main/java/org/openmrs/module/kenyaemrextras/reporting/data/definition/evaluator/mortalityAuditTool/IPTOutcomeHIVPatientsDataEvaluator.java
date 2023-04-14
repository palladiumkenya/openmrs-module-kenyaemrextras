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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.IPTOutcomeHIVPatientsDataDefinition;
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
 * Evaluates IPT Outcome Data Definition
 */
@Handler(supports = IPTOutcomeHIVPatientsDataDefinition.class, order = 50)
public class IPTOutcomeHIVPatientsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id,\n"
		        + "              if(i.patient_id is not null, case o.outcome\n"
		        + "                                               when 1267 then 'Treatment completed'\n"
		        + "                                               when 5240 then 'Lost to followup'\n"
		        + "                                               when 159836 then 'Discontinued'\n"
		        + "                                               when 160034 then 'Died'\n"
		        + "                                               when 159492 then 'Transferred Out'\n"
		        + "                                               when 112141 then 'Active TB Disease - ATB'\n"
		        + "                                               when 102 then 'Adverse drug reaction - ADR'\n"
		        + "                                               when 159598 then 'Poor adherence - PA'\n"
		        + "                                               when 5622 then 'Others - OTR' else 'Died' end, NULL) as tpt_outcome\n"
		        + "       from kenyaemr_etl.etl_hiv_enrollment e\n"
		        + "           inner join (select d.patient_id from kenyaemr_etl.etl_patient_program_discontinuation d where d.discontinuation_reason = 160034 and d.program_name = 'HIV')d on e.patient_id = d.patient_id\n"
		        + "                left join kenyaemr_etl.etl_ipt_initiation i on e.patient_id = i.patient_id\n"
		        + "                left join (select o.patient_id, o.outcome, o.visit_date from kenyaemr_etl.etl_ipt_outcome o) o\n"
		        + "                          on e.patient_id = o.patient_id;\n";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
