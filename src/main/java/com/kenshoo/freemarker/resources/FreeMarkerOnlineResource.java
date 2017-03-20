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
package com.kenshoo.freemarker.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.kenshoo.freemarker.view.FreeMarkerOnlineView;

/**
 * Created with IntelliJ IDEA.
 * User: shlomis
 * Date: 9/1/13
 * Time: 4:35 PM
 */
@Path("/")
@Component
public class FreeMarkerOnlineResource {

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
            @FormParam("timeZone") String timeZone) {
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
