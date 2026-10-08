package com.buffetrestaurant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SpaForwardControllerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new SpaForwardController()).build();

    @Test
    void forwardsCanonicalAndReactRouterCaseInsensitiveDeepLinks() throws Exception {
        for (String path : new String[] {
                "/admin", "/admin/users", "/ADMIN/users",
                "/staff/tables", "/Staff/Sessions/12/Billing/",
                "/kitchen", "/kitchen/", "/KITCHEN",
                "/billing/preview", "/BILLING/PREVIEW/",
                "/customer/qr", "/customer/qr/", "/Customer/QR"
        }) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    void doesNotForwardUnknownApiOrStaticAssetPaths() throws Exception {
        for (String path : new String[] {"/api/v1/nonexistent", "/assets/missing.js"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }
}
