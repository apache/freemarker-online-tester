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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kenshoo.freemarker.model.ErrorCode;
import com.kenshoo.freemarker.model.ErrorResponse;
import com.kenshoo.freemarker.model.ExecuteRequest;
import com.kenshoo.freemarker.model.ExecuteResourceField;
import com.kenshoo.freemarker.model.ExecuteResourceProblem;
import com.kenshoo.freemarker.model.ExecuteResponse;
import com.kenshoo.freemarker.services.AllowedSettingValuesMaps;
import com.kenshoo.freemarker.services.FreeMarkerService;
import com.kenshoo.freemarker.services.FreeMarkerServiceResponse;
import com.kenshoo.freemarker.util.DataModelParser;
import com.kenshoo.freemarker.util.DataModelParsingException;
import com.kenshoo.freemarker.util.ExceptionUtils;

import freemarker.core.OutputFormat;

/**
 * Created by pradeep on 8/28/2015.
 */
@Path("/api/execute")
@Component
public class FreeMarkerOnlineExecuteResource {
    private static final int MAX_TEMPLATE_INPUT_LENGTH = 10000;

    private static final int MAX_DATA_MODEL_INPUT_LENGTH = 10000;

    private static final String MAX_TEMPLATE_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE
            = "The template length has exceeded the {0} character limit set for this service.";

    private static final String MAX_DATA_MODEL_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE
            = "The data model length has exceeded the {0} character limit set for this service.";

    private static final String UNKNOWN_OUTPUT_FORMAT_ERROR_MESSAGE = "Unknown output format: {0}";
    private static final String UNKNOWN_LOCALE_ERROR_MESSAGE = "Unknown locale: {0}";
    private static final String UNKNOWN_TIME_ZONE_ERROR_MESSAGE = "Unknown time zone: {0}";

    private static final String SERVICE_OVERBURDEN_ERROR_MESSAGE
            = "Sorry, the service is overburden and couldn't handle your request now. Try again later.";

    static final String DATA_MODEL_ERROR_MESSAGE_HEADING = "Failed to parse data model:";
    static final String DATA_MODEL_ERROR_MESSAGE_FOOTER = "Note: This is NOT a FreeMarker error message. "
            + "The data model syntax is specific to this online service.";

    @Autowired
    private FreeMarkerService freeMarkerService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response formResult(
            ExecuteRequest req) {
        ExecuteResponse resp = new ExecuteResponse();
        
        if (StringUtils.isBlank(req.getTemplate()) && StringUtils.isBlank(req.getDataModel())) {
            return Response.status(400).entity("Empty Template & data").build();
        }

        List<ExecuteResourceProblem> problems = new ArrayList<ExecuteResourceProblem>();
        
        String template = getTemplate(req, problems);
        Map<String, Object> dataModel = getDataModel(req, problems);
        OutputFormat outputFormat = getOutputFormat(req, problems);
        Locale locale = getLocale(req, problems);
        TimeZone timeZone = getTimeZone(req, problems);
        
        if (!problems.isEmpty()) {
            resp.setProblems(problems);
            return buildFreeMarkerResponse(resp);
        }
        
        FreeMarkerServiceResponse freeMarkerServiceResponse;
        try {
            freeMarkerServiceResponse = freeMarkerService.calculateTemplateOutput(
                    template, dataModel,
                    outputFormat, locale, timeZone);
        } catch (RejectedExecutionException e) {
            String error = SERVICE_OVERBURDEN_ERROR_MESSAGE;
            return Response.serverError().entity(new ErrorResponse(ErrorCode.FREEMARKER_SERVICE_TIMEOUT, error)).build();
        }
        if (!freeMarkerServiceResponse.isSuccesful()){
            Throwable failureReason = freeMarkerServiceResponse.getFailureReason();
            String error = ExceptionUtils.getMessageWithCauses(failureReason);
            problems.add(new ExecuteResourceProblem(ExecuteResourceField.TEMPLATE, error));
            resp.setProblems(problems);
            return buildFreeMarkerResponse(resp);
        }

        String result = freeMarkerServiceResponse.getTemplateOutput();
        resp.setResult(result);
        resp.setTruncatedResult(freeMarkerServiceResponse.isTemplateOutputTruncated());
        return buildFreeMarkerResponse(resp);
    }

    private String getTemplate(ExecuteRequest req, List<ExecuteResourceProblem> problems) {
        String template = req.getTemplate();
        
        if (template.length() > MAX_TEMPLATE_INPUT_LENGTH) {
            String error = formatMessage(MAX_TEMPLATE_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE, MAX_TEMPLATE_INPUT_LENGTH);
            problems.add(new ExecuteResourceProblem(ExecuteResourceField.TEMPLATE, error));
            return null;
        }
        
        return template;
    }

    private Map<String, Object> getDataModel(ExecuteRequest req, List<ExecuteResourceProblem> problems) {
        String dataModel = req.getDataModel();
        
        if (dataModel.length() > MAX_DATA_MODEL_INPUT_LENGTH) {
            String error = formatMessage(
                    MAX_DATA_MODEL_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE, MAX_DATA_MODEL_INPUT_LENGTH);
            problems.add(new ExecuteResourceProblem(ExecuteResourceField.DATA_MODEL, error));
            return null;
        }
        
        try {
            return DataModelParser.parse(dataModel, freeMarkerService.getFreeMarkerTimeZone());
        } catch (DataModelParsingException e) {
            problems.add(new ExecuteResourceProblem(ExecuteResourceField.DATA_MODEL, decorateResultText(e.getMessage())));
            return null;
        }
    }

    private OutputFormat getOutputFormat(ExecuteRequest req, List<ExecuteResourceProblem> problems) {
        String outputFormatStr = req.getOutputFormat();
        
        if (StringUtils.isBlank(outputFormatStr)) {
            return AllowedSettingValuesMaps.DEFAULT_OUTPUT_FORMAT;
        }
    
        OutputFormat outputFormat = AllowedSettingValuesMaps.OUTPUT_FORMAT_MAP.get(outputFormatStr);
        if (outputFormat == null) {
            problems.add(new ExecuteResourceProblem(
                    ExecuteResourceField.OUTPUT_FORMAT,
                    formatMessage(UNKNOWN_OUTPUT_FORMAT_ERROR_MESSAGE, outputFormatStr)));
        }
        return outputFormat;
    }

    private Locale getLocale(ExecuteRequest req, List<ExecuteResourceProblem> problems) {
        String localeStr = req.getLocale();
        
        if (StringUtils.isBlank(localeStr)) {
            return AllowedSettingValuesMaps.DEFAULT_LOCALE;
        }
        
        Locale locale = AllowedSettingValuesMaps.LOCALE_MAP.get(localeStr);
        if (locale == null) {
            problems.add(new ExecuteResourceProblem(
                    ExecuteResourceField.LOCALE,
                    formatMessage(UNKNOWN_LOCALE_ERROR_MESSAGE, localeStr)));
        }
        return locale;
    }

    private TimeZone getTimeZone(ExecuteRequest req, List<ExecuteResourceProblem> problems) {
        String timeZoneStr = req.getTimeZone();
        
        if (StringUtils.isBlank(timeZoneStr)) {
            return AllowedSettingValuesMaps.DEFAULT_TIME_ZONE;
        }
        
        TimeZone timeZone = AllowedSettingValuesMaps.TIME_ZONE_MAP.get(timeZoneStr);
        if (timeZone == null) {
            problems.add(new ExecuteResourceProblem(
                    ExecuteResourceField.TIME_ZONE,
                    formatMessage(UNKNOWN_TIME_ZONE_ERROR_MESSAGE, timeZoneStr)));
        }
        return timeZone;
    }

    private Response buildFreeMarkerResponse(ExecuteResponse executeResponse){
        return Response.ok().entity(executeResponse).build();
    }
    
    private String decorateResultText(String resultText) {
        return DATA_MODEL_ERROR_MESSAGE_HEADING + "\n\n" + resultText + "\n\n" + DATA_MODEL_ERROR_MESSAGE_FOOTER;
    }
    
    private String formatMessage(String key, Object... params) {
        return new MessageFormat(key, Locale.US).format(params);
    }
    
}
