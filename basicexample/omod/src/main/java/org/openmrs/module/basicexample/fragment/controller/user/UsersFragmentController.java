package org.openmrs.module.basicexample.fragment.controller.user;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Date;
import java.util.List;

/**
 * Created by patrick on 2/15/2017.
 */
public class UsersFragmentController {
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService userService) {
		model.addAttribute("today", new Date());
		List<User> users = userService.getAllUsers();
		model.addAttribute("user", users);
		System.out.println("Controller method called");
		
	}
	
}
