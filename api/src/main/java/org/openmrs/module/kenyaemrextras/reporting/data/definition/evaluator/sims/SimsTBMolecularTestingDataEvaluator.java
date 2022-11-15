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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTBMolecularTestingDataDefinition;
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
 * Evaluates current on art who are presumed to have TB document receipt of molecular testing as
 * their first-line diagnostic test
 */
@Handler(supports = SimsTBMolecularTestingDataDefinition.class, order = 50)
public class SimsTBMolecularTestingDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select t.patient_id, if(genexpert_ordered = 162202 or  spatum_smear_ordered = 307 or lab_test in (162202,1465,307), 'Y','N') from (\n"
		        + "  select fup.patient_id,lab_test,\n"
		        + "  mid(max(concat(fup.visit_date,fup.genexpert_ordered)),11) as genexpert_ordered,\n"
		        + "  mid(max(concat(fup.visit_date,fup.spatum_smear_ordered )),11) as spatum_smear_ordered\n"
		        + "  from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "  left join (\n"
		        + "  select patient_id, mid(max(concat(x.visit_date,x.lab_test)),11) as lab_test\n"
		        + "    from kenyaemr_etl.etl_laboratory_extract x \n"
		        + "    where x.lab_test in (162202,1465,307) and x.visit_date <= date(:endDate)\n"
		        + "    group by x.patient_id\n"
		        + "  ) l on fup.patient_id = l.patient_id\n"
		        + "  where fup.visit_date <= date(:endDate)\n"
		        + "    GROUP BY fup.patient_id\n"
		        + "    having genexpert_ordered = 162202 or  spatum_smear_ordered = 307\n" + "  )t";
		
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
