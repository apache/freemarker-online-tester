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
package com.kenshoo.freemarker.model;

import java.util.List;

/**
 * Created by Pmuruge on 8/29/2015.
 */
public class ExecuteResponse {
    private String result;
    private List<ExecuteResourceProblem> problems;
    private boolean truncatedResult;

    public ExecuteResponse(String result, List<ExecuteResourceProblem> problems, boolean truncatedResult) {
        this.result = result;
        this.problems = problems;
        this.truncatedResult = truncatedResult;
    }

    public ExecuteResponse() {

    }

    public List<ExecuteResourceProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<ExecuteResourceProblem> problems) {
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
