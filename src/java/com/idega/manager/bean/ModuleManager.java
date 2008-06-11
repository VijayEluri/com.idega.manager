/*
 * $Id: ModuleManager.java,v 1.21 2008/06/11 21:10:01 tryggvil Exp $
 * Created on Nov 10, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.manager.maven1.business.ApplicationUpdater;
import com.idega.manager.maven1.business.PomSorter;
import com.idega.manager.maven1.data.Module;
import com.idega.manager.maven1.util.ManagerConstants;
import com.idega.manager.maven1.util.ManagerUtils;
import com.idega.util.datastructures.SortedByValueMap;


/**
 * 
 *  Last modified: $Date: 2008/06/11 21:10:01 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.21 $
 */
public class ModuleManager {
	
	private static  int maxNumberOfShownErrorMessages = 5;
	
	private IWResourceBundle resourceBundle = null;
	private PomSorter pomSorter = null;
	
	private String outputText1Value;
	private String outputText2Value;
	private String button1Label;
	private String button2Label;
	private String button3Label;
	
	private String actionBack = ManagerConstants.ACTION_BACK_UPDATE;
	private String actionNext = "";
	private String actionNextChangeToNewValue = null;
	
	public ModuleManager() {
		initialize();
	}
	
	private void initialize() {
		this.resourceBundle = ManagerUtils.getInstanceForCurrentContext().getResourceBundle();
		initializePomSorter();
		initializeOutputText();
		initializeSubmitButtons();
	}
	
	private void initializePomSorter() {
		if (this.pomSorter == null) {
			this.pomSorter = ManagerUtils.getPomSorter();
		}
	}	
	
	private void initializeOutputText() {
		this.outputText1Value = this.resourceBundle.getLocalizedString("man_manager_header", "Module Manager");
		this.outputText2Value = this.resourceBundle.getLocalizedString("man_manager_do_you_want_to_install_modules","Do you want to install the following modules?");
	}

	private void initializeSubmitButtons() {
		this.button1Label = this.resourceBundle.getLocalizedString("man_manager_back","Back");
		this.button2Label = this.resourceBundle.getLocalizedString("man_manager_next","Install");
		this.button3Label = this.resourceBundle.getLocalizedString("man_manager_cancel","Cancel");
	}
	
	private void initializeDataTable1() {
		String noPreviousVersionInstalled = this.resourceBundle.getLocalizedString("man_manager_no_previous_version_installed","No previous version installed");
		List rows = new ArrayList();
		SortedMap toBeInstalled = null;
		SortedMap sortedInstalledMap = null;
		if (this.pomSorter != null) {
			toBeInstalled = this.pomSorter.getToBeInstalledPoms();
			sortedInstalledMap =this.pomSorter.getSortedInstalledPoms();
		}
		if (toBeInstalled == null || toBeInstalled.isEmpty()) {
			String noModulesNeedToBeInstalled = this.resourceBundle.getLocalizedString("man_manager_no_modules_need_to_be_installed","No modules need to be installed");
			String[] firstRow = { noModulesNeedToBeInstalled };
			rows.add(firstRow);
		}
		else {
			Map tableRows = new HashMap();
			Iterator iterator = toBeInstalled.values().iterator();
			while (iterator.hasNext()) {
				Module module = (Module) iterator.next();
				String name = module.getNameForLabel(this.resourceBundle);
				String version = module.getCurrentVersionForLabel(this.resourceBundle);
				String artifactId = module.getArtifactId();
				Module oldPom = (Module) sortedInstalledMap.get(artifactId);
				String oldVersion = (oldPom == null) ? noPreviousVersionInstalled : oldPom.getCurrentVersionForLabel(this.resourceBundle);
				String[] row = {name, version, oldVersion};
				tableRows.put(row, name);
			}
			Locale locale = this.resourceBundle.getLocale();
			SortedByValueMap sortedMap = new SortedByValueMap(tableRows, locale);
			Iterator valueIterator = sortedMap.keySet().iterator();
			while (valueIterator.hasNext()) {
				String[] row = (String[]) valueIterator.next();
				rows.add(row);	
			}
		}			
		this.dataTable1Model = new ListDataModel(rows);
		// initialize columnNames
		String module = this.resourceBundle.getLocalizedString("man_manager_module", "Module");
		String version = this.resourceBundle.getLocalizedString("man_manager_module", "New Version");
		String oldVersion = this.resourceBundle.getLocalizedString("man_manager_old_version","Old version");
		String[] columnNames = {module, version, oldVersion};
		initializeHtmlDataTable(columnNames);
	}	

	private void initializeErrorMessages() {
		List errorMessages = this.pomSorter.getErrorMessages();
		initializeErrorMessages(errorMessages);
	}
		
	private void initializeErrorMessages(List errorMessages) {
		HtmlPanelGroup group = getGroupPanel1();
		List list = group.getChildren();
		list.clear();
		this.button2.setDisabled(false);
		this.button2Label = this.resourceBundle.getLocalizedString("man_manager_next","Install");
		this.outputText2Value = this.resourceBundle.getLocalizedString("man_manager_do_you_want_to_install_modules","Do you want to install the following modules?");
		if (errorMessages != null) {
			this.outputText2Value = this.resourceBundle.getLocalizedString("man_manager_success","Problems occurred, you can not proceed");
			this.button2.setDisabled(true);
			Iterator iterator = errorMessages.iterator();
			boolean go = true;
			int i = 0;
			while (iterator.hasNext() && go) {
				String errorMessage = null;
				if (i++ == maxNumberOfShownErrorMessages) {
					go = false;
					errorMessage = this.resourceBundle.getLocalizedString("man_manager_more_problems", "more problems...");
				}
				else {
					errorMessage = (String) iterator.next();
				}
				errorMessage = errorMessage + " <br/>";
				HtmlOutputText error = new HtmlOutputText();
				error.setValue(errorMessage);
				error.setStyle("color: red");
				error.setEscape(false);
				list.add(error);
			}
		}
	}
	
	public void initializeDynamicContent() {
		initializeDataTable1();
		initializeErrorMessages();
	}
	
    private HtmlDataTable dataTable1 = new HtmlDataTable();

    public HtmlDataTable getDataTable1() {
    	return this.dataTable1;
    }

    public void setDataTable1(HtmlDataTable dataTable1) {
    	this.dataTable1 = dataTable1;
    }
    	
    private void  initializeHtmlDataTable(String[] columnNames) {
    	Application application = ManagerUtils.getInstanceForCurrentContext().getApplication();
    	try {
    		// First we remove columns from table
    		List list = this.dataTable1.getChildren();
    		list.clear();
//    		// This is data obtained in "executeQuery" method
//    		ResultSet resultSet = (ResultSet)data.getWrappedData();
//     
//    		columnNames = new String[colNumber];
    		UIColumn column;
    		HtmlOutputText outText;
    		HtmlOutputText headerText;
//     
//    		ResultSetMetaData metaData = resultSet.getMetaData();
    		this.dataTable1.setVar("currentRow");
    		this.dataTable1.setColumnClasses("moduleManagerBigColumnClass, moduleManagerColumnClass, moduleManagerColumnClass");
//			dataTable1.setHeaderClass("moduleManagerHeaderClass");
    		
    		// "currentRow" must be set in the corresponding JSP page!
    		// this variable is used above by the method call dataTable1.getVar()
     
    		// In this loop we are going to add columns to table,
    		// and to make sure that all column are populated (table.getVar is taking care of that).
    		// As you can se we will add "headerText" and "outText" to "column",
    		// same way we would add "<f:facet name="header">" and "<h:outputtext>" to "<h:column>"
    		// Notice that "var" is previously set to "dbTable" and now we are using it.
    		for (int i = 0; i < 3; i++) {
//    			columnNames[i] = metaData.getColumnName(i + 1);
//     
    			headerText = new HtmlOutputText();
    			headerText.setTitle(columnNames[i]);
    			headerText.setValue(columnNames[i]);
    			//headerText.setStyleClass("moduleManagerHeaderClass");
     
    			outText = new HtmlOutputText();
     
    			//String vblExpression = 	"#{" + hdt.getVar() + "." + metaData.getColumnName(i + 1) + "}";
    			String vblExpression = "#{" + this.dataTable1.getVar() + "[" + Integer.toString(i) + "]}";
    			ValueBinding vb = application.createValueBinding(vblExpression);
    			outText.setValueBinding("value", vb);
     
    			column = new UIColumn();


    			//map.put("width","444px");
     
    			column.getChildren().add(outText);
    			column.setHeader(headerText);
    			// ... we add column to list of table's components
    			list.add(column);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	public void submitForm(ActionEvent event) {
		if (this.pomSorter != null) {
			ApplicationUpdater updater = new ApplicationUpdater(this.pomSorter);
			if (! updater.installModules()) { 
				String errorMessage = updater.getErrorMessage();
				List errorMessages = new ArrayList(1);
				errorMessages.add(errorMessage);
				initializeErrorMessages(errorMessages);
			}
			else {
				this.outputText2Value = this.resourceBundle.getLocalizedString("man_manager_success","Modules have been successfully installed");
				this.button1.setDisabled(true);
				this.button2.setDisabled(true);
				this.button3.setDisabled(true);
				this.pomSorter = null;
//				button2Label = resourceBundle.getLocalizedString("man_manager_finish","Finish");
//				actionNextChangeToNewValue = ManagerConstants.ACTION_CANCEL;
			}
		}
	}
    
    
    private ListDataModel dataTable1Model = new ListDataModel();

    public ListDataModel getDataTable1Model() {
        return this.dataTable1Model;
    }

    public void setDataTable1Model(ListDataModel dtdm) {
        this.dataTable1Model = dtdm;
    }

    private HtmlCommandButton button1 = new HtmlCommandButton();

    public HtmlCommandButton getButton1() {
        return this.button1;
    }

    public void setButton1(HtmlCommandButton hcb) {
        this.button1 = hcb;
    }

    private HtmlCommandButton button2 = new HtmlCommandButton();

    public HtmlCommandButton getButton2() {
        return this.button2;
    }

    public void setButton2(HtmlCommandButton hcb) {
        this.button2 = hcb;
    }

    private HtmlCommandButton button3 = new HtmlCommandButton();

    public HtmlCommandButton getButton3() {
        return this.button3;
    }

    public void setButton3(HtmlCommandButton hcb) {
        this.button3 = hcb;
    }    
    
    private HtmlOutputText outputText1 = new HtmlOutputText();

    public HtmlOutputText getOutputText1() {
        return this.outputText1;
    }

    public void setOutputText1(HtmlOutputText hot) {
        this.outputText1 = hot;
    }

    private HtmlOutputText outputText2 = new HtmlOutputText();

    public HtmlOutputText getOutputText2() {
        return this.outputText2;
    }

    public void setOutputText2(HtmlOutputText hot) {
        this.outputText2 = hot;
    }
    
    public String getOutputText1Value() {
    	return this.outputText1Value;
    }
 
    public String getOutputText2Value() {
    	return this.outputText2Value;
    }    
    
    public String getButton1Label() {
    	return this.button1Label;
    }
    
    public String getButton2Label() {
    	return this.button2Label;
    }
    public String getButton3Label() {
    	return this.button3Label;
    }
    
    public void setActionBack(String actionBack) {
    	this.actionBack = actionBack;
    }
    
    public String button1_action() {
    	return this.actionBack;
    }
    
    // first submitForm is called then this method is invoked 
    // change the value after returning the old value!
    public String button2_action() {
		String returnValue = this.actionNext;
    	if (this.actionNextChangeToNewValue != null) {
    		this.actionNext = this.actionNextChangeToNewValue;
    		this.actionNextChangeToNewValue = null;
    	}
    	return returnValue;
    }    
    
    
    public String button3_action() {
    	return ManagerConstants.ACTION_CANCEL;
    }
    
    private HtmlPanelGroup groupPanel1 = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanel1() {
        return this.groupPanel1;
    }

    public void setGroupPanel1(HtmlPanelGroup hpg) {
        this.groupPanel1 = hpg;
    }
}
