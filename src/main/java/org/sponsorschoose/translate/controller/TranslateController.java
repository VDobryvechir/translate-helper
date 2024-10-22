package org.sponsorschoose.translate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.sponsorschoose.translate.service.*;
import org.sponsorschoose.translate.model.*;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

        @Autowired
        private TranslateService translateService;

	@GetMapping("/count/{src}/{dst}/{kind}")
	public String count(@PathVariable String src,@PathVariable String dst,@PathVariable String kind) {
		return translateService.makeWordStatistics(src,dst,kind);
	}

	@GetMapping("/next/{src}/{dst}/{kind}")
	public WordInfo next(@PathVariable String src,@PathVariable String dst,@PathVariable String kind) {
		return translateService.getNextPortion(src,dst,kind);
	}

	@PostMapping("/part/{src}/{dst}/{kind}")
	public WordInfo part(@PathVariable String src,@PathVariable String dst,@PathVariable String kind,@RequestBody String buf) {
		return translateService.saveNextPortion(src,dst,kind,buf);
	}

	@GetMapping("/clean")
	public String clean() {
                translateService.cleanAll();
		return "cleaned";
	}


}