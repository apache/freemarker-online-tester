/*
 * Copyright 2014 Kenshoo.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenshoo.freemarker.services;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kenshoo.freemarker.util.LengthLimitExceededException;
import com.kenshoo.freemarker.util.LengthLimitedWriter;

import freemarker.core.FreeMarkerInternalsAccessor;
import freemarker.core.OutputFormat;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.core.TemplateConfiguration;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Created with IntelliJ IDEA.
 * User: nir
 * Date: 4/12/14
 * Time: 10:15 AM
 */
@Service
public class FreeMarkerService {

    private static final int DEFAULT_MAX_OUTPUT_LENGTH = 100000;
    private static final int DEFAULT_MAX_THREADS = Math.max(2,
            (int) Math.round(Runtime.getRuntime().availableProcessors() * 3.0 / 4));
    /** Not implemented yet, will need 2.3.22, even then a _CoreAPI call. */
    private static final long DEFAULT_MAX_TEMPLATE_EXECUTION_TIME = 2000;
    private static final int MIN_DEFAULT_MAX_QUEUE_LENGTH = 2;
    private static final int MAX_DEFAULT_MAX_QUEUE_LENGTH_MILLISECONDS = 30000;
    private static final long THREAD_KEEP_ALIVE_TIME = 4 * 1000;
    private static final long ABORTION_LOOP_TIME_LIMIT = 5000;
    private static final long ABORTION_LOOP_INTERRUPTION_DISTANCE = 50;
    
    private static final String MAX_OUTPUT_LENGTH_EXCEEDED_TERMINATION = "\n----------\n"
            + "Aborted template processing, as the output length has exceeded the {0} character limit set for "
            + "this service.";
    
    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerService.class);

    private final Configuration freeMarkerConfig;
    
    private ExecutorService templateExecutor;
    
    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    
    private int maxThreads = DEFAULT_MAX_THREADS;
    private Integer maxQueueLength;
    private long maxTemplateExecutionTime = DEFAULT_MAX_TEMPLATE_EXECUTION_TIME;

    public FreeMarkerService() {
        freeMarkerConfig = new Configuration(Configuration.getVersion());
        freeMarkerConfig.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freeMarkerConfig.setLogTemplateExceptions(false);
        freeMarkerConfig.setLocale(AllowedSettingValuesMaps.DEFAULT_LOCALE);
        freeMarkerConfig.setTimeZone(AllowedSettingValuesMaps.DEFAULT_TIME_ZONE);
        freeMarkerConfig.setOutputFormat(AllowedSettingValuesMaps.DEFAULT_OUTPUT_FORMAT);
        freeMarkerConfig.setOutputEncoding("UTF-8");
    }
    
    /**
     * @param templateSourceCode
     *            The FTL to execute; not {@code null}.
     * @param dataModel
     *            The FreeMarker data-model to execute the template with; maybe {@code null}.
     * @param outputFormat
     *            The output format to execute the template with; maybe {@code null}.
     * @param locale
     *            The locale to execute the template with; maybe {@code null}.
     * @param timeZone
     *            The time zone to execute the template with; maybe {@code null}.
     * 
     * @return The result of the template parsing and evaluation. The method won't throw exception if that fails due to
     *         errors in the template provided, instead it indicates this fact in the response object. That's because
     *         this is a service for trying out the template language, so such errors are part of the normal operation.
     * 
     * @throws RejectedExecutionException
     *             If the service is overburden and thus doing the calculation was rejected.
     * @throws FreeMarkerServiceException
     *             If the calculation fails from a reason that's not a mistake in the template and doesn't fit the
     *             meaning of {@link RejectedExecutionException} either.
     */
    public FreeMarkerServiceResponse calculateTemplateOutput(
            String templateSourceCode, Object dataModel, OutputFormat outputFormat, Locale locale, TimeZone timeZone)
            throws RejectedExecutionException {
        Objects.requireNonNull(templateExecutor, "templateExecutor was null - was postConstruct ever called?");
        
        final CalculateTemplateOutput task = new CalculateTemplateOutput(
                templateSourceCode, dataModel, outputFormat, locale, timeZone);
        Future<FreeMarkerServiceResponse> future = templateExecutor.submit(task);
        
        synchronized (task) {
            while (!task.isTemplateExecutionStarted() && !task.isTaskEnded() && !future.isDone()) {
                try {
                    task.wait(50); // Timeout is needed to periodically check future.isDone()
                } catch (InterruptedException e) {
                    throw new FreeMarkerServiceException("Template execution task was interrupted.", e);
                }
            }
        }
        
        try {
            return future.get(maxTemplateExecutionTime, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new FreeMarkerServiceException("Template execution task unexpectedly failed", e.getCause());
        } catch (InterruptedException e) {
            throw new FreeMarkerServiceException("Template execution task was interrupted.", e);
        } catch (TimeoutException e) {
            // Exactly one interruption should be enough, and it should abort template processing pretty much
            // immediately. But to be on the safe side we will interrupt in a loop, with a timeout.
            final long abortionLoopStartTime = System.currentTimeMillis();
            long timeLeft = ABORTION_LOOP_TIME_LIMIT;
            boolean templateExecutionEnded = false;
            do {
                synchronized (task) {
                    Thread templateExecutorThread = task.getTemplateExecutorThread();
                    if (templateExecutorThread == null) {
                        templateExecutionEnded = true;
                    } else {
                        FreeMarkerInternalsAccessor.interruptTemplateProcessing(templateExecutorThread);
                        logger.debug("Trying to interrupt overly long template processing (" + timeLeft + " ms left).");
                    }
                }
                if (!templateExecutionEnded) {
                    try {
                        timeLeft = ABORTION_LOOP_TIME_LIMIT - (System.currentTimeMillis() - abortionLoopStartTime);
                        if (timeLeft > 0) {
                            Thread.sleep(ABORTION_LOOP_INTERRUPTION_DISTANCE);
                        }
                    } catch (InterruptedException eInt) {
                        logger.error("Template execution abortion loop was interrupted", eInt);
                        timeLeft = 0;
                    }
                }
            } while (!templateExecutionEnded && timeLeft > 0);
            
            if (templateExecutionEnded) {
                logger.debug("Long template processing has ended.");
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e1) {
                    throw new FreeMarkerServiceException("Failed to get result from template executor task", e);
                }
            } else {
                throw new FreeMarkerServiceException(
                        "Couldn't stop long running template processing within " + ABORTION_LOOP_TIME_LIMIT
                        + " ms. It's possibly stuck forever. Such problems can exhaust the executor pool. "
                        + "Template (quoted): " + StringEscapeUtils.escapeJava(templateSourceCode));
            }
        }
    }
    
    public int getMaxOutputLength() {
        return maxOutputLength;
    }

    public void setMaxOutputLength(int maxOutputLength) {
        this.maxOutputLength = maxOutputLength;
    }

    public int getMaxThreads() {
        return maxThreads;
    }
    
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    
    public void setMaxQueueLength(int maxQueueLength) {
        this.maxQueueLength = maxQueueLength;
    }

    public long getMaxTemplateExecutionTime() {
        return maxTemplateExecutionTime;
    }
    
    public void setMaxTemplateExecutionTime(long maxTemplateExecutionTime) {
        this.maxTemplateExecutionTime = maxTemplateExecutionTime;
    }

    /**
     * Returns the time zone used by the FreeMarker templates.
     */
    public TimeZone getFreeMarkerTimeZone() {
        return freeMarkerConfig.getTimeZone();
    }
    
    private FreeMarkerServiceResponse createFailureResponse(Throwable e) {
        logger.debug("The template had error(s)", e);
        return new FreeMarkerServiceResponse.Builder().buildForFailure(e);
    }

    @PostConstruct
    public void postConstruct() {
        int actualMaxQueueLength = maxQueueLength != null
                ? maxQueueLength
                : Math.max(
                        MIN_DEFAULT_MAX_QUEUE_LENGTH,
                        (int) (MAX_DEFAULT_MAX_QUEUE_LENGTH_MILLISECONDS / maxTemplateExecutionTime));
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                maxThreads, maxThreads,
                THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                new BlockingArrayQueue<Runnable>(actualMaxQueueLength));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        templateExecutor = threadPoolExecutor;
    }
    
    private class CalculateTemplateOutput implements Callable<FreeMarkerServiceResponse> {
        
        private boolean templateExecutionStarted;
        private Thread templateExecutorThread;
        private final String templateSourceCode;
        private final Object dataModel;
        private final OutputFormat outputFormat;
        private final Locale locale;
        private final TimeZone timeZone;
        private boolean taskEnded;

        private CalculateTemplateOutput(String templateSourceCode, Object dataModel,
                OutputFormat outputFormat, Locale locale, TimeZone timeZone) {
            this.templateSourceCode = templateSourceCode;
            this.dataModel = dataModel;
            this.outputFormat = outputFormat;
            this.locale = locale;
            this.timeZone = timeZone;
        }
        
        @Override
        public FreeMarkerServiceResponse call() throws Exception {
            try {
                Template template;
                try {
                    TemplateConfiguration tCfg = new TemplateConfiguration();
                    tCfg.setParentConfiguration(freeMarkerConfig);
                    if (outputFormat != null) {
                        tCfg.setOutputFormat(outputFormat);
                    }
                    if (locale != null) {
                        tCfg.setLocale(locale);
                    }
                    if (timeZone != null) {
                        tCfg.setTimeZone(timeZone);
                    }
                    
                    template = new Template(null, null,
                            new StringReader(templateSourceCode), freeMarkerConfig, tCfg, null);
                    
                    tCfg.apply(template);
                } catch (ParseException e) {
                    // Expected (part of normal operation)
                    return createFailureResponse(e);
                } catch (Exception e) {
                    // Not expected
                    throw new FreeMarkerServiceException("Unexpected exception during template parsing", e);
                }
                
                FreeMarkerInternalsAccessor.makeTemplateInterruptable(template);
                
                boolean resultTruncated;
                StringWriter writer = new StringWriter();
                try {
                    synchronized (this) {
                        templateExecutorThread = Thread.currentThread(); 
                        templateExecutionStarted = true;
                        notifyAll();
                    }
                    try {
                        template.process(dataModel, new LengthLimitedWriter(writer, maxOutputLength));
                    } finally {
                        synchronized (this) {
                            templateExecutorThread = null;
                            FreeMarkerInternalsAccessor.clearAnyPendingTemplateProcessingInterruption();
                        }
                    }
                    resultTruncated = false;
                } catch (LengthLimitExceededException e) {
                    // Not really an error, we just cut the output here.
                    resultTruncated = true;
                    writer.write(new MessageFormat(MAX_OUTPUT_LENGTH_EXCEEDED_TERMINATION, AllowedSettingValuesMaps.DEFAULT_LOCALE)
                            .format(new Object[] { maxOutputLength }));
                    // Falls through
                } catch (TemplateException e) {
                    // Expected (part of normal operation)
                    return createFailureResponse(e);
                } catch (Exception e) {
                    if (FreeMarkerInternalsAccessor.isTemplateProcessingInterruptedException(e)) {
                        return new FreeMarkerServiceResponse.Builder().buildForFailure(new TimeoutException(
                                "Template processing was aborted for exceeding the " + getMaxTemplateExecutionTime()
                                + " ms time limit set for this online service. This is usually because you have "
                                + "a very long running #list (or other kind of loop) in your template.")); 
                    }
                    // Not expected
                    throw new FreeMarkerServiceException("Unexpected exception during template evaluation", e);
                }
                
                return new FreeMarkerServiceResponse.Builder().buildForSuccess(writer.toString(), resultTruncated);
            } finally {
                synchronized (this) {
                    taskEnded = true;
                    notifyAll();
                }
            }
        }
        
        private synchronized boolean isTemplateExecutionStarted() {
            return templateExecutionStarted;
        }

        private synchronized boolean isTaskEnded() {
            return taskEnded;
        }
        
        /**
         * @return non-{@code null} after the task execution has actually started, but before it has finished.
         */
        private synchronized Thread getTemplateExecutorThread() {
            return templateExecutorThread;
        }
        
    }

}
