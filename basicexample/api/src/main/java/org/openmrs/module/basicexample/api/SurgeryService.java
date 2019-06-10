package org.openmrs.module.basicexample.api;

import org.openmrs.Patient;

import java.util.List;

/**
 * Created by patrick on 2/16/2017.
 */
public interface SurgeryService {
	
	List<Patient> getSurgeryPatients();
}
