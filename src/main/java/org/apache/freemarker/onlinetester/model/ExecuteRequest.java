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
 package org.apache.freemarker.onlinetester.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class ExecuteRequest {
    private String template;
    private String dataModel;
    private String outputFormat;
    private String locale;
    private String timeZone;
    private String tagSyntax;
    private String interpolationSyntax;

    public static enum Field {
        DATA_MODEL("dataModel"),
        TEMPLATE("template"),
        OUTPUT_FORMAT("outputFormat"),
        LOCALE("locale"),
        TIME_ZONE("timeZone"),
    	TAG_SYNTAX("tagSyntax"),
    	INTERPOLATION_SYNTAX("interpolationSyntax");
        
        private final String fieldName;
        
        private Field(String filedName) {
            this.fieldName = filedName;
        }
        
        public String toString() {
            return getFieldName();
        }
        
        @JsonValue
        public String getFieldName() {
            return fieldName;
        }

        @JsonCreator
        public static Field fromEnumString(String val) {
            for(Field field : values()) {
                if(field.getFieldName().equals(val)) {
                    return field;
                }
            }
            throw new IllegalArgumentException("Invalid string value passed: " + val);
        }
        
    }
    
    public ExecuteRequest() {
    }

    public ExecuteRequest(String template, String dataModel) {
        this.template = template;
        this.dataModel = dataModel;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public String getTemplate() {

        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

	public String getTagSyntax() {
		return tagSyntax;
	}

	public void setTagSyntax(String tagSyntax) {
		this.tagSyntax = tagSyntax;
	}

	public String getInterpolationSyntax() {
		return interpolationSyntax;
	}

	public void setInterpolationSyntax(String interpolationSyntax) {
		this.interpolationSyntax = interpolationSyntax;
	}
    
}
