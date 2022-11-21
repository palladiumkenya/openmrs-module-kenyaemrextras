/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.cohort.definition.sims;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Instructions: Pediatric ART patients: Review 10 records (e.g., charts, high viral load register,
 * EMR entries) of 5 pediatric (<10 years old) and 5 adolescent (10-19 years old) patients on ART
 * ≥12 months with virologic non-suppression.
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.S0223CohortDefinition")
public class S0223CohortDefinition extends BaseCohortDefinition {
	
}
