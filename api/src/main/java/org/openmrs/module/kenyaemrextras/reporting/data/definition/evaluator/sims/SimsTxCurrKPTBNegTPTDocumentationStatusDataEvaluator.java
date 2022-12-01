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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsTxCurrKPTBNegTPTDocumentationStatusDataDefinition;
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
 * Evaluates whether TX_CURR KPs screened negative for TB had been initiated on TPT/IPT or not
 */
@Handler(supports = SimsTxCurrKPTBNegTPTDocumentationStatusDataDefinition.class, order = 50)
public class SimsTxCurrKPTBNegTPTDocumentationStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if(a.on_anti_tb_drugs = 1065 or a.tb_status in (142177, 1662, 1111), 'NA',\n"
		        + "          if(a.tb_status = 1660 and ((ipt_initiation_date = fup_date or on_ipt = 1065) or\n"
		        + "                                      (ipt_client is not null and (ipt_initiation_date > date(disc_date) or\n"
		        + "                                      date(disc_date) > date(:endDate) or disc_patient is null) )), 'Y', 'N')) as ipt_status\n"
		        + "from (select f.patient_id                                                 as patient_id\n"
		        + "           , max(f.visit_date)                                            as fup_date\n"
		        + "           , mid(max(concat(date(f.visit_date), f.screened_for_tb)), 11)  as screened_for_tb\n"
		        + "           , mid(max(concat(date(f.visit_date), f.on_anti_tb_drugs)), 11) as on_anti_tb_drugs\n"
		        + "           , mid(max(concat(date(f.visit_date), f.on_ipt)), 11)           as on_ipt\n"
		        + "           , mid(max(concat(date(f.visit_date), f.tb_status)), 11)        as tb_status\n"
		        + "           , i.patient_id                                                 as ipt_client\n"
		        + "           , i.ipt_date                                                   as ipt_initiation_date\n"
		        + "           , i.disc_date                                                  as disc_date\n"
		        + "           , i.disc_patient                                                 as disc_patient\n"
		        + "      from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "               left join (select i.patient_id,\n"
		        + "                                 max(i.visit_date)                             as ipt_date,\n"
		        + "                                 coalesce(d.effective_disc_date, d.visit_date) as disc_date,\n"
		        + "                                 d.disc_patient as disc_patient\n"
		        + "                          from kenyaemr_etl.etl_ipt_initiation i\n"
		        + "                                   left join (select patient_id      as disc_patient,\n"
		        + "                                                     max(visit_date) as visit_date,\n"
		        + "                                                     mid(\n"
		        + "                                                             max(concat(date(visit_date), date(effective_discontinuation_date))),\n"
		        + "                                                             11)     as effective_disc_date\n"
		        + "                                              from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "                                              where date(visit_date) <= date(:endDate)\n"
		        + "                                                and program_name = 'TPT'\n"
		        + "                                              group by patient_id) d on i.patient_id = d.disc_patient\n"
		        + "                          where i.visit_date <= date(:endDate)\n"
		        + "                          group by i.patient_id) i on f.patient_id = i.patient_id\n"
		        + "      where date(f.visit_date) <= date(:endDate)\n" + "             and f.person_present = 978\n"
		        + "      group by f.patient_id\n" + "      having screened_for_tb = 'Yes') a;";
		
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
