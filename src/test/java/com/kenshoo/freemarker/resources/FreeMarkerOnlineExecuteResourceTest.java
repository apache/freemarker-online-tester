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

package com.kenshoo.freemarker.resources;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import com.kenshoo.freemarker.model.ExecuteRequest;
import com.kenshoo.freemarker.model.ExecuteResourceField;
import com.kenshoo.freemarker.model.ExecuteResourceProblem;
import com.kenshoo.freemarker.model.ExecuteResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * Created by Pmuruge on 8/29/2015.
 */
public class FreeMarkerOnlineExecuteResourceTest extends JerseyTest {
    private static final String DATA_MODEL = "user=John";
    private static final String TEMPLATE_WITH_VARIABLE = "Welcome ${user}";
    private static final String TEMPLATE_PLAIN = "Welcome John";
    private static final String MALFORMED_DATA_MODEL = "userJohn";
    private static final String EXECUTE_API = "api/execute";
    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("com.kenshoo.freemarker.resources")
                        .contextPath("/")
                        .contextListenerClass(ContextLoaderListener.class)
                        .contextParam("contextConfigLocation", "classpath:spring/bootstrap-context.xml")
                        .servletClass(SpringServlet.class)
                        .requestListenerClass(RequestContextListener.class)
                        .build();
    }

    @Test
    public void testSuccessRequest() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_WITH_VARIABLE, DATA_MODEL);
        ClientResponse resp = client().resource(getBaseURI().toString() + EXECUTE_API)
                .header("Content-Type", "application/json").entity(req).post(ClientResponse.class);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.getEntity(ExecuteResponse.class);
        assertNull(response.getProblems());
    }

    @Test
    public void testMalformedDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_PLAIN, MALFORMED_DATA_MODEL);
        ClientResponse resp = client().resource(getBaseURI().toString() + EXECUTE_API)
                .header("Content-Type", "application/json").entity(req).post(ClientResponse.class);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.getEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteResourceField.DATA_MODEL));
    }

    @Test
    public void testLongDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(TEMPLATE_PLAIN, create30KString());
        ClientResponse resp = client().resource(getBaseURI().toString() + EXECUTE_API)
                .header("Content-Type", "application/json").entity(req).post(ClientResponse.class);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.getEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteResourceField.DATA_MODEL));
        String problemMessage = getProblemMessage(response, ExecuteResourceField.DATA_MODEL);
        assertThat(problemMessage, containsString("data model"));
        assertThat(problemMessage, containsString("limit"));
    }

    @Test
    public void testLongTemplate() throws Exception {
        ExecuteRequest req = new ExecuteRequest(create30KString(), DATA_MODEL);
        ClientResponse resp = client().resource(getBaseURI().toString() + EXECUTE_API)
                .header("Content-Type", "application/json").entity(req).post(ClientResponse.class);
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.getEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertTrue(containsProblem(response, ExecuteResourceField.TEMPLATE));
        String problemMessage = getProblemMessage(response, ExecuteResourceField.TEMPLATE);
        assertThat(problemMessage, containsString("template"));
        assertThat(problemMessage, containsString("limit"));
    }

    @Test
    public void testMultipleErrorsDataModel() throws Exception {
        ExecuteRequest req = new ExecuteRequest(create30KString(), create30KString());
        req.setOutputFormat("wrongOutputFormat");
        req.setLocale("wrongLocale");
        req.setTimeZone("wrongTimeZone");
        
        ClientResponse resp = client().resource(getBaseURI() + EXECUTE_API)
                .header("Content-Type", "application/json").entity(req).post(ClientResponse.class);
        
        assertEquals(200, resp.getStatus());
        ExecuteResponse response = resp.getEntity(ExecuteResponse.class);
        assertNotNull(response.getProblems());
        assertThat(getProblemMessage(response, ExecuteResourceField.TEMPLATE), containsString("limit"));
        assertThat(getProblemMessage(response, ExecuteResourceField.DATA_MODEL), containsString("limit"));
        assertThat(getProblemMessage(response, ExecuteResourceField.OUTPUT_FORMAT), containsString("wrongOutputFormat"));
        assertThat(getProblemMessage(response, ExecuteResourceField.LOCALE), containsString("wrongLocale"));
        assertThat(getProblemMessage(response, ExecuteResourceField.TIME_ZONE), containsString("wrongTimeZone"));
    }
    
    private String create30KString() {
        final String veryLongString;
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 30000 / 10; i++) {
                sb.append("0123456789");
            }
            veryLongString = sb.toString();
        }
        return veryLongString;
    }

    private boolean containsProblem(ExecuteResponse response, ExecuteResourceField field) {
        for (ExecuteResourceProblem problem : response.getProblems()) {
            if (problem.getField() == field) {
                return true;
            }
        }
        return false;
    }

    private String getProblemMessage(ExecuteResponse response, ExecuteResourceField field) {
        for (ExecuteResourceProblem problem : response.getProblems()) {
            if (problem.getField() == field) {
                return problem.getMessage();
            }
        }
        return null;
    }
    
}
