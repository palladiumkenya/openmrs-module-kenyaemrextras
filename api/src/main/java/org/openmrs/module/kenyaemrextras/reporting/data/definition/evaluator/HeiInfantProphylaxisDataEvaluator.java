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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiFinalAntibodyTestResultDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.HeiInfantProphylaxisDataDefinition;
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
 * Evaluates Final Antibody Test Result DataDefinition
 */
@Handler(supports = HeiInfantProphylaxisDataDefinition.class, order = 50)
public class HeiInfantProphylaxisDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select  en.patient_id,\n" + "  (case en.infant_prophylaxis when 80586 then \"NVP only\"\n"
		        + "                               when 1652 then \"NVP and AZT\"\n"
		        + "                               when 1149 then \"NVP\"\n"
		        + "                               when 160123 then \"AZT only\"\n"
		        + "                               when 1107 then \"None\"  else \"\" end) as infant_prophylaxis\n"
		        + "from kenyaemr_etl.etl_hei_enrollment en\n"
		        + "   inner join  kenyaemr_etl.etl_patient_demographics d on d.patient_id = en.patient_id\n"
		        + "   inner join kenyaemr_etl.etl_patient_program_discontinuation disc on disc.patient_id = en.patient_id\n"
		        + "    where  disc.program_name in ('MCH Child HEI','MCH Child') and disc.discontinuation_reason = 160432\n"
		        + "group by en.patient_id;";
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
