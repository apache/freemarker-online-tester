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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.freemarker.onlinetester.services.FreeMarkerService;
import org.apache.freemarker.onlinetester.services.FreeMarkerService.ExecuteTemplateArgs;
import org.apache.freemarker.onlinetester.view.FreeMarkerOnlineView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebPageResourceTest {

    @InjectMocks
    WebPageResource webPageResource;

    @Mock
    FreeMarkerService freeMarkerService;

    @Test
    public void testInitialForm() {
        FreeMarkerOnlineView view = webPageResource.blankForm();
        assertEquals(view.getTemplate(), "");
        assertEquals(view.getDataModel(), "");
        verify(freeMarkerService, never()).executeTemplate(any(ExecuteTemplateArgs.class));
    }
    
    @Test
    public void testPostedBlankForm() {
        FreeMarkerOnlineView view = webPageResource.formResult(null, null, null, null, null, null, null);
        assertEquals(view.getTemplate(), "");
        assertEquals(view.getDataModel(), "");
        verify(freeMarkerService, never()).executeTemplate(any(ExecuteTemplateArgs.class));
    }
    
}
