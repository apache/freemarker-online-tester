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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kenshoo.freemarker.services.FreeMarkerService;
import com.kenshoo.freemarker.view.FreeMarkerOnlineView;

import freemarker.core.OutputFormat;

/**
 * Created with IntelliJ IDEA.
 * User: nir
 * Date: 4/12/14
 * Time: 11:23 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class FreeMarkerOnlineResourceTest {

    @InjectMocks
    FreeMarkerOnlineResource freeMarkerOnlineResultResource;

    @Mock
    FreeMarkerService freeMarkerService;

    @Test
    public void testInitialForm() {
        when(freeMarkerService.calculateTemplateOutput(
                anyString(), anyMap(), any(OutputFormat.class), any(Locale.class), any(TimeZone.class)))
                .thenThrow(new AssertionError());
        FreeMarkerOnlineView view = freeMarkerOnlineResultResource.blankForm();
        assertEquals(view.getTemplate(), "");
        assertEquals(view.getDataModel(), "");
    }
    
    @Test
    public void testPostedBlankForm() {
        when(freeMarkerService.calculateTemplateOutput(
                anyString(), anyMap(), any(OutputFormat.class), any(Locale.class), any(TimeZone.class)))
                .thenThrow(new AssertionError());
        FreeMarkerOnlineView view = freeMarkerOnlineResultResource.formResult(null, null, null, null, null);
        assertEquals(view.getTemplate(), "");
        assertEquals(view.getDataModel(), "");
    }
}
