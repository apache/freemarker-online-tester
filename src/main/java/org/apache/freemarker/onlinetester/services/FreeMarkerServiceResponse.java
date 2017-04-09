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

public class FreeMarkerServiceResponse {
    
    private final String templateOutput;
    private final boolean templateOutputTruncated;
    private final Throwable failureReason;

    FreeMarkerServiceResponse(String templateOutput, boolean templateOutputTruncated) {
        this.templateOutput = templateOutput;
        this.templateOutputTruncated = templateOutputTruncated;
        this.failureReason = null;
    }

    FreeMarkerServiceResponse(Throwable failureReason) {
        this.templateOutput = null;
        this.templateOutputTruncated = false;
        this.failureReason = failureReason;
    }
    
    public String getTemplateOutput() {
        return templateOutput;
    }

    public boolean isTemplateOutputTruncated() {
        return templateOutputTruncated;
    }

    public boolean isSuccesful() {
        return failureReason == null;
    }

    public Throwable getFailureReason() {
        return failureReason;
    }

    public static class Builder {
        
        public FreeMarkerServiceResponse buildForSuccess(String result, boolean resultTruncated){
            return new FreeMarkerServiceResponse(result, resultTruncated);
        }

        public FreeMarkerServiceResponse buildForFailure(Throwable failureReason){
            return new FreeMarkerServiceResponse(failureReason);
        }
        
    }
    
}
