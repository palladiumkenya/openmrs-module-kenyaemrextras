/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.calculation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculation for Clients With Recency ID report 1. Alive 2. With Recency ID
 */
public class ClientsWithRecencyIDCalculation extends AbstractPatientCalculation {
	
	protected static final Log log = LogFactory.getLog(ClientsWithRecencyIDCalculation.class);
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params,
	        PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		
		Set<Integer> alive = Filters.alive(cohort, context);
		for (int ptId : cohort) {
			boolean hasRecencyIdentifier = false;
			PatientService patientService = Context.getPatientService();
			PatientIdentifierType recencyIDPatientIdentifier = MetadataUtils.existing(PatientIdentifierType.class,
			    CommonMetadata._PatientIdentifierType.RECENCY_TESTING_ID);
			
			List<PatientIdentifier> clientRecencyIdentifier = patientService.getPatientIdentifiers(null,
			    Arrays.asList(recencyIDPatientIdentifier), null, Arrays.asList(patientService.getPatient(ptId)), false);
			
			// Is patient alive and has RecencyID
			if (alive.contains(ptId) && clientRecencyIdentifier.size() > 0) {
				hasRecencyIdentifier = true;
			}
			ret.put(ptId, new BooleanResult(hasRecencyIdentifier, this, context));
			
		}
		return ret;
	}
}
