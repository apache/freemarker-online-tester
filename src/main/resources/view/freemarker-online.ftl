<#ftl outputFormat="HTML">
<#import "utils.ftl" as u>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link rel="stylesheet" href="css/main.css">
    <link rel="stylesheet" href="http://yui.yahooapis.com/pure/0.5.0/pure-min.css">
    
    <script src="https://code.jquery.com/jquery-1.11.2.min.js"></script>
    <script src="js/jquery.blockUI.js"></script>
    <script src="js/autosize.min.js"></script>
    <script src="js/script.js"></script>
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
                    <@u.htmlSelect caption="Output format" name="outputFormat" selectionOptions=outputFormats />
                  </div>
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Locale" name="locale" selectionOptions=locales />
                  </div>
                  <div class="horizontalBox">
                    <@u.htmlSelect caption="Time zone" name="timeZone" selectionOptions=timeZones />
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
            <a href="http://freemarker.org/docs/" target="_blank">Contents</a>
            |
            <a href="http://freemarker.org/docs/dgui_template_overallstructure.html" target="_blank">Overall&nbsp;syntax</a>
            |
            <a href="http://freemarker.org/docs/dgui_template_exp.html#exp_cheatsheet" target="_blank">Expression&nbsp;syntax</a>
            |
            <a href="http://freemarker.org/docs/ref_directive_alphaidx.html" target="_blank">List&nbsp;of&nbsp;&lt;#<i>directives</i>&gt;</a>
            |
            <a href="http://freemarker.org/docs/ref_builtins_alphaidx.html" target="_blank">List&nbsp;of&nbsp;<tt>?<i>built_in</i></tt> functions</a>
        </div><!-- footer -->
    </div><!-- main -->
    
    <!-- Fork me on GitHub: -->
    <a href="https://github.com/kenshoo/freemarker-online" target="_blank"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://camo.githubusercontent.com/a6677b08c955af8400f44c6298f40e7d19cc5b2d/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677261795f3664366436642e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"></a>
</div><!-- layout -->
</body>
</html>