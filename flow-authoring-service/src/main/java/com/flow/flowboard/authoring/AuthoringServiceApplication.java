/*
 ************************************************************************
 * flow CONFIDENTIAL ___________________
 * Copyright 2024 flow Systems Incorporated All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property of flow Systems
 * Incorporated and its suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary to flow Systems Incorporated and its suppliers and are protected by all
 * applicable intellectual property laws, including trade secret and copyright laws. Dissemination
 * of this information or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from flow Systems Incorporated.
 *************************************************************************
 */
package com.flow.flowboard.authoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Flow Authoring Service application.
 * Example: run the app and POST /api/v1/workflows
 */
@SpringBootApplication
public class AuthoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthoringServiceApplication.class, args);
    }
}
