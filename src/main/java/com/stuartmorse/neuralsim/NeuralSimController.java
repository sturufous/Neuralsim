package com.stuartmorse.neuralsim;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/")
public class NeuralSimController {
	
	NeuralSimDefaults defaults;
	ServletContext context; 
	
	@Autowired
	NeuralSimController(NeuralSimDefaults neuralSimDefaults, ServletContext context) {
		this.defaults = neuralSimDefaults;
		this.context = context;	
	}
	
	@RequestMapping(value = "test", method = RequestMethod.GET)
	public ModelAndView showMessage(HttpServletRequest request) {

		System.out.println("Hello Stuart Morse!");
		ModelAndView response = new ModelAndView("jsp/firstone.jsp");
		return response;
	}
}