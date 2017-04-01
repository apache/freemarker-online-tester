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
package com.kenshoo.freemarker.platform;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: shlomis
 * Date: 9/8/13
 * Time: 10:50 PM
 */
public class YamlPropertiesPersister implements PropertiesPersister {
    @Override
    public void load(Properties props, InputStream is) throws IOException {
        load(props, new InputStreamReader(is));
    }

    /**
     * We want to traverse map representing Yaml object and each time we find String=String pair we want to
     * save it as Property. As we are going deeper into map we generate compound key as path-like String
     *
     * @see org.springframework.util.PropertiesPersister#load(java.util.Properties, java.io.Reader)
     */
    @Override
    public void load(Properties props, Reader reader) throws IOException {
        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) yaml.load(reader);
        // now we can populate supplied props
        assignProperties(props, map, null);
    }

    @SuppressWarnings("unchecked")
    public void assignProperties(Properties props, Map<String, Object> map, String path) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isEmpty(path))
                key = path + "." + key;
            Object val = entry.getValue();
            if (val instanceof String) {
                // see if we need to create a compound key
                props.put(key, val);
            } else if (val instanceof Map) {
                assignProperties(props, (Map<String, Object>) val, key);
            }
        }
    }

    @Override
    public void store(Properties props, OutputStream os, String header) throws IOException {
        throw new IllegalStateException("Current implementation is a read-only");
    }

    @Override
    public void store(Properties props, Writer writer, String header) throws IOException {
        throw new IllegalStateException("Current implementation is a read-only");
    }

    @Override
    public void loadFromXml(Properties props, InputStream is) throws IOException {
        throw new IllegalStateException("Use DefaultPropertiesPersister if you want to read/write XML");
    }

    @Override
    public void storeToXml(Properties props, OutputStream os, String header) throws IOException {
        throw new IllegalStateException("Use DefaultPropertiesPersister if you want to load/store to XML");
    }

    @Override
    public void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException {
        throw new IllegalStateException("Use DefaultPropertiesPersister if you want to read/write XML");
    }
}
