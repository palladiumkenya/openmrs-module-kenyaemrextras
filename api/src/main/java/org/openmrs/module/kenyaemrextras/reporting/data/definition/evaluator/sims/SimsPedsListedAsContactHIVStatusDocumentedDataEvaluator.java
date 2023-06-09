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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsPedsListedAsContactsHIVStatusDocumentedDataDefinition;
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
 * Evaluates whether child listed as a contact has HIV status document
 */
@Handler(supports = SimsPedsListedAsContactsHIVStatusDocumentedDataDefinition.class, order = 50)
public class SimsPedsListedAsContactHIVStatusDocumentedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id,\n"
		        + "  if((hivStatus = \"Unknown\" and testResult is null) or (hivStatus is null and testResult is null)\n"
		        + "  or (hivStatus =\"0\" and testResult is null), 'N','Y'  )  from (\n" + "select c.id    as contact_id,\n"
		        + "c.patient_related_to  as patient_related_to,\n" + "c.patient_id  as patient_id,\n"
		        + "c.baseline_hiv_status as hivStatus,\n" + "c.relationship_type   as relationship,\n"
		        + "c.date_created as date_created,\n" + "l.test_1_result as testResult\n"
		        + "from kenyaemr_etl.etl_patient_contact c\n" + "left join (\n"
		        + "  select patient_id,test_1_result  from kenyaemr_etl.etl_hts_test h\n"
		        + ") l on c.patient_id = l.patient_id ) t\n" + "where patient_id is not null and patient_id != 0";
		
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
