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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

import freemarker.core.HTMLOutputFormat;
import freemarker.core.OutputFormat;
import freemarker.core.PlainTextOutputFormat;
import freemarker.core.RTFOutputFormat;
import freemarker.core.UndefinedOutputFormat;
import freemarker.core.XHTMLOutputFormat;
import freemarker.core.XMLOutputFormat;
import freemarker.template.Configuration;

/**
 * Maps of the FreeMarker configuration setting values the remote caller can
 * chose from (these are the value shown in a dropdown on the UI). This is
 * possibly more restricted than what FreeMarker supports, for security reasons.
 */
public class AllowedSettingValues {

    public static final OutputFormat DEFAULT_OUTPUT_FORMAT = UndefinedOutputFormat.INSTANCE;
    public static final String DEFAULT_OUTPUT_FORMAT_KEY = DEFAULT_OUTPUT_FORMAT.getName();
    public static final Map<String, OutputFormat> OUTPUT_FORMAT_MAP = ImmutableMap.<String, OutputFormat>builder()
    		.put(UndefinedOutputFormat.INSTANCE.getName(), UndefinedOutputFormat.INSTANCE)
    		.put(HTMLOutputFormat.INSTANCE.getName(), HTMLOutputFormat.INSTANCE)
    		.put(XMLOutputFormat.INSTANCE.getName(), XMLOutputFormat.INSTANCE)
    		.put(XHTMLOutputFormat.INSTANCE.getName(), XHTMLOutputFormat.INSTANCE)
    		.put(RTFOutputFormat.INSTANCE.getName(), RTFOutputFormat.INSTANCE)
    		.put(PlainTextOutputFormat.INSTANCE.getName(), PlainTextOutputFormat.INSTANCE)
    		.build();
    
    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final String DEFAULT_LOCALE_KEY = DEFAULT_LOCALE.toString();
    public static final Map<String, Locale> LOCALE_MAP;
    static {
        List<Locale> availableLocales = new ArrayList<Locale>(Arrays.asList(Locale.getAvailableLocales()));
        
        for (Iterator<Locale> iterator = availableLocales.iterator(); iterator.hasNext();) {
            Locale locale = iterator.next();
            // Don't bloat the list with "variants"
            if (!StringUtils.isBlank(locale.getVariant())) {
                iterator.remove();
            }
        }
        
        if (!availableLocales.contains(DEFAULT_LOCALE)) {
            availableLocales.add(DEFAULT_LOCALE);
        }
        
        Map<String, Locale> map = new HashMap<String, Locale>();
        for (Locale locale : availableLocales) {
            map.put(locale.toString(), locale);
        }
        
        LOCALE_MAP = Collections.unmodifiableMap(map);
    }

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("America/Los_Angeles");
    public static final String DEFAULT_TIME_ZONE_KEY;
    public static final Map<String, TimeZone> TIME_ZONE_MAP;
    static {
        String[] availableIDs = TimeZone.getAvailableIDs();
        
        DEFAULT_TIME_ZONE_KEY = AllowedSettingValues.DEFAULT_TIME_ZONE.getID();
        if (!ArrayUtils.contains(availableIDs, DEFAULT_TIME_ZONE_KEY)) {
            ArrayUtils.add(availableIDs, DEFAULT_TIME_ZONE_KEY);
        }
        
        Map<String, TimeZone> map = new HashMap<String, TimeZone>();
        for (String timeZoneId : availableIDs) {
            map.put(timeZoneId, TimeZone.getTimeZone(timeZoneId));
        }
        
        TIME_ZONE_MAP = Collections.unmodifiableMap(map);
    }

    public static final String DEFAULT_TAG_SYNTAX_KEY = "angleBracket";
    public static final Map<String, Integer> TAG_SYNTAX_MAP = ImmutableMap.of(
    		"angleBracket", Configuration.ANGLE_BRACKET_TAG_SYNTAX,
    		"squareBracket", Configuration.SQUARE_BRACKET_TAG_SYNTAX,
    		"autoDetect", Configuration.AUTO_DETECT_TAG_SYNTAX);
    public static final int DEFAULT_TAG_SYNTAX = TAG_SYNTAX_MAP.get(DEFAULT_TAG_SYNTAX_KEY);
    
    public static final String DEFAULT_INTERPOLATION_SYNTAX_KEY = "legacy";
    public static final Map<String, Integer> INTERPOLATION_SYNTAX_MAP = ImmutableMap.of(
    		"legacy", Configuration.LEGACY_INTERPOLATION_SYNTAX,
    		"dollar", Configuration.DOLLAR_INTERPOLATION_SYNTAX,
    		"squareBracket", Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
    public static final int DEFAULT_INTERPOLATION_SYNTAX = Configuration.LEGACY_INTERPOLATION_SYNTAX;
    
}
