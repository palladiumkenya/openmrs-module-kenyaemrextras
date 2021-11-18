/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemrextras.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.globalProperty;

/**
 * Metadata constants
 */
@Component
public class ExtrasMetadata extends AbstractMetadataBundle {
	
	public static final String MODULE_ID = "kenyaemrextras";
	
	public static final String DQA_SAMPLE_SIZES = "kenyaemrextras.dqa.samplesizes";
	
	private String defaultConfig = "0,1-199:10,200-1000:30,1001:50";
	
	@Override
	public void install() throws Exception {
		
		install(globalProperty(DQA_SAMPLE_SIZES, "DQA sample size configuration ", defaultConfig));
	}
	
}
