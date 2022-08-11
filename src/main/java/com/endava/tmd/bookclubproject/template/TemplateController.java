package com.endava.tmd.bookclubproject.template;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class TemplateController {

    @RequestMapping(method = RequestMethod.GET, value = "login")
    public String getLogin() {
        return "login";
    }

    @RequestMapping(method = RequestMethod.GET, value = "mainPage")
    public String getMainPage(){
        return "mainPage";
    }

}
