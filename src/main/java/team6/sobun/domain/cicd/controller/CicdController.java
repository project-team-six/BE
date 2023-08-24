package team6.sobun.domain.cicd.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CicdController {

    @GetMapping("/cicd")
    public String helloWorld(){
        return "CICD test";
    }
}