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

import com.google.common.io.Resources;
import com.kenshoo.freemarker.dropwizard.ApplicationStartup;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * Created with IntelliJ IDEA.
 * User: shlomis
 * Date: 9/9/13
 * Time: 10:43 AM
 */
public class DropWizardServiceTest {
    @ClassRule
    public static TestRule testRule = new DropwizardServiceRule<>(ApplicationStartup.class,
            Resources.getResource("freemarker-online.yml").getPath());


    @Test
    public void testServerIsUp() throws Exception {
        ((DropwizardServiceRule) testRule).getService();
    }
}
