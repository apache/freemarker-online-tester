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
package com.kenshoo.freemarker.view;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.kenshoo.freemarker.services.AllowedSettingValuesMaps;

import freemarker.core.HTMLOutputFormat;

/**
 * Created with IntelliJ IDEA.
 * User: nir
 * Date: 4/12/14
 * Time: 11:13 PM
 */
public class FreeMarkerOnlineViewTest {

    private static final String TEMPLATE = "Template";
    private static final String DATA_MODEL = "DataModel";

    @Test
    public void testVieEmptyConstrucotr() {
        FreeMarkerOnlineView view = new FreeMarkerOnlineView();
        assertEquals(view.getTemplate(), "");
        assertEquals(view.getDataModel(), "");
        assertEquals(view.getOutputFormat(), AllowedSettingValuesMaps.DEFAULT_OUTPUT_FORMAT_KEY);
        assertEquals(view.getLocale(), AllowedSettingValuesMaps.DEFAULT_LOCALE_KEY);
        assertEquals(view.getTimeZone(), AllowedSettingValuesMaps.DEFAULT_TIME_ZONE_KEY);
    }

    @Test
    public void testViewWhenAllOK() {
        FreeMarkerOnlineView view = new FreeMarkerOnlineView();
        
        view.setTemplate(TEMPLATE);
        view.setDataModel(DATA_MODEL);
        String outputFormatStr = HTMLOutputFormat.INSTANCE.getName();
        view.setOutputFormat(outputFormatStr);
        String localeStr = Locale.GERMAN.toString();
        view.setLocale(localeStr);
        String timeZoneStr = TimeZone.getTimeZone("GMT+01").getID();
        view.setTimeZone(timeZoneStr);
        
        assertEquals(view.getTemplate(), TEMPLATE);
        assertEquals(view.getDataModel(), DATA_MODEL);
        assertEquals(view.getOutputFormat(), outputFormatStr);
        assertEquals(view.getLocale(), localeStr);
        assertEquals(view.getTimeZone(), timeZoneStr);
    }
    
}
