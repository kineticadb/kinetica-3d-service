/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.gserv;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;

/**
 * Top level application object for Spring Boot.
 * @author Chad Juliano
 */
@SpringBootApplication
public class GservApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
            .sources(GservApplication.class)
            .listeners(new ApplicationPidFileWriter())
            .run(args);
    }
}
