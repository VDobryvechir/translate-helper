package org.sponsorschoose.translate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	@GetMapping("/")
	public String index() {
		return "<a href='/api/translate/count/nb/en/s'>Count</a> <a href='/api/translate/next/nb/en/s'>Next</a>";
	}

}