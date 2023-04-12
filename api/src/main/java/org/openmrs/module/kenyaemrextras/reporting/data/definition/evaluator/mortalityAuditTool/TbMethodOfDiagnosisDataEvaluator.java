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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbMethodOfDiagnosisDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.mortalityAuditTool.TbPatientSourceDataDefinition;
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
 * TB Method of Diagnosis Evaluator
 */
@Handler(supports = TbMethodOfDiagnosisDataDefinition.class, order = 50)
public class TbMethodOfDiagnosisDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select tb.patient_id,\n"
		        + "    (case coalesce(tb.genexpert_result,tb.spatum_smear_result,tb.chest_xray_result,tb.clinical_tb_diagnosis)\n"
		        + "                                when 703 then 'AFB Smear Microscopy'\n"
		        + "                                when 162203 then 'Gene X pert'\n"
		        + "                                when 162204 then 'Gene X pert'\n"
		        + "                                when 164104 then 'Gene X pert'\n"
		        + "                                when 152526 then 'X ray'  else '' end) as diagnosis_method\n"
		        + "from  kenyaemr_etl.etl_tb_screening tb\n"
		        + "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = tb.patient_id\n"
		        + "  inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = tb.patient_id\n"
		        + "   and  disc.discontinuation_reason in (160432,160034)\n"
		        + "where (tb.chest_xray_result = 152526 or tb.genexpert_result in (162203,162204,164104) or tb.spatum_smear_result = 703 or tb.clinical_tb_diagnosis =703)\n"
		        + "group by tb.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
