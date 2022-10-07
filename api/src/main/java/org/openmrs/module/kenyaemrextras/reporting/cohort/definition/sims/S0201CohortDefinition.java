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
 * S0201 cohort definition - Adult ART patients: Review the last 10 register entries or charts
 * (whichever source has the most updated information) of adult and adolescent patients ≥15 years
 * old who newly initiated ART in the last 3 months .
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.S0201CohortDefinition")
public class S0201CohortDefinition extends BaseCohortDefinition {
	
}
