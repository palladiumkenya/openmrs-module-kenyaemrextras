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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsChildListedAsContactDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTBScreeningResultDataDefinition;
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
 * Evaluates whether child listed as a contact
 */
@Handler(supports = SimsChildListedAsContactDataDefinition.class, order = 50)
public class SimsChildListedAsContactDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id, IF(relationship is null, 'N/A',\n"
		        + "if(find_in_set('not known', group_concat(case ifnull(t.hivStatus, 'was null')\n"
		        + " when 'was null' then 'not known' when '1067' then 'not known' when '0' then 'not known'\n"
		        + " when 'Unknown' then 'not known' when '664' then 'Negative' when '703' then 'Positive'\n"
		        + " when 'Negative' then 'Negative' when 'Positive' then 'Positive'\n"
		        + " else t.hivStatus end)) != 0, 'N', 'Y')) AS hivstatus\n"
		        + "from kenyaemr_etl.etl_patient_demographics d\n"
		        + "left join (\n"
		        + "select c.patient_id    as contact_id, c.patient_related_to  as patient_id,c.baseline_hiv_status as hivStatus, c.relationship_type   as relationship\n"
		        + "from kenyaemr_etl.etl_patient_contact c\n" + "where c.relationship_type =1528\n"
		        + "and c.voided = 0) t on d.patient_id = t.patient_id\n" + "group by d.patient_id;";
		
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
