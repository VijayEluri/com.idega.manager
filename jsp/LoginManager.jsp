<jsp:root version="2.1" 
	xmlns:f="http://java.sun.com/jsf/core" 
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:t="http://myfaces.apache.org/tomahawk"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
	xmlns:wf="http://xmlns.idega.com/com.idega.webface">
    <jsp:directive.page contentType="text/html" pageEncoding="UTF-8"/>
    <f:view>
		<ws:page stylesheeturls="/idegaweb/bundles/com.idega.manager.bundle/resources/style/manager.css">
        	<h:form id="loginManagerForm1">
            <wf:wfblock title="#{localizedStrings['com.idega.manager']['install_update']}">
                
              <f:facet name="header">
              	<wf:container styleClass="wizardheader">

	                <wf:container styleClass="name">
	                    <h:outputText binding="#{LoginManager.outputText2}" 
	                    	id="outputText2" 
	                    	value="#{LoginManager.outputText2Value}"/>
					</wf:container>
				
                
	                <wf:container styleClass="description">
	                    <h:outputText binding="#{LoginManager.outputText1}" id="outputText1" value="#{LoginManager.outputText1Value}"/>
	                </wf:container>

				</wf:container>
			  </f:facet>
				
				<h:messages styleClass="wf_messages"
                    	showSummary="false" 
                    	showDetail="true"
            			id="messages1"/>
				
				<wf:container styleClass="formitem" >
                    <h:outputLabel for="textField1" 
                    	styleClass="managerLogin"
                    	binding="#{LoginManager.outputText3}" 
                    	id="outputText3" 
                    	value="#{LoginManager.outputText3Value}"/>
                   	<h:inputText binding="#{LoginManager.textField1}" 
                   		style="width: 300px"
                   		id="textField1"/>
				</wf:container>
				
				<wf:container styleClass="formitem">
                   	<h:outputLabel for="textField2" 
                   		styleClass="managerLogin"
                   		binding="#{LoginManager.outputText4}" 
                   		id="outputText4" 
                   		value="#{LoginManager.outputText4Value}"/>
                   	<h:inputText binding="#{LoginManager.textField2}" 
                   		style="width: 300px"
                   		id="textField2"/>
				</wf:container>
				
				<wf:container styleClass="formitem">
                   	<h:outputLabel for="secretField1" 
                   		styleClass="managerLogin"
                   		binding="#{LoginManager.outputText5}" 
                   		id="outputText5" 
                   		value="#{LoginManager.outputText5Value}"/>
                   	<t:inputSecret binding="#{LoginManager.secretField1}" 
                   		style="width: 300px"
                   		id="secretField1"
                   		forceId="true"/>
				</wf:container>
				
	            	<h:inputHidden value="noSelection" validator="#{LoginManager.validateUserPassword}"/>
              
              
              <f:facet name="footer">
              	<wf:container styleClass="buttons">
                   	<h:commandButton binding="#{LoginManager.button1}" id="button1"  
                   		disabled="true"
                    	value="#{LoginManager.button1Label}"/>
                    	
                    <h:commandButton binding="#{LoginManager.button2}" id="button2" 
                    	action="#{LoginManager.button2_action}"
                    	actionListener="#{LoginManager.submitForm}"
                    	value="#{LoginManager.button2Label}"/>
                    	
                    <h:commandButton binding="#{LoginManager.button3}" id="button3" 
                    	immediate="true"
                    	action="#{LoginManager.button3_action}"
                    	value="#{LoginManager.button3Label}"/>
                	</wf:container>
                </f:facet>
            </wf:wfblock>
            </h:form>
		</ws:page>
    </f:view>
</jsp:root>
