package org.openmrs.module.basicexample.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.basicexample.api.SurgeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by patrick on 2/16/2017.
 */
@Service
public class SurgeryServiceImpl implements SurgeryService {
	
	@Autowired
	PatientService patientService;
	
	public List<Patient> getSurgeryPatients() {
		
		return patientService.getAllPatients();
		
	}
}
