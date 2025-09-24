package de.codebarista.shopware.appbackend.sdk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Resource> handleError(HttpServletRequest request, HttpServletResponse response) {
        String requestId = response.getHeader("X-REQUEST-ID");
        int httpStatus = response.getStatus();
        LOGGER.error("Request {} invoked error handler. Status: {}", requestId, httpStatus);

        // TODO: Check request accepted-content type and respond with JSON to requests expecting JSON
        Resource resource = new ClassPathResource("error.html");
        return ResponseEntity.status(httpStatus).body(resource);
    }
}
