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

import org.junit.Test;

public class FreeMarkerServiceResponseBuilderTest {

    private static final String RESULT = "Result";
    
    private final FreeMarkerServiceResponse.Builder builder = new FreeMarkerServiceResponse.Builder();

    @Test
    public void testSuccessResult() {
        FreeMarkerServiceResponse result = builder.buildForSuccess(RESULT, false);
        assertThat(result.getTemplateOutput(), equalTo(RESULT));
        assertThat(result.isTemplateOutputTruncated(), is(false));
        assertThat(result.isSuccesful(), is(true));
        assertThat(result.getFailureReason(), nullValue());
    }
        
    @Test
    public void testSuccessTruncatedResult() {
        FreeMarkerServiceResponse result = builder.buildForSuccess(RESULT, true);
        assertThat(result.getTemplateOutput(), equalTo(RESULT));
        assertThat(result.isTemplateOutputTruncated(), is(true));
        assertThat(result.isSuccesful(), is(true));
        assertThat(result.getFailureReason(), nullValue());
    }

    @Test
    public void testErrorResult() {
        Throwable failureReason = new Exception();
        FreeMarkerServiceResponse result = builder.buildForFailure(failureReason);
        assertThat(result.getTemplateOutput(), nullValue());
        assertThat(result.isTemplateOutputTruncated(), is(false));
        assertThat(result.isSuccesful(), is(false));
        assertThat(result.getFailureReason(), sameInstance(failureReason));
    }
    
}
