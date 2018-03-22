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

package org.apache.freemarker.onlinetester.model;

import java.util.List;

public class ExecuteResponse {
    private String result;
    private List<ExecuteResponseProblem> problems;
    private boolean truncatedResult;

    public ExecuteResponse(String result, List<ExecuteResponseProblem> problems, boolean truncatedResult) {
        this.result = result;
        this.problems = problems;
        this.truncatedResult = truncatedResult;
    }

    public ExecuteResponse() {

    }

    public List<ExecuteResponseProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<ExecuteResponseProblem> problems) {
        this.problems = problems;
    }

    public boolean isTruncatedResult() {
        return truncatedResult;
    }

    public void setTruncatedResult(boolean truncatedResult) {
        this.truncatedResult = truncatedResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
}
