/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.sims;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsNewHIVPosLinkageToTreatmentDataDefinition;
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
 * S_07_03 Q4: HTS Linkage to HIV Care and Treatment at the Site Level: Does the chart record
 * successful linkage to treatment/ART?
 */
@Handler(supports = SimsNewHIVPosLinkageToTreatmentDataDefinition.class, order = 50)
public class SimsNewHIVPosLinkageToTreatmentDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id, if(a.art_client is not null, 'Y', 'N') as on_art\n"
		        + "from (select dem.patient_id,\n"
		        + "             disc.effective_disc_date as disc_date,\n"
		        + "             max(e.visit_date)        as enr_date,\n"
		        + "             d.patient_id             as art_client,\n"
		        + "             disc.patient_id          as disc_patient\n"
		        + "      from kenyaemr_etl.etl_patient_demographics dem\n"
		        + "               left join kenyaemr_etl.etl_hiv_enrollment e on dem.patient_id = e.patient_id\n"
		        + "               left join (select d.patient_id,\n"
		        + "                                 mid(max(concat(d.visit_date, d.date_started)), 11) as date_started\n"
		        + "                          from kenyaemr_etl.etl_drug_event d\n"
		        + "                          where d.program = 'HIV'\n"
		        + "                            and d.date_started <= date(:endDate)\n"
		        + "                            and d.discontinued is null\n"
		        + "                          group by d.patient_id) d\n"
		        + "                         on dem.patient_id = d.patient_id\n"
		        + "               left join (select patient_id,\n"
		        + "                                 max(visit_date) as visit_date,\n"
		        + "                                 mid(max(concat(date(visit_date), date(effective_discontinuation_date))),\n"
		        + "                                     11)         as effective_disc_date\n"
		        + "                          from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                          where date(visit_date) <= date(:endDate)\n"
		        + "                            and program_name = 'TB'\n"
		        + "                          group by patient_id) disc on dem.patient_id = disc.patient_id\n"
		        + "      group by dem.patient_id) a\n" + "where (a.disc_date > a.enr_date\n"
		        + "   or a.disc_patient is null);";
		
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
