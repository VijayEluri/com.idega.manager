/*
 * $Id: DependencyMatrix.java,v 1.10 2006/04/09 11:42:59 laddi Exp $
 * Created on Nov 26, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.manager.data.Dependency;
import com.idega.manager.data.Module;
import com.idega.manager.data.Pom;
import com.idega.manager.util.VersionComparator;
import com.idega.util.datastructures.HashMatrix;


/**
 * 
 *  Last modified: $Date: 2006/04/09 11:42:59 $ by $Author: laddi $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.10 $
 */
public class DependencyMatrix {
	
	private IWResourceBundle resourceBundle = null;
	
	private List errorMessages = null;
	
	private HashMatrix moduleDependencies = null;
	
	private Collection notInstalledModules = null;
	private Collection installedModules = null;
	
	private List tempToBeInstalledModules = null;
	private List tempNecessaryModules = null;
	
	
	public static DependencyMatrix getInstance(Collection notInstalledModules, Collection installedModules, IWResourceBundle resourceBundle) {
		DependencyMatrix dependencyMatrix = new DependencyMatrix();
		dependencyMatrix.notInstalledModules = notInstalledModules;
		dependencyMatrix.installedModules = installedModules;
		dependencyMatrix.resourceBundle = resourceBundle;
		return dependencyMatrix;
	}
	
	public List getListOfNecessaryModules(VersionComparator versionComparator) {
		Collection tempNotInstalled = this.notInstalledModules;
		Collection tempInstalled = this.installedModules;
		this.tempToBeInstalledModules = null;
		this.tempNecessaryModules = null;
		boolean go = true;
		// this loop removes modules that are obsolete to be installed 
		// (e.g. another modules demands a newer version, the older version must not to be installed) 
		while (go) {
			initializeMatrix(tempNotInstalled, tempInstalled);
			try {
				tryCalculateListOfModulesToBeInstalled(versionComparator);
			}
			catch (IOException ex) {
				String errorMessage = this.resourceBundle.getLocalizedString("man_manager_could_not_get_dependencies","Could not figure out dependencies" + ex.getMessage());
				addErrorMessage(errorMessage);
				return this.tempNecessaryModules;
			}
			go = tempNotInstalled.retainAll(this.tempToBeInstalledModules);
		}
		return this.tempNecessaryModules;
	}
			
	public boolean hasErrors() {
		return ! (this.errorMessages == null || this.errorMessages.isEmpty());
	}
	
	public List getErrorMessages() {
		return this.errorMessages;
	}
	
	private void tryCalculateListOfModulesToBeInstalled(VersionComparator versionComparator) throws IOException {
		this.tempToBeInstalledModules = new ArrayList();
		this.tempNecessaryModules = new ArrayList();
		// get a list of required modules
		Iterator iterator = this.moduleDependencies.firstKeySet().iterator();
		while (iterator.hasNext()) {
			// x (also key) = dependencyKey
			String key = (String) iterator.next();
			Map map = this.moduleDependencies.get(key);
			Iterator iteratorMap = map.keySet().iterator();
			Module toBeInstalled = null;
			while (iteratorMap.hasNext()) {
				// y (also innerKey) = dependantKey
				String innerKey = (String) iteratorMap.next();
				Module module = (Module) map.get(innerKey);
				if (toBeInstalled == null || (module.compare(toBeInstalled, versionComparator) > 0)) {
					toBeInstalled = module;
				}
			}
			// install only modules that are not installed and not included in other modules
			if (! (toBeInstalled.isInstalled() || toBeInstalled.isIncluded())) {
				this.tempToBeInstalledModules.add(toBeInstalled);
			}
			this.tempNecessaryModules.add(toBeInstalled);	
		}
	}
	
	// e.g. returns "bundles_com.idega.block.article_installed"
	// e.g. returns "bundles_com.idega.block.article"
	private StringBuffer getKeyForDependant(Module module)	{
		StringBuffer buffer = getKeyForDependency(module);
		if (module.isInstalled()) {
			buffer.append("_installed");
		}
		return buffer;
	}
	
	// e.g. returns "bundles_com.idega.block.article"
	private StringBuffer getKeyForDependency(Module module) {
		String groupId = module.getGroupId();
		String artifactId = module.getArtifactId();
		StringBuffer buffer = new StringBuffer(groupId);
		buffer.append("_").append(artifactId);
		return buffer;
	}
	
	private HashMatrix getModuleDependencies() {
		if (this.moduleDependencies == null) {
			this.moduleDependencies = new HashMatrix();
		}
		return this.moduleDependencies;
	}
	
	private void initializeMatrix(Collection tempNotInstalledModules, Collection tempInstalledModules) {
		this.errorMessages = null;
		addEntries(tempInstalledModules);
		addEntries(tempNotInstalledModules);
	}
	
	private void addEntries(Collection poms) {
		Iterator iterator = poms.iterator();
		while (iterator.hasNext()) {
			Pom pom = (Pom) iterator.next();
			addEntry(pom);
		}
	}
	
	private void addEntry(Pom pom) {
		HashMatrix matrix = getModuleDependencies();
		addEntry(pom, pom, matrix);
	}
	
	private void addEntry(Pom dependant, Pom source, HashMatrix matrix) {
		// e.g. dependantKey "bundles_com.idega.block.article_installed"
		// e.g. dependantKey "bundles_com.idega.block.article"
		String dependantKey = getKeyForDependant(dependant).toString();
		// e.g. dependencyKeyForDependant "bundles_com.idega.block.article"
		String dependencyKeyForDependant = getKeyForDependency(dependant).toString();
		// x = dependencyKey, y = dependantKey
		this.moduleDependencies.put(dependencyKeyForDependant, dependantKey, dependant);
		List dependencies = null;
		try {
			 dependencies = source.getDependencies();
		}
		catch (IOException ex) {
			String errorMessage = this.resourceBundle.getLocalizedString("man_manager_could_not_get_dependencies","Could not figure out dependencies of ") + source.getArtifactId();
			addErrorMessage(errorMessage);
			return;
		}
		Iterator iterator = dependencies.iterator();
		while (iterator.hasNext()) {
			Dependency dependency = (Dependency) iterator.next();
			String dependencyKey = getKeyForDependency(dependency).toString();
			this.moduleDependencies.put(dependencyKey, dependantKey, dependency);
			Pom dependencyPom = null;
			try {
				dependencyPom  = dependency.getPom();
			}
			catch (IOException ex) {
				String errorMessage = this.resourceBundle.getLocalizedString("man_manager_could_not_get_dependencies","Could not figure out dependencies of ") + dependency.getArtifactId();
				addErrorMessage(errorMessage);

			}
			if (dependencyPom != null) {
				// go further
				addEntry(dependant, dependencyPom, matrix);					
			}
		}
	}
	
	private void addErrorMessage(String errorMessage) {
		if (this.errorMessages == null) {
			this.errorMessages = new ArrayList();
		}
		this.errorMessages.add(errorMessage);
	}
	
	

}