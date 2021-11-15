/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.DQAActiveCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Evaluator for active patients eligible for DQA
 */
@Handler(supports = { DQAActiveCohortDefinition.class })
public class DQAActiveCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		DQAActiveCohortDefinition definition = (DQAActiveCohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		//String qry = "select distinct FLOOR(1 + (RAND() * 999999)) as index_no, active.patient_id\n" +
		String below15qry = "select distinct FLOOR(1 + (RAND() * 999999)) as index_no, t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "           p.dob as dob,\n"
		        + "           d.patient_id as disc_patient,\n"
		        + "           d.effective_disc_date as effective_disc_date,\n"
		        + "           max(d.visit_date) as date_discontinued,\n"
		        + "           de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "           left outer JOIN\n"
		        + "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "              group by patient_id\n"
		        + "             ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having timestampdiff(YEAR ,dob,date(:endDate)) <= 14 and (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "            )\n" + "        )\n" + "    ) t order by index_no limit 10;";
		
		String generalPopQry = "select distinct FLOOR(1 + (RAND() * 999999)) as index_no, t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "           p.dob as dob,\n"
		        + "           d.patient_id as disc_patient,\n"
		        + "           d.effective_disc_date as effective_disc_date,\n"
		        + "           max(d.visit_date) as date_discontinued,\n"
		        + "           de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "           left outer JOIN\n"
		        + "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "              group by patient_id\n"
		        + "             ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having timestampdiff(YEAR ,dob,date(:endDate)) >= 15 and (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "            )\n" + "        )\n" + "    ) t order by index_no limit 10;";
		
		String pmtctQry = "select distinct FLOOR(1 + (RAND() * 999999)) as index_no, t.patient_id\n"
		        + "from(\n"
		        + "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n"
		        + "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n"
		        + "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n"
		        + "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n"
		        + "           d.patient_id as disc_patient,\n"
		        + "           p.dob as dob,\n"
		        + "           d.effective_disc_date as effective_disc_date,\n"
		        + "           max(d.visit_date) as date_discontinued,\n"
		        + "           de.patient_id as started_on_drugs\n"
		        + "    from kenyaemr_etl.etl_patient_hiv_followup fup\n"
		        + "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n"
		        + "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n"
		        + "           inner join kenyaemr_etl.etl_patient_program pp on pp.patient_id=fup.patient_id and program='MCH-Mother Services' and (\n"
		        + "    date(date_enrolled) between date(:startDate) and date(:endDate) or \n"
		        + "    date(date_completed) between date(:startDate) and date(:endDate) or\n"
		        + "    (date(date_enrolled) < date(:startDate) and (date_completed is null or date(date_completed) > date(:endDate))) )\n"
		        + "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n"
		        + "           left outer JOIN\n"
		        + "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n"
		        + "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n"
		        + "              group by patient_id\n"
		        + "             ) d on d.patient_id = fup.patient_id\n"
		        + "    where fup.visit_date <= date(:endDate)\n"
		        + "    group by patient_id\n"
		        + "    having timestampdiff(YEAR ,dob,date(:endDate)) >= 15 and (started_on_drugs is not null and started_on_drugs <> '') and (\n"
		        + "        (\n"
		        + "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or d.effective_disc_date is null))\n"
		        + "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n"
		        + "            )\n" + "        )\n" + "    ) t order by index_no limit 10;";
		
		Map<String, Object> m = new HashMap<String, Object>();
		
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		
		m.put("endDate", endDate);
		TreeMap<Double, Integer> pedsMap = (TreeMap<Double, Integer>) makePatientDataMapFromSQL(below15qry, m);
		TreeMap<Double, Integer> generalPopMap = (TreeMap<Double, Integer>) makePatientDataMapFromSQL(generalPopQry, m);
		
		m.put("startDate", startDate);
		TreeMap<Double, Integer> pmtctMap = (TreeMap<Double, Integer>) makePatientDataMapFromSQL(pmtctQry, m);
		
		if (pedsMap != null) {
			for (Double rand : pedsMap.keySet()) {
				newCohort.addMember(pedsMap.get(rand));
			}
		}
		
		if (generalPopMap != null) {
			for (Double rand : generalPopMap.keySet()) {
				newCohort.addMember(generalPopMap.get(rand));
			}
		}
		
		if (pmtctMap != null) {
			for (Double rand : pmtctMap.keySet()) {
				newCohort.addMember(pmtctMap.get(rand));
			}
		}
		
		return new EvaluatedCohort(newCohort, definition, context);
	}
	
	//======================================= data extraction methods =============================================
	
	protected Map<Double, Integer> makePatientDataMapFromSQL(String sql, Map<String, Object> substitutions) {
		List<Object> data = Context.getService(KenyaEmrService.class).executeSqlQuery(sql, substitutions);
		
		return makePatientDataMap(data);
	}
	
	protected Map<Double, Integer> makePatientDataMap(List<Object> data) {
		Map<Double, Integer> dataTreeMap = new TreeMap<Double, Integer>();
		for (Object o : data) {
			Object[] parts = (Object[]) o;
			if (parts.length == 2) {
				Double rand = (Double) parts[0];
				Integer pid = (Integer) parts[1];
				dataTreeMap.put(rand, pid);
			}
		}
		
		return dataTreeMap;
	}
}
