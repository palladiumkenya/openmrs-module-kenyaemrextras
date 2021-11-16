/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrextras.reporting.data.definition.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.List;

/**
 * Created by codehub on 09/03/15.
 */
public class DQAIdentifierCompletenessDataConverter implements DataConverter {
	
	@Override
	public Object convert(Object obj) {
		
		if (obj == null) {
			return "No";
		}
		List<Object> idList = (List<Object>) obj;
		System.out.print("List 1: " + idList.get(0));
		
		String val = String.valueOf(obj);
		System.out.print("Idnentifier: " + val);
		System.out.println(", Idnentifier: " + val.length());
		if (StringUtils.isBlank(val)) {
			return "No";
		}
		
		if (val.length() < 10 || val.length() > 10) {
			return "No";
		}
		return "Yes";
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Object.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
}
