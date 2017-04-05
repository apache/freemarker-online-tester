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

package freemarker.core;

import freemarker.core.ThreadInterruptionSupportTemplatePostProcessor.TemplateProcessingThreadInterruptedException;
import freemarker.template.Template;

/**
 * Functions that depend on unpublished FreeMarker functionality. Might need to be adjusted for new FreeMarker releases.
 * The relevant parts of the FreeMarker source code contains comments about keeping this in sync with that, so,
 * hopefully this won't be a problem.
 */
public final class FreeMarkerInternalsAccessor {

    /**
     * Ensures that the template will react to {@link #interruptTemplateProcessing(Thread)}. 
     */
    public static void makeTemplateInterruptable(Template template) {
        _CoreAPI.addThreadInterruptedChecks(template);
    }

    /**
     * Checks if the template processing has thrown exception because of a {@link #interruptTemplateProcessing(Thread)}
     * call.
     */
    public static boolean isTemplateProcessingInterruptedException(Throwable e) {
        return e instanceof TemplateProcessingThreadInterruptedException;
    }

    /**
     * Tells a template processing in another thread to abort; asynchronous.
     */
    public static void interruptTemplateProcessing(Thread t) {
        t.interrupt();
    }

    /**
     * Called from the thread where the interruptible template execution ran earlier, to clear any related thread state.
     */
    public static void clearAnyPendingTemplateProcessingInterruption() {
        Thread.interrupted();  // To clears the interruption flag 
    }
    
    private FreeMarkerInternalsAccessor() {
        // Not meant to be instantiated
    }

}
