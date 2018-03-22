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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.freemarker.onlinetester.model.ErrorCode;
import org.apache.freemarker.onlinetester.model.ErrorResponse;
import org.apache.freemarker.onlinetester.model.ExecuteRequest;
import org.apache.freemarker.onlinetester.model.ExecuteResponse;
import org.apache.freemarker.onlinetester.model.ExecuteResponseProblem;
import org.apache.freemarker.onlinetester.services.AllowedSettingValues;
import org.apache.freemarker.onlinetester.services.FreeMarkerService;
import org.apache.freemarker.onlinetester.services.FreeMarkerService.ExecuteTemplateArgs;
import org.apache.freemarker.onlinetester.services.FreeMarkerServiceResponse;
import org.apache.freemarker.onlinetester.util.DataModelParser;
import org.apache.freemarker.onlinetester.util.DataModelParsingException;
import org.apache.freemarker.onlinetester.util.ExceptionUtils;

import freemarker.template.Configuration;
import freemarker.template.utility.StringUtil;

/**
 * AJAX API for executing the template submitted.
 */
@Path("/api/execute")
public class ExecuteApiResource {
    private static final int MAX_TEMPLATE_INPUT_LENGTH = 10000;

    private static final int MAX_DATA_MODEL_INPUT_LENGTH = 10000;

    private static final String MAX_TEMPLATE_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE
            = "The template length has exceeded the {0} character limit set for this service.";

    private static final String MAX_DATA_MODEL_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE
            = "The data model length has exceeded the {0} character limit set for this service.";

    private static final String SERVICE_TIMEOUT_ERROR_MESSAGE
            = "Sorry, the service is overburden and couldn't handle your request now. Try again later.";

    static final String DATA_MODEL_ERROR_MESSAGE_HEADING = "Failed to parse data model:";
    static final String DATA_MODEL_ERROR_MESSAGE_FOOTER = "Note: This is NOT a FreeMarker error message. "
            + "The data model syntax is specific to this online service.";

    public static final int DEFAULT_TAG_SYNTAX = Configuration.ANGLE_BRACKET_TAG_SYNTAX;
    public static final int DEFAULT_INTERPLOATION_SYNTAX = Configuration.LEGACY_INTERPOLATION_SYNTAX;
    
    private final FreeMarkerService freeMarkerService;

    public ExecuteApiResource(FreeMarkerService freeMarkerService) {
        this.freeMarkerService = freeMarkerService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formResult(
            ExecuteRequest req) {
        ExecuteResponse resp = new ExecuteResponse();
        
        if (StringUtils.isBlank(req.getTemplate()) && StringUtils.isBlank(req.getDataModel())) {
            return Response.status(400).entity("Empty Template & data").build();
        }

        List<ExecuteResponseProblem> problems = new ArrayList<>();
        
        ExecuteTemplateArgs serviceArgs = new ExecuteTemplateArgs()
        		.templateSourceCode(lengthCheckAndGetTemplate(req, problems))
        		.dataModel(parseDataModel(req, problems))
        		.outputFormat(parseChoiceField(
		        		ExecuteRequest.Field.OUTPUT_FORMAT, req.getOutputFormat(),
		        		AllowedSettingValues.DEFAULT_OUTPUT_FORMAT, AllowedSettingValues.OUTPUT_FORMAT_MAP,
		        		problems))
        		.locale(parseChoiceField(
		        		ExecuteRequest.Field.LOCALE, req.getLocale(),
		        		AllowedSettingValues.DEFAULT_LOCALE, AllowedSettingValues.LOCALE_MAP,
		        		problems))
        		.timeZone(parseChoiceField(
		        		ExecuteRequest.Field.TIME_ZONE, req.getTimeZone(),
		        		AllowedSettingValues.DEFAULT_TIME_ZONE, AllowedSettingValues.TIME_ZONE_MAP,
		        		problems))
        		.tagSyntax(parseChoiceField(
		        		ExecuteRequest.Field.TAG_SYNTAX, req.getTagSyntax(),
		        		AllowedSettingValues.DEFAULT_TAG_SYNTAX, AllowedSettingValues.TAG_SYNTAX_MAP,
		        		problems))
        		.interpolationSyntax(parseChoiceField(
		        		ExecuteRequest.Field.INTERPOLATION_SYNTAX, req.getInterpolationSyntax(),
		        		AllowedSettingValues.DEFAULT_INTERPOLATION_SYNTAX, AllowedSettingValues.INTERPOLATION_SYNTAX_MAP,
		        		problems));
        
        if (!problems.isEmpty()) {
            resp.setProblems(problems);
            return buildFreeMarkerResponse(resp);
        }
        
        FreeMarkerServiceResponse serviceResponse;
        try {
            serviceResponse = freeMarkerService.executeTemplate(serviceArgs);
        } catch (RejectedExecutionException e) {
            String error = SERVICE_TIMEOUT_ERROR_MESSAGE;
            return Response.serverError().entity(new ErrorResponse(ErrorCode.FREEMARKER_SERVICE_TIMEOUT, error)).build();
        }
        if (!serviceResponse.isSuccesful()){
            Throwable failureReason = serviceResponse.getFailureReason();
            String error = ExceptionUtils.getMessageWithCauses(failureReason);
            problems.add(new ExecuteResponseProblem(ExecuteRequest.Field.TEMPLATE, error));
            resp.setProblems(problems);
            return buildFreeMarkerResponse(resp);
        }

        String result = serviceResponse.getTemplateOutput();
        resp.setResult(result);
        resp.setTruncatedResult(serviceResponse.isTemplateOutputTruncated());
        return buildFreeMarkerResponse(resp);
    }

    private String lengthCheckAndGetTemplate(ExecuteRequest req, List<ExecuteResponseProblem> problems) {
        String template = req.getTemplate();
        if (template != null && template.length() > MAX_TEMPLATE_INPUT_LENGTH) {
            String error = formatMessage(MAX_TEMPLATE_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE, MAX_TEMPLATE_INPUT_LENGTH);
            problems.add(new ExecuteResponseProblem(ExecuteRequest.Field.TEMPLATE, error));
            return null;
        }
        return template;
    }

    private Map<String, Object> parseDataModel(ExecuteRequest req, List<ExecuteResponseProblem> problems) {
        String dataModel = req.getDataModel();

        if (dataModel.length() > MAX_DATA_MODEL_INPUT_LENGTH) {
            String error = formatMessage(
                    MAX_DATA_MODEL_INPUT_LENGTH_EXCEEDED_ERROR_MESSAGE, MAX_DATA_MODEL_INPUT_LENGTH);
            problems.add(new ExecuteResponseProblem(ExecuteRequest.Field.DATA_MODEL, error));
            return null;
        }
        
        try {
            return DataModelParser.parse(dataModel, freeMarkerService.getFreeMarkerTimeZone());
        } catch (DataModelParsingException e) {
            problems.add(new ExecuteResponseProblem(ExecuteRequest.Field.DATA_MODEL, decorateResultText(e.getMessage())));
            return null;
        }
    }

    private <T> T parseChoiceField(ExecuteRequest.Field name, String rawValue, T defaultValue,
    		Map<String, ? extends T> rawToParsedMap, List<ExecuteResponseProblem> problems) {
        if (StringUtils.isBlank(rawValue)) {
            return defaultValue;
        }
        
        T parsedValue = rawToParsedMap.get(rawValue);
        if (parsedValue == null) {
            problems.add(new ExecuteResponseProblem(name,
            		formatMessage("Invalid value for \"{0}\": {1}", name, StringUtil.jQuote(rawValue))));
        }
        return parsedValue;
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
