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

package org.apache.freemarker.onlinetester.dropwizard;

import com.berico.fallwizard.SpringConfiguration;
import com.berico.fallwizard.SpringService;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.views.ViewBundle;

public class ApplicationStartup extends SpringService<SpringConfiguration> {

    public static void main(String[] args) throws Exception {
        new ApplicationStartup().run(args);
    }

    @Override
    public void initialize(Bootstrap<SpringConfiguration> bootstrap) {
        bootstrap.setName("freemarker-online");
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css"));
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/js"));
    }

}