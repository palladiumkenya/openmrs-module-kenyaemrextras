/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.evaluator.sims;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrextras.metadata.ExtrasMetadata;
import org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims.S0311CohortDefinition;
import org.openmrs.module.kenyaemrextras.reporting.library.sims.SimsReportQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Evaluator for CohortDefinition: HIV-positive KPs patients : In KP program and on ART >=12 months
 */
@Handler(supports = { S0311CohortDefinition.class })
public class S0311CohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		S0311CohortDefinition definition = (S0311CohortDefinition) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = SimsReportQueries.txCurrKpMoreThan12MonthsOnArtWithVlQuery();
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("startDate", startDate);
		builder.addParameter("endDate", endDate);
		
		List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);
		newCohort.setMemberIds(new HashSet<Integer>(ptIds));
		return new EvaluatedCohort(newCohort, definition, context);
		
	}
	
	/**
	 * Gets sample size from the config string based on patients on treatment
	 * 
	 * @param totalPatients
	 * @param configuration
	 * @return
	 */
	private Integer getSampleSize(Integer totalPatients, DQASampleSizeConfiguration configuration) {
		
		Integer lowestBoundary = configuration.getFirst();
		Map<Integer, Integer> upperMostBoundary = configuration.getLast();
		Map<String, Integer> otherLevels = configuration.getMiddleLevels();
		Integer size = 0;
		
		Integer upperKey = new ArrayList<Integer>(upperMostBoundary.keySet()).get(0);
		Integer upperValue = upperMostBoundary.get(upperKey);
		
		if (totalPatients <= lowestBoundary) {
			size = totalPatients;
		} else if (totalPatients >= upperKey) {
			size = upperValue;
		} else {
			size = processMiddleLevels(totalPatients, otherLevels);
		}
		return size;
	}
	
	private Integer processMiddleLevels(Integer val, Map<String, Integer> otherLevels) {
		Integer size = 0;
		for (String key : otherLevels.keySet()) {
			String rawKey[] = key.split("-");
			Integer lower = Integer.valueOf(rawKey[0]);
			Integer upper = Integer.valueOf(rawKey[1]);
			Integer sampleSize = otherLevels.get(key);
			
			if (lower != null && upper != null) {
				if (val >= lower && val <= upper) {
					size = sampleSize;
					break;
				}
			}
		}
		return size;
	}
	
	/**
	 * Reads the dqa sample size config for TX CURR
	 * 
	 * @return
	 */
	private DQASampleSizeConfiguration getSampleConfiguration() {
		
		String sampleSizeConf = Context.getAdministrationService().getGlobalProperty(ExtrasMetadata.DQA_SAMPLE_SIZES);
		
		if (sampleSizeConf == null)
			return new DQASampleSizeConfiguration();
		
		String confArray[] = sampleSizeConf.split(",");
		
		DQASampleSizeConfiguration configuration = new DQASampleSizeConfiguration();
		Map<String, Integer> middleLevels = new HashMap<String, Integer>();
		
		for (int i = 0; i < confArray.length; i++) {
			int len = confArray.length - 1;
			if (i == 0) {
				configuration.setFirst(Integer.parseInt(confArray[0]));
			} else if (i == len) {
				String lastItemArr[] = confArray[len].split(":");
				Map<Integer, Integer> lastItem = new HashMap<Integer, Integer>();
				
				Integer key = Integer.valueOf(lastItemArr[0]);
				Integer value = Integer.valueOf(lastItemArr[1]);
				lastItem.put(key, value);
				configuration.setLast(lastItem);
			} else {
				
				String itemArr[] = confArray[i].split(":");
				String key = itemArr[0];
				Integer value = Integer.valueOf(itemArr[1]);
				
				middleLevels.put(key, value);
			}
		}
		configuration.setMiddleLevels(middleLevels);
		return configuration;
	}
	
	/**
	 * A private class to hold the sample config
	 */
	class DQASampleSizeConfiguration {
		
		private Integer first;
		
		private Map<Integer, Integer> last;
		
		private Map<String, Integer> middleLevels;
		
		public Integer getFirst() {
			return first;
		}
		
		public void setFirst(Integer first) {
			this.first = first;
		}
		
		public Map<Integer, Integer> getLast() {
			return last;
		}
		
		public void setLast(Map<Integer, Integer> last) {
			this.last = last;
		}
		
		public Map<String, Integer> getMiddleLevels() {
			return middleLevels;
		}
		
		public void setMiddleLevels(Map<String, Integer> middleLevels) {
			this.middleLevels = middleLevels;
		}
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
