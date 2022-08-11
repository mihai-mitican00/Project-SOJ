package com.endava.tmd.bookclubproject.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "register")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @RequestMapping(method = RequestMethod.POST)
    public String register(@RequestBody RegistrationRequest request){
        return registrationService.register(request);
    }

    @RequestMapping(method = RequestMethod.GET, path = "confirm")
    public String confirmToken(@RequestParam("token") final String token){
       return registrationService.confirmToken(token);
    }
}
