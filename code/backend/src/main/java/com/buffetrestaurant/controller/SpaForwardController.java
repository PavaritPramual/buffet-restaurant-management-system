package com.buffetrestaurant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/admin", "/admin/**",
            "/staff/**",
            "/kitchen",
            "/billing/preview",
            "/customer/qr"
    })
    public String forwardFrontendRoutes() {
        return "forward:/index.html";
    }
}
