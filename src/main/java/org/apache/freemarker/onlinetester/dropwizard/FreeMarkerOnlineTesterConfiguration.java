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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.bundles.assets.AssetsBundleConfiguration;
import io.dropwizard.bundles.assets.AssetsConfiguration;

public class FreeMarkerOnlineTesterConfiguration extends Configuration implements AssetsBundleConfiguration {

    private Map<String, Map<String, String>> viewRendererConfiguration;
	private AssetsConfiguration assetsConfiguration;

	@JsonProperty("viewRendererConfiguration")
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return viewRendererConfiguration;
    }
    
    @JsonProperty("viewRendererConfiguration")
    public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
        this.viewRendererConfiguration = viewRendererConfiguration;
    }
    
    @JsonProperty("assets")
    @Override
    public AssetsConfiguration getAssetsConfiguration() {
      return assetsConfiguration;
    }

    @JsonProperty("assets")
	public void setAssetsConfiguration(AssetsConfiguration assetsConfiguration) {
		this.assetsConfiguration = assetsConfiguration;
	}
    
}
