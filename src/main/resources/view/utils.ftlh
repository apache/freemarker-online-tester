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

<#macro htmlSelect caption name selectionOptions helpLink='' helpHover=''>
  <div <@titleAttr helpHover />>${caption}<#if helpLink != ''> (<a href='${helpLink}' target='_blank'>?</a>)</#if></div>
  <div> 
    <select name="${name}" id="${name}" class="pure-input-1" <@titleAttr helpHover />>
      <#list selectionOptions as selectionOption>
        <#local value = selectionOption.value>
        <option value="${value}"<#if value == .vars[name]!> selected</#if>>${selectionOption.label}</option>
      </#list>
    </select>
  </div>
</#macro>

<#macro titleAttr title><#if title != ''>title="${title}"</#if></#macro>