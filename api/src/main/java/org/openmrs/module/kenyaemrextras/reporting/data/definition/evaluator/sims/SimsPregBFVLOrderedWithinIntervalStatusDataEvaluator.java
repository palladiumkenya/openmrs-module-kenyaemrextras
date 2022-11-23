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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsPregBFVLOrderedWithinIntervalStatusDataDefinition;
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
 * Evalueates whether a Viral load ordered within the appropriate interval per national guidelines
 * for Pregnant and Breastfeeding 1. for Known positive - immediately they test positive for
 * pregnancy 2. routine Vl - Every 6 months
 */
@Handler(supports = SimsPregBFVLOrderedWithinIntervalStatusDataDefinition.class, order = 50)
public class SimsPregBFVLOrderedWithinIntervalStatusDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select a.patient_id,\n"
		        + "       if((a.preg_status = 1066 or a.preg_status is null) and a.preg_client is null,'NA',if((IFNULL(a.date_tested_preg_positive, '0001-00-00') = IFNULL(a.vl_date, '0000-00-00')) or\n"
		        + "          (a.preg_status = 1065 and a.latest_visit = ifnull(a.vl_date, '0000-00-00')), 'Y', 'N'))\n"
		        + "from (select f.patient_id,\n"
		        + "             max(f.visit_date)                                      as latest_visit,\n"
		        + "             mid(max(concat(f.visit_date, f.pregnancy_status)), 11) as preg_status,\n"
		        + "             mid(max(concat(f.visit_date, f.breastfeeding)), 11) as bf_status,\n"
		        + "             x.preg_lab_date as date_tested_preg_positive,\n"
		        + "             x.patient_id                                           as preg_client,\n"
		        + "             vl.patient_id                                          as vl_client,\n"
		        + "             vl.latest_vl_lab_date                                  as vl_date\n"
		        + "          from kenyaemr_etl.etl_patient_hiv_followup f\n"
		        + "               left join (select x.patient_id, date(x.visit_date) as preg_lab_date\n"
		        + "                          from kenyaemr_etl.etl_laboratory_extract x\n"
		        + "                          where date(x.visit_date) between date(:startDate) and date(:endDate)\n"
		        + "                            and x.lab_test = 45\n"
		        + "                            and x.test_result = 703\n"
		        + "                          group by x.patient_id) x on f.patient_id = x.patient_id\n"
		        + "               left join (select x.patient_id,\n"
		        + "                                 max(date(x.date_activated)) as latest_vl_lab_date -- ,mid(max(concat(visit_date,lab_test)),11) as lab_test\n"
		        + "                          from openmrs.orders x\n"
		        + "                          where date(x.date_activated) between date(:startDate) and date(:endDate)\n"
		        + "                            and x.concept_id in (1305, 856) and x.order_reason in (1434,159882)\n"
		        + "                          group by x.patient_id) vl\n"
		        + "                         on x.patient_id = vl.patient_id and x.preg_lab_date = vl.latest_vl_lab_date\n"
		        + "             /*  left join (select r.patient_id,\n"
		        + "                                 max(date(r.date_activated)) as max_vl_lab_date,\n"
		        + "                                 date(r.date_activated) as vl_date\n"
		        + "                          from openmrs.orders r\n"
		        + "                          where date(r.date_activated) between date(:startDate) and date(:endDate)\n"
		        + "                            and r.concept_id in (1305, 856) and r.order_reason in (1434,159882)\n"
		        + "                          group by r.patient_id) rvl\n"
		        + "                         on x.patient_id = rvl.patient_id and (rvl.vl_date > x.preg_lab_date or rvl.vl_date > max(f.visit_date))*/\n"
		        + "      where date(f.visit_date) <= date(:endDate)\n" + "      group by f.patient_id) a;";
		
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
