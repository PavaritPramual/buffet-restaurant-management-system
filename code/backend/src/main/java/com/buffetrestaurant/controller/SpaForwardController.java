package com.buffetrestaurant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/{section:(?i:admin|staff)}",
            "/{section:(?i:admin|staff)}/**",
            "/{section:(?i:kitchen)}", "/{section:(?i:kitchen)}/",
            "/{section:(?i:billing)}/{page:(?i:preview)}",
            "/{section:(?i:billing)}/{page:(?i:preview)}/",
            "/{section:(?i:customer)}/{page:(?i:qr)}",
            "/{section:(?i:customer)}/{page:(?i:qr)}/"
    })
    public String forwardFrontendRoutes() {
        return "forward:/index.html";
    }
}
