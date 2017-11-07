package org.apache.freemarker.onlinetester.dropwizard;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class FreeMarkerOnlineTesterConfiguration extends Configuration {

    private Map<String, Map<String, String>> viewRendererConfiguration;

	@JsonProperty("viewRendererConfiguration")
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return viewRendererConfiguration;
    }
    
    @JsonProperty("viewRendererConfiguration")
    public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
        this.viewRendererConfiguration = viewRendererConfiguration;
    }
    
}
