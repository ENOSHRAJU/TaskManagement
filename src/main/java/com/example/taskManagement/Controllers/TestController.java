package com.example.taskManagement.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userEndpoint() {
        return "Hello user";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerEndpoint() {
        return "Hello manager";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Hello admin";
    }

    @GetMapping("/admin/manager")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminAndManager() {
        return "Hello admin and manager";
    }
}
