/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.onlinetester.services;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.freemarker.onlinetester.services.FreeMarkerService.ExecuteTemplateArgs;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.Environment;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class FreeMarkerServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(FreeMarkerServiceTest.class); 
    
    private static final int MAX_THREADS = 3;
    private static final int MAX_QUEUE_LENGTH = 2;
    private static final int MAX_TEMPLATE_EXECUTION_TIME = 1500;

    private static final int BLOCKING_TEST_TIMEOUT = 5000;
    
    private static final String TRUNCATION_TEST_TEMPLATE = "12345";

    private FreeMarkerService.Builder serviceBuilder;

    @Before
    public void initializeService() {
        serviceBuilder = new FreeMarkerService.Builder();
        serviceBuilder.setMaxQueueLength(MAX_QUEUE_LENGTH);
        serviceBuilder.setMaxThreads(MAX_THREADS);
        serviceBuilder.setMaxTemplateExecutionTime(MAX_TEMPLATE_EXECUTION_TIME);
    }

    private FreeMarkerService getService() {
        return serviceBuilder.build();
    }

    @Test
    public void testCalculationOfATemplateWithNoDataModel() {
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode("test").dataModel(Collections.emptyMap()));
        assertThat(serviceResponse.isSuccesful(), is(true));
        assertThat(serviceResponse.getTemplateOutput(), is("test"));
    }

    @Test
    public void testSimpleTemplate() {
        HashMap<String, Object> dataModel = new HashMap<>();
        dataModel.put("var1", "val1");
        String templateSourceCode = "${var1}";
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode(templateSourceCode).dataModel(dataModel));
        assertThat(serviceResponse.getTemplateOutput(), equalTo("val1"));
    }

    @Test
    public void testTemplateWithFewArgsAndOperators() {
        HashMap<String, Object> dataModel = new HashMap<>();
        dataModel.put("var1", "val1");
        dataModel.put("var2", "val2");
        String template = "${var1?capitalize} ${var2?cap_first}";
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode(template).dataModel(dataModel));
        assertThat(serviceResponse.getTemplateOutput(), equalTo("Val1 Val2"));
    }

    @Test
    public void testOutputFormatParamterMatters() {
        String template = "${'&'}";
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("&"));
        }
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).outputFormat(HTMLOutputFormat.INSTANCE));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("&amp;"));
        }
    }

    @Test
    public void testLocaleParameterMatters() {
        String template = "${.locale}";
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).locale(Locale.US));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("en_US"));
        }
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).locale(new Locale("ru", "RU")));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("ru_RU"));
        }
    }

    @Test
    public void testTimeZoneParameterMatters() {
        String template = "${" + System.currentTimeMillis() + "?numberToDatetime}";
        
        String gmt1Result;
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).timeZone(TimeZone.getTimeZone("GMT+01")));
            gmt1Result = serviceResponse.getTemplateOutput();
        }
        
        String gmt2Result;
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).locale(new Locale("ru", "RU")));
            gmt2Result = serviceResponse.getTemplateOutput();
        }
        
        assertThat(gmt1Result, not(equalTo(gmt2Result)));
    }

    @Test
    public void testTagSyntaxParameterMatters() {
        String template = "[#if true]1[/#if]<#if true>2</#if>";
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).tagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("[#if true]1[/#if]2"));
        }
        for (int tagSyntax : new int[] { Configuration.SQUARE_BRACKET_TAG_SYNTAX, Configuration.AUTO_DETECT_TAG_SYNTAX }) {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).tagSyntax(tagSyntax));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("1<#if true>2</#if>"));
        }
    }

    @Test
    public void testInterpolationSyntaxParameterMatters() {
        String template = "${1} #{2} [=3]";
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).interpolationSyntax(Configuration.LEGACY_INTERPOLATION_SYNTAX));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("1 2 [=3]"));
        }
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).interpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("1 #{2} [=3]"));
        }
        {
            FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
            		.templateSourceCode(template).interpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX));
            assertThat(serviceResponse.getTemplateOutput(), equalTo("${1} #{2} 3"));
        }
    }
    
    @Test
    public void testTemplateWithSyntaxError() {
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode("test ${xx").dataModel(Collections.emptyMap()));
        assertThat(serviceResponse.isSuccesful(), is(false));
        assertThat(serviceResponse.getFailureReason(), instanceOf(ParseException.class));
    }

    @Test
    public void testTemplateWithEvaluationError() {
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode("test ${x}").dataModel(Collections.emptyMap()));
        assertThat(serviceResponse.isSuccesful(), is(false));
        assertThat(serviceResponse.getFailureReason(), instanceOf(TemplateException.class));
    }

    @Test
    public void testResultAlmostTruncation() {
        serviceBuilder.setMaxOutputLength(5);
        FreeMarkerServiceResponse serviceResponse = getService().executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode(TRUNCATION_TEST_TEMPLATE).dataModel(Collections.emptyMap()));
        assertThat(serviceResponse.isSuccesful(), is(true));
        assertThat(serviceResponse.isTemplateOutputTruncated(), is(false));
        assertThat(serviceResponse.getTemplateOutput(), equalTo(TRUNCATION_TEST_TEMPLATE));
    }

    @Test
    public void testResultTruncation() {
        serviceBuilder.setMaxOutputLength(4);
        FreeMarkerService service = getService();
        FreeMarkerServiceResponse serviceResponse = service.executeTemplate(new ExecuteTemplateArgs()
        		.templateSourceCode(TRUNCATION_TEST_TEMPLATE).dataModel(Collections.emptyMap()));
        assertThat(serviceResponse.isSuccesful(), is(true));
        assertThat(serviceResponse.isTemplateOutputTruncated(), is(true));
        assertThat(serviceResponse.getTemplateOutput(),
                startsWith(TRUNCATION_TEST_TEMPLATE.substring(0, service.getMaxOutputLength())));
        assertThat(serviceResponse.getTemplateOutput().charAt(service.getMaxOutputLength()),
                not(equalTo(TRUNCATION_TEST_TEMPLATE.charAt(service.getMaxOutputLength()))));
    }
    
    @Test
    public void testTemplateExecutionTimeout() throws InterruptedException, ExecutionException {
        serviceBuilder.setMaxTemplateExecutionTime(200);
        
        // To avoid blocking the CI server forever without giving error:
        Future<FreeMarkerServiceResponse> future = Executors.newSingleThreadExecutor().submit(
                new Callable<FreeMarkerServiceResponse>() {
        
                    @Override
                    public FreeMarkerServiceResponse call() throws Exception {
                        return getService().executeTemplate(new ExecuteTemplateArgs()
                        		.templateSourceCode("<#list 1.. as _></#list>")
                        		.dataModel(Collections.emptyMap()));
                    }
                    
                });
        FreeMarkerServiceResponse serviceResponse;
        try {
            serviceResponse = future.get(BLOCKING_TEST_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new AssertionError("The template execution wasn't aborted (within the timeout).");
        }
        assertThat(serviceResponse.isSuccesful(), is(false));
        assertThat(serviceResponse.getFailureReason(), instanceOf(TimeoutException.class));
    }
    
    @Test
    public void testServiceOverburden() throws InterruptedException {
        final BlockerDirective blocker = new BlockerDirective();
        final Map<String, BlockerDirective> blockerDataModel = Collections.singletonMap("blocker", blocker);
        try {
            // Fill all available task "slots":
            FreeMarkerService service = getService();
            for (int i = 0; i < MAX_THREADS + MAX_QUEUE_LENGTH; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        service.executeTemplate(new ExecuteTemplateArgs()
                        		.templateSourceCode("<@blocker/>").dataModel(blockerDataModel));
                    }
                }).start();
            }
            
            // Wait until all template executions has started:
            synchronized (blocker) {
                final long startTime = System.currentTimeMillis();
                while (blocker.getEntered() < MAX_THREADS) {
                    // To avoid blocking the CI server forever is something goes wrong:
                    if (System.currentTimeMillis() - startTime > BLOCKING_TEST_TIMEOUT) {
                        fail("JUnit test timed out");
                    }
                    blocker.wait(1000);
                }
            }
            Thread.sleep(200);
            // Because the others are waiting in the queue, and weren't started:
            assertThat(blocker.getEntered(), not(greaterThan(MAX_THREADS)));
            
            // Shouldn't accept on more tasks:
            try {
                service.executeTemplate(new ExecuteTemplateArgs()
                		.templateSourceCode("<@blocker/>").dataModel(blockerDataModel));
                fail("Expected RejectedExecutionException, but nothing was thrown.");
            } catch (RejectedExecutionException e) {
                // Expected
            }
        } finally {
            // Ensure that the started threads will end:
            blocker.release();
        }
    }
    
    private static final class BlockerDirective implements TemplateDirectiveModel {
        
        private int entered;
        private boolean released;

        public synchronized void release() {
            released = true;
            notifyAll();
        }
        
        @Override
        public synchronized void execute(Environment env, @SuppressWarnings("rawtypes") Map params,
                TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
            entered++;
            notifyAll();
            final long startTime = System.currentTimeMillis();
            while (!released) {
                // To avoid blocking the CI server forever is something goes wrong:
                if (System.currentTimeMillis() - startTime > BLOCKING_TEST_TIMEOUT) {
                    LOG.error("JUnit test timed out");
                }
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    LOG.error("JUnit test was interrupted");
                }
            }
            LOG.debug("Blocker released");
        }

        public synchronized int getEntered() {
            return entered;
        }
        
    }
    
}
