/*
 * $Id: UpdateListManager.java,v 1.18 2006/04/09 11:42:59 laddi Exp $
 * Created on Nov 10, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.manager.business.DependencyMatrix;
import com.idega.manager.business.PomSorter;
import com.idega.manager.business.PomValidator;
import com.idega.manager.data.Module;
import com.idega.manager.data.Pom;
import com.idega.manager.data.ProxyPom;
import com.idega.manager.data.RepositoryLogin;
import com.idega.manager.util.ManagerConstants;
import com.idega.manager.util.ManagerUtils;
import com.idega.manager.util.VersionComparator;
import com.idega.util.datastructures.SortedByValueMap;


/**
 * 
 *  Last modified: $Date: 2006/04/09 11:42:59 $ by $Author: laddi $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.18 $
 */
public class UpdateListManager {
	
	protected IWResourceBundle resourceBundle;
	protected PomValidator pomValidator = null;
	protected PomSorter pomSorter = null;
	
	protected String outputText1Value;
	protected String outputText2Value;
	protected String button1Label;
	protected String button2Label;
	protected String button3Label;
	
	public UpdateListManager() {
		initialize();
	}
	
	protected void initialize() {
		this.resourceBundle = ManagerUtils.getInstanceForCurrentContext().getResourceBundle();
		initializePomSorter();
		initializeOutputText();
		initializeSubmitButtons();
	}

	protected void initializePomSorter() {
		if (this.pomSorter == null) {
			this.pomSorter = ManagerUtils.getPomSorter();
		}
	}
	
	protected void initializeOutputText() {
		this.outputText1Value = this.resourceBundle.getLocalizedString("man_manager_header", "Manager");
		this.outputText2Value = this.resourceBundle.getLocalizedString("man_manager_select _updates","Select updates");
	}

	protected void initializeSubmitButtons() {
		this.button1Label = this.resourceBundle.getLocalizedString("man_manager_back","Back");
		this.button2Label = this.resourceBundle.getLocalizedString("man_manager_next","Next");
		this.button3Label = this.resourceBundle.getLocalizedString("man_manager_cancel","Cancel");
	}
	
	protected void initializeList() {
		this.multiSelectListbox1DefaultItems = new ArrayList();
		String errorMessage = null;
		try {
			RepositoryLogin repositoryLogin = ManagerUtils.getRepositoryLogin();
			this.pomSorter.initializeInstalledPomsAndAvailableUpdates(repositoryLogin);
		}
		catch (IOException ex) {
			errorMessage = this.resourceBundle.getLocalizedString("man_manager_no_connection", "Problems connecting to remote repository occurred");
		}
		HtmlPanelGroup group = getGroupPanel1();
		List list = group.getChildren();
		list.clear();
		this.button2.setDisabled(false);
		if (errorMessage != null) {
			this.button2.setDisabled(true);
			errorMessage = errorMessage + " <br/>";
			HtmlOutputText error = new HtmlOutputText();
			error.setValue(errorMessage);
			error.setStyle("color: red");
			error.setEscape(false);
			list.add(error);		
			return;
		}
	 	
		SortedMap sortedInstalledPom = this.pomSorter.getSortedInstalledPoms();
		Map repositoryPom = this.pomSorter.getSortedRepositoryPomsOfAvailableUpdates();
		fillList(sortedInstalledPom.keySet(), repositoryPom, sortedInstalledPom);
	}
		
	protected void fillList(Collection	keys, Map repositoryPom, Map groupItemMap) {
		Map listItems = new HashMap();
		Iterator iterator = keys.iterator();
		while (iterator.hasNext()) {
		 	String artifactId = (String) iterator.next();
		 	SortedSet pomProxies = (SortedSet) repositoryPom.get(artifactId);
		 	SelectItem[] items = null;
		 	if (pomProxies != null) {
		 		Iterator pomProxiesIterator = pomProxies.iterator();
			 	Module pom = getModuleGroupItem(groupItemMap, artifactId);
		 		items = new SelectItem[pomProxies.size()];
			 	int i = items.length;
			 	while (pomProxiesIterator.hasNext()) {
			 		ProxyPom proxy = (ProxyPom) pomProxiesIterator.next();
			 		// file is used as identifier
			 		String fileName = proxy.getFileName();
			 		String label = getLabelForItem(pom, proxy);
			 		items[--i] = new SelectItem(fileName, label);
			 	}
			 	String label = getLabelForItemGroup(pom);
			 	SelectItemGroup itemGroup = new SelectItemGroup(label, null, true, items);
			 	listItems.put(itemGroup, label);
		 	}
		}
		Locale locale = this.resourceBundle.getLocale();
		SortedByValueMap sortedMap = new SortedByValueMap(listItems, locale);
		Iterator valueIterator = sortedMap.keySet().iterator();
		while (valueIterator.hasNext()) {
			SelectItemGroup selectItemGroup = (SelectItemGroup) valueIterator.next();
			this.multiSelectListbox1DefaultItems.add(selectItemGroup);		 	
		}
	}
	
	protected String getLabelForItemGroup(Module groupModule) {
	 	String pomName = groupModule.getNameForLabel(this.resourceBundle);
	 	String pomVersion = groupModule.getCurrentVersionForLabel(this.resourceBundle);
	 	StringBuffer buffer = new StringBuffer();
	 	buffer.append(pomName);
	 	buffer.append(", ");
	 	buffer.append(pomVersion);
	 	return buffer.toString();
	}
	
	
	protected String getLabelForItem(Module groupModule, Module itemModule) {
 		String label = itemModule.getCurrentVersionForLabel(this.resourceBundle);
		// if the installed version is a snapshot do not recommend to install a stable version and vice versa
 		if (groupModule.isSnapshot() ^ itemModule.isSnapshot()) {
 			String notRecommended = this.resourceBundle.getLocalizedString("man_manager_not_recommended", "not recommended");
 			StringBuffer buffer = new StringBuffer(label);
 			buffer.append(" - ").append(notRecommended);
 			return buffer.toString();
 		}
 		return label;
	}
	
	protected Module getModuleGroupItem(Map sortedInstalledPom, String artifactId) {
		return (Module) sortedInstalledPom.get(artifactId);
	}
	
	public void submitForm(ActionEvent event) {
		submitAndSetModuleManager(event, ManagerConstants.ACTION_BACK_UPDATE);
	}
		
	protected void submitAndSetModuleManager(ActionEvent event, String action) {
		UIComponent component = event.getComponent();
		UIComponent parentForm = component.getParent();
		HtmlSelectManyListbox selectManyList = (HtmlSelectManyListbox) parentForm.findComponent("multiSelectListbox1");
		Object[] selectedValues = selectManyList.getSelectedValues();
		Map repositoryPoms = this.pomSorter.getRepositoryPoms();
		Map selectedPoms = new HashMap();
		for (int i = 0; i < selectedValues.length; i++) {
			Pom pom = (Pom) repositoryPoms.get(selectedValues[i]);
			String artifactId = pom.getArtifactId();
			selectedPoms.put(artifactId, pom);
		}
		Map installedPoms = this.pomSorter.getSortedInstalledPoms();
		Collection installedModules = installedPoms.values();
		Collection notInstalledModules = selectedPoms.values();
		VersionComparator versionComparator = this.pomSorter.getUsedVersionComparator();
		DependencyMatrix dependencyMatrix = DependencyMatrix.getInstance(notInstalledModules, installedModules, this.resourceBundle);
		List necessaryModules = dependencyMatrix.getListOfNecessaryModules(versionComparator);
		// it is important to reset the error messages - that is to set to null - if there are not any
		List errorMessages = dependencyMatrix.hasErrors() ? dependencyMatrix.getErrorMessages() : null;
		this.pomSorter.setErrorMessages(errorMessages);
		this.pomSorter.setNecessaryPoms(necessaryModules);
		ModuleManager moduleManager = ManagerUtils.getModuleManager();
		if (moduleManager != null) {
			moduleManager.initializeDynamicContent();
			moduleManager.setActionBack(action);
		}
	}
 
	
	public void validateSelectedModules(FacesContext context, UIComponent toValidate, Object value) {
		// the value of a hidden input is validated because only in this way this method is called even if nothing has been selected.
		// We could use the attribute "required" but this causes problems with the localization of the corresponding error message.
		// get the value of the component we are really interested in....
		UIComponent component = context.getViewRoot().findComponent(ManagerConstants.JSF_COMPONENT_ID_MULTI_SELECT_1);
		Object componentValue = ((UIInput) component).getValue();
		if (this.pomValidator == null) {
			this.pomValidator = new PomValidator();
		}
		this.pomValidator.validateSelectedModules(context, toValidate, componentValue, this.pomSorter , this.resourceBundle);
	}

	public void initializeDynamicContent() {
		initializeList();
	}	
	
    private HtmlSelectManyListbox multiSelectListbox1 = new HtmlSelectManyListbox();

    public HtmlSelectManyListbox getMultiSelectListbox1() {
        return this.multiSelectListbox1;
    }

    public void setMultiSelectListbox1(HtmlSelectManyListbox hsml) {
        this.multiSelectListbox1 = hsml;
    }

    protected List multiSelectListbox1DefaultItems = new ArrayList();

    public List getMultiSelectListbox1DefaultItems() {
        return this.multiSelectListbox1DefaultItems;
    }

    public void setMultiSelectListbox1DefaultItems(List dsia) {
        this.multiSelectListbox1DefaultItems = dsia;
    }

    private UISelectItems multiSelectListbox1SelectItems = new UISelectItems();

    public UISelectItems getMultiSelectListbox1SelectItems() {
        return this.multiSelectListbox1SelectItems;
    }

    public void setMultiSelectListbox1SelectItems(UISelectItems uisi) {
        this.multiSelectListbox1SelectItems = uisi;
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
    
    public String button1_action() {
    	return ManagerConstants.ACTION_BACK;
    }
    
    public String button2_action() {
    	return ManagerConstants.ACTION_NEXT;
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
