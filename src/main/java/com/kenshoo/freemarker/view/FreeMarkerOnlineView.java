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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.kenshoo.freemarker.model.SelectionOption;
import com.kenshoo.freemarker.services.AllowedSettingValuesMaps;
import com.yammer.dropwizard.views.View;

import freemarker.template.Configuration;

/**
 * Created with IntelliJ IDEA. User: nir Date: 4/11/14 Time: 12:23 PM
 */
public class FreeMarkerOnlineView extends View {

    private static final List<SelectionOption> LOCALE_SELECTION_OPTIONS = toLocaleSelectionOptions(AllowedSettingValuesMaps.LOCALE_MAP);
    private static final List<SelectionOption> TIME_ZONE_SELECTION_OPTIONS = toSelectionOptions(AllowedSettingValuesMaps.TIME_ZONE_MAP);
    private static final List<SelectionOption> OUTPUT_FORMAT_SELECTION_OPTIONS = toSelectionOptions(AllowedSettingValuesMaps.OUTPUT_FORMAT_MAP);
    
    private String template = "";
    private String dataModel = "";
    private String outputFormat = AllowedSettingValuesMaps.DEFAULT_OUTPUT_FORMAT_KEY;
    private String locale = AllowedSettingValuesMaps.DEFAULT_LOCALE_KEY;
    private String timeZone = AllowedSettingValuesMaps.DEFAULT_TIME_ZONE_KEY;
    
    private boolean execute;
    
    private static List<SelectionOption> toSelectionOptions(Map<String, ?> settingValueMap) {
        ArrayList<SelectionOption> selectionOptions = new ArrayList<SelectionOption>(settingValueMap.size());
        for (String key : settingValueMap.keySet()) {
            selectionOptions.add(new SelectionOption(key, truncate(key, 25)));
        }
        Collections.sort(selectionOptions);
        return selectionOptions;
    }
    
    private static List<SelectionOption> toLocaleSelectionOptions(Map<String, Locale> localeMap) {
        ArrayList<SelectionOption> selectionOptions = new ArrayList<SelectionOption>(localeMap.size());
        for (Map.Entry<String, Locale> ent : localeMap.entrySet()) {
            Locale locale = ent.getValue();
            selectionOptions.add(
                    new SelectionOption(ent.getKey(),
                    truncate(locale.getDisplayName(Locale.US), 18) + "; " + locale.toString()));
        }
        Collections.sort(selectionOptions);
        return selectionOptions;
    }
    
    private static String truncate(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        return s.length() <= maxLength ? s : s.substring(0, Math.max(maxLength - 3, 0)) + "[...]";
    }    

    /**
     *
     * @param template
     * @param dataModel
     * @param execute set to true if the execution should be triggered on page load.
     */
    public FreeMarkerOnlineView() {
        super("/view/freemarker-online.ftl", Charset.forName("utf-8"));
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = withDefault(template, "");
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = withDefault(dataModel, "");
    }

    public String getFreeMarkerVersion() {
        return Configuration.getVersion().toString();
    }
    
    public List<SelectionOption> getOutputFormats() {
        return OUTPUT_FORMAT_SELECTION_OPTIONS;
    }

    public List<SelectionOption> getLocales() {
        return LOCALE_SELECTION_OPTIONS;
    }

    public List<SelectionOption> getTimeZones() {
        return TIME_ZONE_SELECTION_OPTIONS;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = withDefault(outputFormat, AllowedSettingValuesMaps.DEFAULT_OUTPUT_FORMAT_KEY);
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = withDefault(locale, AllowedSettingValuesMaps.DEFAULT_LOCALE_KEY);
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = withDefault(timeZone, AllowedSettingValuesMaps.DEFAULT_TIME_ZONE_KEY);
    }
    
    public boolean isExecute() {
        return execute;
    }

    public void setExecute(boolean executeImmediately) {
        this.execute = executeImmediately;
    }

    private static String withDefault(String value, String defaultValue) {
        return !StringUtils.isBlank(value) ? value : defaultValue;
    }
    
}
