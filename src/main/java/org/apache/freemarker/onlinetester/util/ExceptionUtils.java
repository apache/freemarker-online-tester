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

import freemarker.core.ParseException;
import freemarker.template.TemplateException;

public final class ExceptionUtils {

    private ExceptionUtils() {
        // Not meant to be instantiated
    }

    /**
     * The error message (and sometimes also the class), and then the same with the cause exception, and so on. Doesn't
     * contain the stack trace or other location information.
     */
    public static String getMessageWithCauses(final Throwable exc) {
        StringBuilder sb = new StringBuilder();
        
        Throwable curExc = exc;
        while (curExc != null) {
            if (curExc != exc) {
                sb.append("\n\nCaused by:\n");
            }
            String msg = curExc.getMessage();
            if (msg == null || !(curExc instanceof TemplateException || curExc instanceof ParseException)) {
                sb.append(curExc.getClass().getName()).append(": ");
            }
            sb.append(msg);
            curExc = curExc.getCause();
        }
        return sb.toString();
    }

}
