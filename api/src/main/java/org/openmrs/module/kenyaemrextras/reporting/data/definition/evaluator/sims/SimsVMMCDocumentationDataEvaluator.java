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
import org.openmrs.module.kenyaemrextras.reporting.data.definition.sims.SimsVMMCDocumentationDataDefinition;
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
 * Precision and safeguarding of VMMC Clinical Records: Do the records contain the following:
 * complete contact details, history and physical exam, weight, Blood Pressure, surgical method,
 * follow-up date and presence/absence of Adverse Events, and stored in a locked location?
 */
@Handler(supports = SimsVMMCDocumentationDataDefinition.class, order = 50)
public class SimsVMMCDocumentationDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select patient_id,\n"
		        + "if(temperature is not null and next_appointment_date is not null  and surgical_method is not null \n"
		        + "and (blood_pressure1 is not null or blood_pressure2 is not null ) and clientWeight is not null , 'Y','N')\n"
		        + "from (\n"
		        + "select e.patient_id,\n"
		        + "COALESCE(h.temperature,n.temperature) as temperature,c.surgical_circumcision_method as surgical_method,\n"
		        + "tr.clientWeight as clientWeight,h.blood_pressure as blood_pressure1, n.blood_pressure as blood_pressure2,\n"
		        + "n.next_appointment_date,cf.has_adverse_event_followup,c.has_adverse_event_procedure\n"
		        + "from kenyaemr_etl.etl_vmmc_enrolment e\n"
		        + "join kenyaemr_etl.etl_vmmc_medical_history h on e.patient_id = h.patient_id\n"
		        + "left outer join (\n"
		        + "  select p.patient_id, \n"
		        + "  mid(max(concat(p.visit_date,  p.surgical_circumcision_method )), 11) as surgical_circumcision_method,\n"
		        + "  mid(max(concat(p.visit_date,  p.has_adverse_event )), 11) as has_adverse_event_procedure\n"
		        + "  from kenyaemr_etl.etl_vmmc_circumcision_procedure p\n"
		        + "  group by p.patient_id\n"
		        + ") c on h.patient_id = c.patient_id\n"
		        + "left outer join (\n"
		        + "  select a.patient_id,\n"
		        + "    mid(max(concat(a.visit_date,  a.temperature )), 11) as temperature,\n"
		        + "    mid(max(concat(a.visit_date, a.blood_pressure )), 11) as blood_pressure,\n"
		        + "    mid(max(concat(a.visit_date, a.next_appointment_date )), 11) as next_appointment_date \n"
		        + "   from kenyaemr_etl.etl_vmmc_post_operation_assessment a\n"
		        + "   group by a.patient_id\n"
		        + ") n on h.patient_id = n.patient_id\n"
		        + "left outer join (\n"
		        + "  select f.patient_id,mid(max(concat(f.visit_date, f.has_adverse_event )), 11) as has_adverse_event_followup\n"
		        + "  from kenyaemr_etl.etl_vmmc_client_followup f\n" + "  group by f.patient_id\n"
		        + ") cf on h.patient_id = cf.patient_id\n" + "left outer join (\n"
		        + " select t.patient_id,t.visit_date,mid(max(concat(t.visit_date, t.weight )), 11) as clientWeight\n"
		        + " from kenyaemr_etl.etl_patient_triage t\n" + " group by t.patient_id\n"
		        + ") tr on h.patient_id = tr.patient_id and h.visit_date = tr. visit_date\n" + ") y\n";
		
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
