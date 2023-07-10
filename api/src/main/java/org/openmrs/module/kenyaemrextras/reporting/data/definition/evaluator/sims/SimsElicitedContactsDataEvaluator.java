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
 * KHP3-3824:Include PWID alongside sexual partners in S_02_07 Q3. Also Check if there is documentation of HIV test in HTS: TODO If contact is tested, results may need to updated in Patient Contact
 */
@Handler(supports = SimsElicitedContactsDataDefinition.class, order = 50)
public class SimsElicitedContactsDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select d.patient_id,\n" +
				"       IF(relationship is null, 'N/A',\n" +
				"          if(find_in_set('not known', group_concat(case ifnull(c.hivStatus, 'was null')\n" +
				"                                                       when 'was null' then 'not known'\n" +
				"                                                       when '1067' then 'not known'\n" +
				"                                                       when '0' then 'not known'\n" +
				"                                                       when 'Unknown' then 'not known'\n" +
				"                                                       when '664' then 'Negative'\n" +
				"                                                       when '703' then 'Positive'\n" +
				"                                                       when 'Negative' then 'Negative'\n" +
				"                                                       when 'Positive' then 'Positive'\n" +
				"                                                       else c.hivStatus end)) != 0 or\n" +
				"             find_in_set('not known', group_concat(case ifnull(c.hts_status, 'was null')\n" +
				"                                                       when 'was null' then 'not known'\n" +
				"                                                       when 'Inconclusive' then 'not known'\n" +
				"                                                       when 'Negative' then 'Negative'\n" +
				"                                                       when 'Positive' then 'Positive'\n" +
				"                                                       else c.hts_status end)) != 0, 'N', 'Y')) AS hivstatus\n" +
				"from kenyaemr_etl.etl_patient_demographics d\n" +
				"         left join (select c.id                  as contact_id,\n" +
				"                           c.patient_id,\n" +
				"                           c.patient_related_to  as idx_patient_id,\n" +
				"                           c.baseline_hiv_status as hivStatus,\n" +
				"                           c.relationship_type   as relationship,\n" +
				"                           hts.patient_id        as hts_client,\n" +
				"                           hts.final_test_result as hts_status\n" +
				"                    from kenyaemr_etl.etl_patient_contact c\n" +
				"                             left join (select hts.patient_id, hts.final_test_result\n" +
				"                                        from kenyaemr_etl.etl_hts_test hts\n" +
				"                                        where date(hts.visit_date) <= date(:endDate)) hts\n" +
				"                                       on c.patient_id = hts.patient_id\n" +
				"                    where c.relationship_type in (162221, 163565, 5617, 157351)\n" +
				"                      and c.voided = 0) c on d.patient_id = c.idx_patient_id\n" +
				"group by d.patient_id;";
		
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
