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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsElicitedContactsDataDefinition;
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
 * Evaluates HIV-positive adult and adolescent patients aged ≥15 years who have been on ART for ≥12
 * months, who elicited contacts and have their contacts HIV status documented or their HIV testing
 * documented.
 */
@Handler(supports = SimsElicitedContactsDataDefinition.class, order = 50)
public class SimsElicitedContactsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id, IF(relationship is null, 'N/A', \n"
		        + "         if(find_in_set('not known', group_concat(case ifnull(t.hivStatus, 'was null') \n"
		        + "         when 'was null' then 'not known' when '1067' then 'not known' when '0' then 'not known' \n"
		        + "         when 'Unknown' then 'not known' when '664' then 'Negative' when '703' then 'Positive'\n"
		        + "         when 'Negative' then 'Negative' when 'Positive' then 'Positive' \n"
		        + "         else t.hivStatus end)) != 0, 'N', 'Y')) AS hivstatus\n"
		        + "         from kenyaemr_etl.etl_patient_demographics d          left join ( \n"
		        + "             select c.id    as contact_id,            c.patient_related_to  as patient_id, \n"
		        + "                    c.baseline_hiv_status as hivStatus,            c.relationship_type   as relationship \n"
		        + "             from kenyaemr_etl.etl_patient_contact c \n"
		        + "             where c.relationship_type in (971, 972, 1528, 162221, 163565, 970, 5617) \n"
		        + "               and c.voided = 0) t on d.patient_id = t.patient_id\n" + "group by d.patient_id;";
		
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
