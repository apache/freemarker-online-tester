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

package org.apache.freemarker.onlinetester.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.freemarker.onlinetester.model.SelectionOption;
import org.apache.freemarker.onlinetester.services.AllowedSettingValues;

import freemarker.template.Configuration;
import io.dropwizard.views.View;

public class FreeMarkerOnlineView extends View {

	private static final List<SelectionOption> LOCALE_SELECTION_OPTIONS = toSelectionOptions(
			AllowedSettingValues.LOCALE_MAP,
			(k, v) -> truncate(v.getDisplayName(Locale.US), 18) + "; " + v.toString(),
			true);
	private static final List<SelectionOption> TIME_ZONE_SELECTION_OPTIONS = toSelectionOptions(
			AllowedSettingValues.TIME_ZONE_MAP,
			(k, v) -> truncate(k, 25),
			true);
	private static final List<SelectionOption> OUTPUT_FORMAT_SELECTION_OPTIONS = toSelectionOptions(
			AllowedSettingValues.OUTPUT_FORMAT_MAP,
			(k, v) -> k,
			true);
	private static final List<SelectionOption> TAG_SYNTAX_SELECTION_OPTIONS = toSelectionOptions(
			AllowedSettingValues.TAG_SYNTAX_MAP,
			(k, v) -> {
				String label = k;
				if (v == Configuration.ANGLE_BRACKET_TAG_SYNTAX) {
					label += ", like <#...>, <@...>";
				} else if (v == Configuration.SQUARE_BRACKET_TAG_SYNTAX) {
					label += ", like [#...], [@...]";
				}
				return label;
			},
			false);
	private static final List<SelectionOption> INTERPOLATION_SYNTAX_SELECTION_OPTIONS = toSelectionOptions(
			AllowedSettingValues.INTERPOLATION_SYNTAX_MAP, (k, v) -> {
				String label = k;
				if (v == Configuration.LEGACY_INTERPOLATION_SYNTAX) {
					label += ", like ${...}, #{...}";
				} else if (v == Configuration.DOLLAR_INTERPOLATION_SYNTAX) {
					label += ", like ${...}";
				} else if (v == Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX) {
					label += ", like [=...]";
				}
				return label;
			},
			false);
    
    private String template = "";
    private String dataModel = "";
    private String outputFormat = AllowedSettingValues.DEFAULT_OUTPUT_FORMAT_KEY;
    private String locale = AllowedSettingValues.DEFAULT_LOCALE_KEY;
    private String timeZone = AllowedSettingValues.DEFAULT_TIME_ZONE_KEY;
    private String tagSyntax = String.valueOf(AllowedSettingValues.DEFAULT_TAG_SYNTAX_KEY);
    private String interpolationSyntax = String.valueOf(AllowedSettingValues.DEFAULT_INTERPOLATION_SYNTAX_KEY);
    
    private boolean execute;

    private static <V> List<SelectionOption> toSelectionOptions(
    		Map<String, ? extends V> settingValueMap,
    		BiFunction<String, ? super V, String> kvpToLabel, boolean sortByLabel) {
        ArrayList<SelectionOption> selectionOptions = new ArrayList<SelectionOption>(settingValueMap.size());
        for (Map.Entry<String, ? extends V> ent : settingValueMap.entrySet()) {
        	String key = ent.getKey();
            selectionOptions.add(new SelectionOption(
            		key,
            		kvpToLabel.apply(key, ent.getValue())));
        }
        if (sortByLabel) {
        	Collections.sort(selectionOptions);
        }
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
        super("/view/main.ftlh");
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

    public List<SelectionOption> getTagSyntaxes() {
        return TAG_SYNTAX_SELECTION_OPTIONS;
    }

    public List<SelectionOption> getInterpolationSyntaxes() {
        return INTERPOLATION_SYNTAX_SELECTION_OPTIONS;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = withDefault(outputFormat, AllowedSettingValues.DEFAULT_OUTPUT_FORMAT_KEY);
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = withDefault(locale, AllowedSettingValues.DEFAULT_LOCALE_KEY);
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = withDefault(timeZone, AllowedSettingValues.DEFAULT_TIME_ZONE_KEY);
    }
    
    public String getTagSyntax() {
		return tagSyntax;
	}

	public void setTagSyntax(String tagSyntax) {
		this.tagSyntax = withDefault(tagSyntax, AllowedSettingValues.DEFAULT_TAG_SYNTAX_KEY);
	}

	public String getInterpolationSyntax() {
		return interpolationSyntax;
	}

	public void setInterpolationSyntax(String interpolationSyntax) {
		this.interpolationSyntax = withDefault(interpolationSyntax, AllowedSettingValues.DEFAULT_INTERPOLATION_SYNTAX_KEY);
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
