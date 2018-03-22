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

public class ExecuteResponseProblem {
    
    private ExecuteRequest.Field field;
    private String message;
    
    // Needed for JSON unmarshalling
    public ExecuteResponseProblem() {
        //
    }
    
    public ExecuteResponseProblem(ExecuteRequest.Field field, String message) {
        this.field = field;
        this.message = message;
    }

    public ExecuteRequest.Field getField() {
        return field;
    }

    public void setField(ExecuteRequest.Field field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
