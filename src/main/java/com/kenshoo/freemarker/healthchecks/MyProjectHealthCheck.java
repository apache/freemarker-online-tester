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
package com.kenshoo.freemarker.healthchecks;

import com.yammer.metrics.core.HealthCheck;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: tzachz
 * Date: 5/23/13
 */
@Component
public class MyProjectHealthCheck extends HealthCheck {

    // note that this is due to the default spring CTR
    public MyProjectHealthCheck() {
        super("MyProjectHealthCheck");
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy(); // we're always healthy!
    }
}
