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

package org.apache.freemarker.onlinetester.resources;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.freemarker.onlinetester.model.ExecuteRequest;
import org.apache.freemarker.onlinetester.model.ExecuteResponse;
import org.apache.freemarker.onlinetester.model.ExecuteResponseProblem;
import org.junit.Test;

public class ExecuteApiResourceTest extends ResourceTest {
    private static final String DATA_MODEL = "user=John";
    private static final String TEMPLATE_WITH_VARIABLE = "Welcome ${user}";
    private static final String TEMPLATE_PLAIN = "Welcome John";
    private static final String MALFORMED_DATA_MODEL = "userJohn";

    @Test
    public void testSuccessRequest() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_WITH_VARIABLE, DATA_MODEL);
        Response resp = postJSON(req);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.readEntity(ExecuteResponse.class);
        assertNull(response.getProblems());
    }

    @Test
    public void testMalformedDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_PLAIN, MALFORMED_DATA_MODEL);
        Response resp = postJSON(req);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.readEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteRequest.Field.DATA_MODEL));
    }

    @Test
    public void testLongDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_PLAIN, create30KString());
        Response resp = postJSON(req);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.readEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteRequest.Field.DATA_MODEL));
        String problemMessage = getProblemMessage(response, ExecuteRequest.Field.DATA_MODEL);
        assertThat(problemMessage, containsString("data model"));
        assertThat(problemMessage, containsString("limit"));
    }

    @Test
    public void testLongTemplate() throws Exception {
        ExecuteRequest req = new ExecuteRequest(create30KString(), DATA_MODEL);
        Response resp = postJSON(req);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.readEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteRequest.Field.TEMPLATE));
        String problemMessage = getProblemMessage(response, ExecuteRequest.Field.TEMPLATE);
        assertThat(problemMessage, containsString("template"));
        assertThat(problemMessage, containsString("limit"));
    }

    @Test
    public void testMultipleErrorsDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(create30KString(), create30KString());
        req.setOutputFormat("wrongOutputFormat");
        req.setLocale("wrongLocale");
        req.setTimeZone("wrongTimeZone");
        req.setTagSyntax("wrongTagSyntax");
        req.setInterpolationSyntax("wrongInterpolationSyntax");

        Response resp = postJSON(req);
        
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.readEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertThat(getProblemMessage(response, ExecuteRequest.Field.TEMPLATE), containsString("limit"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.DATA_MODEL), containsString("limit"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.OUTPUT_FORMAT), containsString("wrongOutputFormat"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.LOCALE), containsString("wrongLocale"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.TIME_ZONE), containsString("wrongTimeZone"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.TAG_SYNTAX), containsString("wrongTagSyntax"));
        assertThat(getProblemMessage(response, ExecuteRequest.Field.INTERPOLATION_SYNTAX), containsString(
        		"wrongInterpolationSyntax"));
    }
    
    private String create30KString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30000 / 10; i++) {
            sb.append("0123456789");
        }
        return sb.toString();
    }

    private boolean containsProblem(ExecuteResponse response, ExecuteRequest.Field field) {
        for (ExecuteResponseProblem problem : response.getProblems()) {
            if (problem.getField() == field) {
                return true;
            }
        }
        return false;
    }

    private String getProblemMessage(ExecuteResponse response, ExecuteRequest.Field field) {
        for (ExecuteResponseProblem problem : response.getProblems()) {
            if (problem.getField() == field) {
                return problem.getMessage();
            }
        }
        return null;
    }

    protected Response postJSON(ExecuteRequest req) {
        return RULE.target("/api/execute").request().post(Entity.json(req));
    }

}
