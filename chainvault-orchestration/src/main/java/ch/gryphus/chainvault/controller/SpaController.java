/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/migration/**", "/dashboard", "/overview"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
