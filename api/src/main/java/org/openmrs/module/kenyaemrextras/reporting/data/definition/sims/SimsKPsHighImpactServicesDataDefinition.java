/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.sims;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * High impact services data definition for KPs
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
public class SimsKPsHighImpactServicesDataDefinition extends BaseDataDefinition implements PersonDataDefinition {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public SimsKPsHighImpactServicesDataDefinition() {
		super();
	}
	
	/**
	 * Constructor to populate name only
	 */
	public SimsKPsHighImpactServicesDataDefinition(String name) {
		super(name);
	}
	
	//***** INSTANCE METHODS *****
	
	/**
	 * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
	 */
	public Class<?> getDataType() {
		return String.class;
	}
}