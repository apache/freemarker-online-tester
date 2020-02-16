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

import org.apache.freemarker.onlinetester.util.LengthLimitExceededException;
import org.apache.freemarker.onlinetester.util.LengthLimitedWriter;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.Environment;
import freemarker.core.FreeMarkerInternalsAccessor;
import freemarker.core.OutputFormat;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.core.TemplateConfiguration;
import freemarker.template.AttemptExceptionReporter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import freemarker.template.utility.StringUtil;

public class FreeMarkerService {

    private static final int DEFAULT_MAX_OUTPUT_LENGTH = 100000;
    private static final int DEFAULT_MAX_THREADS = Math.max(2,
            (int) Math.round(Runtime.getRuntime().availableProcessors() * 3.0 / 4));
    private static final long DEFAULT_MAX_TEMPLATE_EXECUTION_TIME = 2000;
    private static final int MIN_DEFAULT_MAX_QUEUE_LENGTH = 2;
    private static final int MAX_DEFAULT_MAX_QUEUE_LENGTH_MILLISECONDS = 30000;
    private static final long THREAD_KEEP_ALIVE_TIME = 4 * 1000;
    private static final long ABORTION_LOOP_TIME_LIMIT = 5000;
    private static final long ABORTION_LOOP_INTERRUPTION_DISTANCE = 50;
    private static final long THREAD_STOP_EFFECT_WAIT_TIME = 500;
    
    private static final String MAX_OUTPUT_LENGTH_EXCEEDED_TERMINATION = "\n----------\n"
            + "Aborted template processing, as the output length has exceeded the {0} character limit set for "
            + "this service.";
    
    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerService.class);

    private final int maxOutputLength;
    private final int maxThreads;
    private final Integer maxQueueLength;
    private final long maxTemplateExecutionTime;

    private final Configuration freeMarkerConfig;
    private final ExecutorService templateExecutor;
    
    private FreeMarkerService(Builder bulder) {
        maxOutputLength = bulder.getMaxOutputLength();
        maxThreads = bulder.getMaxThreads();
        maxQueueLength = bulder.getMaxQueueLength();
        maxTemplateExecutionTime = bulder.getMaxTemplateExecutionTime();

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

        // Avoid ERROR log for using the actual current version. This application is special in that regard.
        Version latestVersion = new Version(Configuration.getVersion().toString());

        freeMarkerConfig = new Configuration(latestVersion);
        freeMarkerConfig.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        freeMarkerConfig.setObjectWrapper(new SimpleObjectWrapperWithXmlSupport(latestVersion));
        freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freeMarkerConfig.setLogTemplateExceptions(false);
        freeMarkerConfig.setAttemptExceptionReporter(new AttemptExceptionReporter() {
			@Override
			public void report(TemplateException te, Environment env) {
				// Suppress it
			}
        });
        freeMarkerConfig.setLocale(AllowedSettingValues.DEFAULT_LOCALE);
        freeMarkerConfig.setTimeZone(AllowedSettingValues.DEFAULT_TIME_ZONE);
        freeMarkerConfig.setOutputFormat(AllowedSettingValues.DEFAULT_OUTPUT_FORMAT);
        freeMarkerConfig.setOutputEncoding("UTF-8");
    }
    
    /**
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
    @SuppressWarnings("deprecation") // for Thread.stop()
	public FreeMarkerServiceResponse executeTemplate(ExecuteTemplateArgs args)
            throws RejectedExecutionException {
        Objects.requireNonNull(templateExecutor, "templateExecutor was null - was postConstruct ever called?");
        
        final ExecuteTempalteTask task = new ExecuteTempalteTask(args);
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
                        logger.debug("Trying to interrupt overly long template processing ({} ms left).", timeLeft);
                        FreeMarkerInternalsAccessor.interruptTemplateProcessing(templateExecutorThread);
                    }
                } // sync
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
            
    		// If a slow operation didn't react to Thread.interrupt, we better risk this than allow
    		// the depletion of the thread pool:
            if (!templateExecutionEnded) {
	            synchronized (task) {
	                Thread templateExecutorThread = task.getTemplateExecutorThread();
	                if (templateExecutorThread == null) {
	                    templateExecutionEnded = true;
	                } else {
	                    if (logger.isWarnEnabled()) {
	                    	logger.warn("Calling Thread.stop() on unresponsive long template processing, which didn't "
	                    			+ "respond to Template.interrupt() on time. Service state may will be inconsistent; "
	                    			+ "JVM restart recommended!\n"
	                    			+ "Template (quoted): " + StringUtil.jQuote(args.templateSourceCode));
	                    }
	                    templateExecutorThread.stop();
	                }
	            } // sync
                try {
                	// We should now receive a result from the task, so that we don't have to die with HTTP 500
					Thread.sleep(THREAD_STOP_EFFECT_WAIT_TIME);
		            synchronized (task) {
		                Thread templateExecutorThread = task.getTemplateExecutorThread();
		                if (templateExecutorThread == null) {
		                    templateExecutionEnded = true;
		                }
		            } // sync
				} catch (InterruptedException e2) {
					// Just continue...
				}
            }
            
            if (templateExecutionEnded) {
                logger.debug("Long template processing has ended.");
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e2) {
                    throw new FreeMarkerServiceException("Failed to get result from template executor task", e2);
                }
            } else {
                throw new FreeMarkerServiceException(
                        "Couldn't stop long running template processing within " + ABORTION_LOOP_TIME_LIMIT
                        + " ms. It's possibly stuck forever. Such problems can exhaust the executor pool. "
                        + "Template (quoted): " + StringUtil.jQuote(args.templateSourceCode));
            }
        }
    }
    
    public int getMaxOutputLength() {
        return maxOutputLength;
    }

    public int getMaxThreads() {
        return maxThreads;
    }
    
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    
    public long getMaxTemplateExecutionTime() {
        return maxTemplateExecutionTime;
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
    
    /**
     * Argument to {@link FreeMarkerService#executeTemplate(ExecuteTemplateArgs)}; fluent API to deal with many parameters,
     * most of which is optional. Only {@code templateSourceCode} must be set to non-{@code null}.
     */
	public static class ExecuteTemplateArgs {
		private String templateSourceCode;
		private Object dataModel;
		private OutputFormat outputFormat;
		private Locale locale;
		private TimeZone timeZone;
		private Integer tagSyntax;
		private Integer interpolationSyntax;

		public ExecuteTemplateArgs templateSourceCode(String templateSourceCode) {
			this.templateSourceCode = templateSourceCode;
			return this;
		}

		public ExecuteTemplateArgs dataModel(Object dataModel) {
			this.dataModel = dataModel;
			return this;
		}

		public ExecuteTemplateArgs outputFormat(OutputFormat outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

		public ExecuteTemplateArgs locale(Locale locale) {
			this.locale = locale;
			return this;
		}

		public ExecuteTemplateArgs timeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public ExecuteTemplateArgs tagSyntax(Integer tagSyntax) {
			this.tagSyntax = tagSyntax;
			return this;
		}

		public ExecuteTemplateArgs interpolationSyntax(Integer interpolationSyntax) {
			this.interpolationSyntax = interpolationSyntax;
			return this;
		}
	}

    private class ExecuteTempalteTask implements Callable<FreeMarkerServiceResponse> {
        
    	private final ExecuteTemplateArgs args;
        private boolean templateExecutionStarted;
        private Thread templateExecutorThread;
        private boolean taskEnded;

        private ExecuteTempalteTask(ExecuteTemplateArgs args) {
            this.args = args;
        }
        
        @Override
        public FreeMarkerServiceResponse call() throws Exception {
            try {
                Template template;
                try {
                    TemplateConfiguration tCfg = new TemplateConfiguration();
                    tCfg.setParentConfiguration(freeMarkerConfig);
                    
                    if (args.outputFormat != null) {
                        tCfg.setOutputFormat(args.outputFormat);
                    }
                    if (args.locale != null) {
                        tCfg.setLocale(args.locale);
                    }
                    if (args.timeZone != null) {
                        tCfg.setTimeZone(args.timeZone);
                    }
                    if (args.tagSyntax != null) {
                    	tCfg.setTagSyntax(args.tagSyntax);
                    }
                    if (args.interpolationSyntax != null) {
                    	tCfg.setInterpolationSyntax(args.interpolationSyntax);
                    }
                    
                    template = new Template(null, null,
                            new StringReader(args.templateSourceCode), freeMarkerConfig, tCfg, null);
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
                        template.process(args.dataModel, new LengthLimitedWriter(writer, maxOutputLength));
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
                    writer.write(new MessageFormat(MAX_OUTPUT_LENGTH_EXCEEDED_TERMINATION, AllowedSettingValues.DEFAULT_LOCALE)
                            .format(new Object[] { maxOutputLength }));
                    // Falls through
                } catch (TemplateException e) {
                    // Expected (part of normal operation)
                    return createFailureResponse(e);
                } catch (Throwable e) {
                    if (FreeMarkerInternalsAccessor.isTemplateProcessingInterruptedException(e)
                    		|| e instanceof ThreadDeath /* due to Thread.stop() */) {
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

    public static class Builder {
        private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
        private int maxThreads = DEFAULT_MAX_THREADS;
        private Integer maxQueueLength;
        private long maxTemplateExecutionTime = DEFAULT_MAX_TEMPLATE_EXECUTION_TIME;

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

        public Integer getMaxQueueLength() {
            return maxQueueLength;
        }

        public void setMaxQueueLength(Integer maxQueueLength) {
            this.maxQueueLength = maxQueueLength;
        }

        public long getMaxTemplateExecutionTime() {
            return maxTemplateExecutionTime;
        }

        public void setMaxTemplateExecutionTime(long maxTemplateExecutionTime) {
            this.maxTemplateExecutionTime = maxTemplateExecutionTime;
        }

        public FreeMarkerService build() {
            return new FreeMarkerService(this);
        }
    }

}
