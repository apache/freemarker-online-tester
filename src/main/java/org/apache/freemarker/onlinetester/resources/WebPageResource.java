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

package org.apache.freemarker.onlinetester.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.freemarker.onlinetester.view.FreeMarkerOnlineView;

/**
 * The HTML web page shown in the browser.
 */
@Path("/")
public class WebPageResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public FreeMarkerOnlineView blankForm() {
        return new FreeMarkerOnlineView();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public FreeMarkerOnlineView formResult(
            @FormParam("template") String template,
            @FormParam("dataModel") String dataModel,
            @FormParam("outputFormat") String outputFormat,
            @FormParam("locale") String locale,
            @FormParam("timeZone") String timeZone,
            @FormParam("tagSyntax") String tagSyntax,
            @FormParam("interpolationSyntax") String interpolationSyntax) {
        FreeMarkerOnlineView view = new FreeMarkerOnlineView();
        view.setTemplate(template);
        view.setDataModel(dataModel);
        view.setOutputFormat(outputFormat);
        view.setLocale(locale);
        view.setTimeZone(timeZone);
        view.setExecute(true);
        return view;
    }

}
