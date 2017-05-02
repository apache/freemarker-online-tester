package org.apache.freemarker.onlinetester.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.apache.freemarker.onlinetester"})
public class SpringConfiguration {}
