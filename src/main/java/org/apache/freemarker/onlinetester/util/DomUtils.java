/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.onlinetester.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DomUtils {
    /**
     * Creates a {@link DocumentBuilder} that's hardened for parsing XML coming from an untrusted user: {@code DOCTYPE}
     * declarations are disallowed, so no external entities can be resolved (XXE: local file disclosure, SSRF from the
     * server), nor internal entities expanded (entity-expansion DoS).
     */
    public static DocumentBuilder newSecureDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Same settings as in NodeModel.getDocumentBuilderFactory(), which the wrapped Document-s expect:
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        // Primary defense; this failing (very old or exotic JAXP implementation) must fail the parsing:
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // Defense in depth follows; not supported by all JAXP implementations, hence only set if possible:
        trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        trySetFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException e) {
            // Pre-JAXP-1.5 implementation; the disallowed DOCTYPE-s already prevent external access.
        }
        return factory.newDocumentBuilder();
    }

    private static void trySetFeature(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
            // The implementation doesn't know this feature; the mandatory disallow-doctype-decl still protects.
        }
    }
}
