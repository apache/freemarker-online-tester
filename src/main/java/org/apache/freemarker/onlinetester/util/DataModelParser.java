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

package org.apache.freemarker.onlinetester.util;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.ext.dom.NodeModel;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.DateUtil.CalendarFieldsToDateConverter;
import freemarker.template.utility.DateUtil.DateParseException;
import freemarker.template.utility.DateUtil.TrivialCalendarFieldsToDateConverter;

/**
 * Parses the text that the user enters into the data model input field.
 */
public final class DataModelParser {

    private static final String KEYWORD_NEGATIVE_INFINITY = "-Infinity";

    private static final String KEYWORD_POSITIVE_INFINITY = "+Infinity";

    private static final String KEYWORD_INFINITY = "Infinity";

    private static final String KEYWORD_TRUE = "true";

    private static final String KEYWORD_FALSE = "false";

    private static final String KEYWORD_NULL = "null";

    private static final String KEYWORD_NAN = "NaN";

    /** Matches a line starting like "someVariable=". */
    private static final Pattern ASSIGNMENT_START = Pattern.compile(
            "^\\s*"
            + "(\\p{L}[\\p{L}\\p{N}\\.:\\-_$@]*)" // name
            + "[ \t]*=\\s*",
            Pattern.MULTILINE);

    /** Matches a value that starts like a number, or probably meant to be number at least. */
    private static final Pattern NUMBER_LIKE = Pattern.compile("[+-]?[\\.,]?[0-9].*", Pattern.DOTALL);
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private DataModelParser() {
        // Not meant to be instantiated
    }

    public static Map<String, Object> parse(String src, TimeZone timeZone) throws DataModelParsingException {
        if (StringUtils.isBlank(src)) {
            return Collections.emptyMap();
        }

        Map<String, Object> dataModel = new LinkedHashMap<>();

        String lastName = null;
        int lastAssignmentStartEnd = 0;
        final Matcher assignmentStart = ASSIGNMENT_START.matcher(src);
        findAssignments: while (true) {
            boolean hasNextAssignment = assignmentStart.find(lastAssignmentStartEnd);

            if (lastName != null) {
                String value = src.substring(
                        lastAssignmentStartEnd, hasNextAssignment ? assignmentStart.start() : src.length())
                        .trim();
                final Object parsedValue;
                try {
                    parsedValue = parseValue(value, timeZone);
                } catch (DataModelParsingException e) {
                    throw new DataModelParsingException(
                            "Failed to parse the value of \"" + lastName + "\":\n" + e.getMessage(), e.getCause());
                }
                dataModel.put(lastName, parsedValue);
            }

            if (lastName == null && (!hasNextAssignment || assignmentStart.start() != 0)) {
                throw new DataModelParsingException(
                        "The data model specification must start with an assignment (name=value).");
            }

            if (!hasNextAssignment) {
                break findAssignments;
            }

            lastName = assignmentStart.group(1).trim();
            lastAssignmentStartEnd = assignmentStart.end();
        }

        return dataModel;
    }
    
    private static Object parseValue(String value, TimeZone timeZone) throws DataModelParsingException {
        // Note: Because we fall back to interpret the input as a literal string value when it doesn't look like
        // anything else (like a number, boolean, etc.), it's important to avoid misunderstandings, and throw exception
        // in suspicious situations. The user can always quote the string value if we are "too smart". But he will
        // be confused about the rules of FreeMarker if what he believes to be a non-string is misinterpreted by this
        // parser as a string. Getting sometimes an error and then quoting the string is better than that.
        
        if (value.endsWith(";")) {  // Tolerate this habit of Java and JavaScript programmers
            value = value.substring(value.length() - 1).trim();
        }
        
        if (NUMBER_LIKE.matcher(value).matches()) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                // Maybe it's a ISO 8601 Date/time/datetime
                CalendarFieldsToDateConverter calToDateConverter = new TrivialCalendarFieldsToDateConverter();
                
                DateParseException attemptedTemportalPExc = null;
                String attemptedTemporalType = null;
                final int dashIdx = value.indexOf('-');
                final int colonIdx = value.indexOf(':');
                if (value.indexOf('T') > 1 || (dashIdx > 1 && colonIdx > dashIdx)) {
                    try {
                        return new Timestamp(
                                DateUtil.parseISO8601DateTime(value, timeZone, calToDateConverter).getTime());
                    } catch (DateParseException pExc) {
                        attemptedTemporalType = "date-time";
                        attemptedTemportalPExc = pExc;
                    }
                } else if (dashIdx > 1) {
                    try {
                        return new java.sql.Date(
                                DateUtil.parseISO8601Date(value, timeZone, calToDateConverter).getTime());
                    } catch (DateParseException pExc) {
                        attemptedTemporalType = "date";
                        attemptedTemportalPExc = pExc;
                    }
                } else if (colonIdx > 1) { 
                    try {
                        return new Time(
                                DateUtil.parseISO8601Time(value, timeZone, calToDateConverter).getTime());
                    } catch (DateParseException pExc) {
                        attemptedTemporalType = "time";
                        attemptedTemportalPExc = pExc;
                    }
                }
                if (attemptedTemportalPExc == null) {
                    throw new DataModelParsingException("Malformed number: " + value, e);
                } else {
                    throw new DataModelParsingException(
                            "Malformed ISO 8601 " + attemptedTemporalType + " (or malformed number): " + 
                            attemptedTemportalPExc.getMessage(), e.getCause());
                }
            }
        } else if (value.startsWith("\"")) {
            try {
                return JSON_MAPPER.readValue(value, String.class);
            } catch (IOException e) {
                throw new DataModelParsingException(
                        "Malformed quoted string (using JSON syntax): " + getMessageWithoutLocation(e),
                        e);
            }
        } else if (value.startsWith("\'")) {
            throw new DataModelParsingException(
                    "Malformed quoted string (using JSON syntax): Use \" character for quotation, not \' character.");
        } else if (value.startsWith("[")) {
            try {
                return JSON_MAPPER.readValue(value, List.class);
            } catch (IOException e) {
                throw new DataModelParsingException(
                        "Malformed list (using JSON syntax): " + getMessageWithoutLocation(e),
                        e);
            }
        } else if (value.startsWith("{")) {
            try {
                return JSON_MAPPER.readValue(value, LinkedHashMap.class);
            } catch (IOException e) {
                throw new DataModelParsingException(
                        "Malformed list (using JSON syntax): " + getMessageWithoutLocation(e),
                        e);
            }
        } else if (value.startsWith("<")) {
            try {
                DocumentBuilder builder = NodeModel.getDocumentBuilderFactory().newDocumentBuilder();
                ErrorHandler errorHandler = NodeModel.getErrorHandler();
                if (errorHandler != null) builder.setErrorHandler(errorHandler);
                final Document doc = builder.parse(new InputSource(new StringReader(value)));
                NodeModel.simplify(doc);
                return doc;
            } catch (SAXException e) {
                final String saxMsg = e.getMessage();
                throw new DataModelParsingException("Malformed XML: " + (saxMsg != null ? saxMsg : e), e);
            } catch (Exception e) {
                throw new DataModelParsingException("XML parsing has failed with internal error: " + e, e);
            }
        } else if (value.equalsIgnoreCase(KEYWORD_TRUE)) {
            checkKeywordCase(value, KEYWORD_TRUE);
            return Boolean.TRUE;
        } else if (value.equalsIgnoreCase(KEYWORD_FALSE)) {
            checkKeywordCase(value, KEYWORD_FALSE);
            return Boolean.FALSE;
        } else if (value.equalsIgnoreCase(KEYWORD_NULL)) {
            checkKeywordCase(value, KEYWORD_NULL);
            return null;
        } else if (value.equalsIgnoreCase(KEYWORD_NAN)) {
            checkKeywordCase(value, KEYWORD_NAN);
            return Double.NaN;
        } else if (value.equalsIgnoreCase(KEYWORD_INFINITY)) {
            checkKeywordCase(value, KEYWORD_INFINITY);
            return Double.POSITIVE_INFINITY;
        } else if (value.equalsIgnoreCase(KEYWORD_POSITIVE_INFINITY)) {
            checkKeywordCase(value, KEYWORD_POSITIVE_INFINITY);
            return Double.POSITIVE_INFINITY;
        } else if (value.equalsIgnoreCase(KEYWORD_NEGATIVE_INFINITY)) {
            checkKeywordCase(value, KEYWORD_NEGATIVE_INFINITY);
            return Double.NEGATIVE_INFINITY;
        } else if (value.length() == 0) {
            throw new DataModelParsingException(
                    "Empty value. (If you indeed wanted a 0 length string, quote it, like \"\".)");
        } else {
            return value;
        }
    }

    private static String getMessageWithoutLocation(IOException e) {
        return e instanceof JsonProcessingException
                ? ((JsonProcessingException) e).getOriginalMessage()
                : e.getMessage();
    }

    private static void checkKeywordCase(String inputKeyword, String correctKeyword) throws DataModelParsingException {
        if (!correctKeyword.equals(inputKeyword)) {
            throw new DataModelParsingException("Keywords are case sensitive; the correct form is: "
                    + correctKeyword);
        }
    }

}
