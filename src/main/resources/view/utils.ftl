<#ftl outputFormat='HTML'>

<#macro htmlSelect caption name selectionOptions>
  <div>${caption}:</div>
  <div> 
    <select name="${name}" id="${name}" class="pure-input-1">
      <#list selectionOptions as selectionOption>
        <#local value = selectionOption.value>
        <option value="${value}"<#if value == .vars[name]!> selected</#if>>${selectionOption.label}</option>
      </#list>
    </select>
  </div>
</#macro>