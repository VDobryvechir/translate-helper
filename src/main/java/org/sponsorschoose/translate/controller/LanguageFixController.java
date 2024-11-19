package org.sponsorschoose.translate.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.sponsorschoose.translate.service.*;

@RestController
@RequestMapping("/api/language")
public class LanguageFixController {

    @Autowired
    private LanguageFixService languageFixService;

    @GetMapping("/fix/{src}/{dst}/{kind}")
    @CrossOrigin
    public String count(@PathVariable("src") String src, @PathVariable("dst") String dst,
            @PathVariable("kind") String kind) {
        return languageFixService.fixCommonDictionary(src, dst, kind);
    }

}
