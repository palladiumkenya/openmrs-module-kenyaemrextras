package org.openmrs.module.basicexample.page.controller.patient;

import org.openmrs.Patient;
import java.util.List;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

/**
 * Created by patrick on 2/15/2017.
 */
public class PatientsPageController {
	
	public void get(PageModel model, @SpringBean("patientService") PatientService patientService) {
		
		model.addAttribute("myName", "Wangoo");
		
		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);
		System.out.println("GET has been  called");
	}
	
}
