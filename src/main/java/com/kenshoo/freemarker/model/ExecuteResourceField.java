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
package com.kenshoo.freemarker.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Pmuruge on 8/31/2015.
 */
public enum ExecuteResourceField {
    DATA_MODEL("dataModel"),
    TEMPLATE("template"),
    OUTPUT_FORMAT("outputFormat"),
    LOCALE("locale"),
    TIME_ZONE("timeZone");
    
    private final String fieldName;
    
    private ExecuteResourceField(String filedName) {
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
    public static ExecuteResourceField fromEnumString(String val) {
        for(ExecuteResourceField field : values()) {
            if(field.getFieldName().equals(val)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid string value passed: " + val);
    }
    
}
