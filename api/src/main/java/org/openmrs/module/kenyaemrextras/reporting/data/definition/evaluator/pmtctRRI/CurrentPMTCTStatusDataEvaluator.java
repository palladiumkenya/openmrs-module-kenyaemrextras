/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.evaluator.pmtctRRI;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.pmtctRRI.CurrentPMTCTStatusDataDefinition;
import org.openmrs.module.kenyaemrextras.reporting.data.definition.pmtctRRI.DateOfMCHEnrollmentDataDefinition;
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
 * Evaluates Current PMTCT status Data Definition
 */
@Handler(supports = CurrentPMTCTStatusDataDefinition.class, order = 50)
public class CurrentPMTCTStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select f.patient_id,\n"
		        + " if(mid(max(concat(f.visit_date, f.pregnancy_status)), 11) = 1065 or c.anc_client, 'Current pregnant',\n"
		        + "    if(mid(max(concat(f.visit_date, f.breastfeeding)), 11) = 1065 or c.baby_feeding_method in (5526, 6046), 'Current breastfeeding',\n"
		        + "       if((mid(max(concat(f.visit_date, f.pregnancy_status)), 11) = 1065 and mid(max(concat(f.visit_date, f.breastfeeding)), 11) = 1065)\n"
		        + "              or (c.anc_client and  c.baby_feeding_method in (5526, 6046)), 'Current pregnant and breastfeeding'\n"
		        + "                    ,null)))as pmtct_status\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "      left join (select c.patient_id,\n"
		        + "                   max(c.visit_date)     as latest_mch_enrollment,\n"
		        + "                   m.visit_date          as disc_visit,\n"
		        + "                   m.effective_disc_date as effective_disc_date,\n"
		        + "                   m.patient_id          as disc_client,\n"
		        + "                   p.baby_feeding_method,\n"
		        + "                   a.patient_id          as anc_client\n"
		        + "                 from kenyaemr_etl.etl_mch_enrollment c\n"
		        + "                   left join (select p.patient_id,\n"
		        + "                                max(p.visit_date)                                         as latest_visit,\n"
		        + "                                mid(max(concat(p.visit_date, p.baby_feeding_method)), 11) as baby_feeding_method\n"
		        + "                              from kenyaemr_etl.etl_mch_postnatal_visit p\n"
		        + "                              where p.visit_date <= date(:endDate)\n"
		        + "                              group by p.patient_id) p on p.patient_id = c.patient_id\n"
		        + "                   left join (select a.patient_id, max(a.visit_date) as latest_visit\n"
		        + "                              from kenyaemr_etl.etl_mch_antenatal_visit a\n"
		        + "                              where a.visit_date <= date(:endDate) and a.maturity <= 42 or maturity is null\n"
		        + "                              group by a.patient_id) a on a.patient_id = c.patient_id\n"
		        + "                   left join (select patient_id,\n"
		        + "                                max(visit_date) as visit_date,\n"
		        + "                                mid(\n"
		        + "                                    max(concat(date(visit_date), date(effective_discontinuation_date))),\n"
		        + "                                    11)     as effective_disc_date\n"
		        + "                              from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                              where date(visit_date) <= date(:endDate)\n"
		        + "                                    and program_name = 'MCH-Mother Services'\n"
		        + "                              group by patient_id) m on c.patient_id = m.patient_id\n"
		        + "                 where c.visit_date <= date(:endDate)\n" + "                 group by c.patient_id\n"
		        + "                 having (disc_client is null or\n"
		        + "                         (latest_mch_enrollment > coalesce(effective_disc_date, disc_visit)))) c\n"
		        + "        on f.patient_id = c.patient_id\n" + "    group by f.patient_id;\n";
		
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
