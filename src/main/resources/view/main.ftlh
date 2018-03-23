<#--

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

-->

<#import "utils.ftlh" as u>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link rel="stylesheet" href="assets/main.css">
    <link rel="stylesheet" href="https://unpkg.com/purecss@1.0.0/build/pure-min.css">
    
    <script src="https://code.jquery.com/jquery-1.11.2.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.blockUI/2.70/jquery.blockUI.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/autosize.js/3.0.8/autosize.min.js"></script>
    <script src="assets/script.js?v=4"></script><!-- Always increase v if the script.js is changed! -->
    <script>
        $(function() {
            // Auto-focus on first form input:
            $('#templateAndModelForm *:input[type!=hidden]:first').focus();
            
            // Submit form when Ctrl+Enter is hit in a textarea:            

        
            // Dynamically adapt text areas heights to their content:
            //$('#templateAndModelForm textarea').autosize();
            autosize($('textarea'));
        
            // Show/hide data model examples:
            $("#showHideDataModelExamples").click(function(e) {
            	 $("#dataModelExamples").toggle();
           		 $("#hideDataModelExamplesLabel").toggle();
                 $("#showDataModelExamplesLabel").toggle();
            	 
                 e.preventDefault();
            	 return false;
            })
            <#if execute>
                execute();
            </#if>
        });
    </script>
    
    <title>Online FreeMarker Template Tester</title>
</head>
<body>
<div id="layout">
    <div id="main">
        <div class="header">
            <h1>Online FreeMarker Template Tester</h1>
        </div>

        <div class="content">
            <!--[if lte IE 8]>
            <div style="background-color: #C00; color: #fff; padding: 12px 24px;">
              You seem to use Internet Explorer 8 or older. This page might won't work properly with that.
            </div>
            <![endif]-->
          
            <form id="templateAndModelForm" method="post" class="pure-form pure-form-stacked">
                <label for="template">Template <span class="faint">(Apache FreeMarker ${freeMarkerVersion})</span></label>
                <textarea id="template" name="template" class="pure-input-1 source-code"
                        placeholder="Enter template, like: Hello ${r'${user}'}!"
                >${template}</textarea>
    
                <label for="template">
                    Data model
                    (<a id="showHideDataModelExamples" href="#" tabindex="-1"><!--
                    --><span id="showDataModelExamplesLabel">show</span><!--
                    --><span id="hideDataModelExamplesLabel" class="hiddenByDefault">hide</span>
                    examples</a>)
                </label>
                <div id="dataModelExamples" class="hiddenByDefault">
                  <div class="description">
                      Note: This syntax is specific to this online service; normally, you just have Java objects as
                      data-model.
                  </div>
                  <pre>someString = Some value
otherString = "JSON\nsyntax"
someNumber = 3.14
someBoolean = true
someDate = 2014-02-28
someTime = 20:50:30.5+02:00
someDatetime = 2014-02-28T18:50Z
someList = ["JSON", "syntax", 1, 2, 3 ]
someMap = { "JSON syntax": true, "nestedList": [1, 2, 3] }
someXML = &lt;example x="1"&gt;text&lt;/example&gt;</pre></div>
                <textarea id="dataModel" name="dataModel" class="pure-input-1 source-code"
                        placeholder='Enter one or more assignments (e.g., user = John Doe), starting each in its own line.'
                >${dataModel}</textarea>
                <div class="formPanel">
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Output format" name="outputFormat" selectionOptions=outputFormats
                        helpLink='https://freemarker.apache.org/docs/dgui_misc_autoescaping.html'
                        helpHover='Influences auto-escaping' />
                  </div>
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Locale" name="locale" selectionOptions=locales
                    	helpHover='Influences how numbers and date/time values are formatted' />
                  </div>
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Time zone" name="timeZone" selectionOptions=timeZones
                    	helpHover='Date/time values are shown as seen from this time zone' />
                  </div>
                </div>
                <div class="formPanel">
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Tag syntax" name="tagSyntax" selectionOptions=tagSyntaxes
                        helpLink='https://freemarker.apache.org/docs/dgui_misc_alternativesyntax.html#dgui_misc_alternativesyntax_tag' />
                  </div>
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Interpolation syntax" name="interpolationSyntax" selectionOptions=interpolationSyntaxes
                        helpLink='https://freemarker.apache.org/docs/dgui_misc_alternativesyntax.html#dgui_misc_alternativesyntax_interpolation'  />
                  </div>
                </div>
                <div class="formBottomButtonsContainer">
	                <input id="eval-btn" type="button" value="Evaluate" class="pure-button pure-button-primary"/>
	                &nbsp; <span class="faint">Ctrl+Enter in input fields will submit this form too</span>
                </div>
                <div style="display:none" class="resultContainer">
                    <label for="result">Result</label>
                    <textarea id="result" class="pure-input-1 source-code" readonly></textarea>
                </div>

            </form>
        </div><!-- content -->
        
        <div class="footer">
            FreeMarker documentation:
            <a href="https://freemarker.apache.org/docs/" target="_blank">Contents</a>
            |
            <a href="https://freemarker.apache.org/docs/dgui_template_overallstructure.html" target="_blank">Overall&nbsp;syntax</a>
            |
            <a href="https://freemarker.apache.org/docs/dgui_template_exp.html#exp_cheatsheet" target="_blank">Expression&nbsp;syntax</a>
            |
            <a href="https://freemarker.apache.org/docs/ref_directive_alphaidx.html" target="_blank">List&nbsp;of&nbsp;&lt;#<i>directives</i>&gt;</a>
            |
            <a href="https://freemarker.apache.org/docs/ref_builtins_alphaidx.html" target="_blank">List&nbsp;of&nbsp;<tt>?<i>built_in</i></tt> functions</a>
        </div><!-- footer -->
    </div><!-- main -->
    
    <!-- Fork me on GitHub: -->
    <a href="https://github.com/apache/incubator-freemarker-online-tester" target="_blank">
        <img class="forkMeOnGithub" src="https://camo.githubusercontent.com/a6677b08c955af8400f44c6298f40e7d19cc5b2d/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677261795f3664366436642e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png">
    </a>
</div><!-- layout -->
</body>
</html>