/*
 * $Id: DependencyPomBundle.java,v 1.10 2005/03/18 14:16:36 thomas Exp $
 * Created on Dec 1, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.data;

import java.io.File;
import java.io.IOException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.manager.util.VersionComparator;


/**
 * 
 *  Last modified: $Date: 2005/03/18 14:16:36 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.10 $
 */
public class DependencyPomBundle extends Dependency {
	
	public static final String GROUP_ID_BUNDLE = "bundles";
	
	Boolean isSnapshot = null;
	
	Pom pom = null;
	
	File bundleArchive = null;
	
	DependencyPomBundle() {
		// use the class method of Dependency
	}
	
	public Pom getPom() throws IOException {
		if (pom == null) {
			pom = getDependantPom().getPom(this);
		}
		return pom;
	}
	
	public File getBundleArchive() throws IOException {
		if (bundleArchive == null) {
			bundleArchive = getDependantPom().getBundleArchive(this);
		}
		return bundleArchive;
	}
	
	
	public boolean isIncluded() {
		return false;
	}
	
	public int compare(Module module, VersionComparator versionComparator) throws IOException {
		// change algebraic sign of returned result
		return - (module.compare(this, versionComparator));
	}
	
	public int compare(Dependency dependency, VersionComparator versionComparator) {
		// not supported, it has never the same group id
		return -1;
	}
	
	public int compare(DependencyPomBundle dependencyPomBundle, VersionComparator versionComparator) throws IOException {
		// Case 1:  both are snapshots 
		if (isSnapshot() && dependencyPomBundle.isSnapshot()) {
			// compare timestamps
			Pom tempPom = dependencyPomBundle.getPom();   
			return compare(tempPom, versionComparator);
		}
		return Pom.compareModules(this, dependencyPomBundle, versionComparator);
	}
		
	public int compare(Pom aPom, VersionComparator versionComparator) throws IOException { 
		// Case 1:  both are snapshots 
		if (isSnapshot() && aPom.isSnapshot()) {
			// compare timestamps
			Pom tempPom = getPom();   
			return tempPom.compare(aPom, versionComparator);
		}
		return Pom.compareModules(this, aPom, versionComparator);
	}
	
	public boolean isSnapshot() {
		if (isSnapshot == null) {
			String tempVersion = getCurrentVersion();
			isSnapshot = new Boolean(RealPom.isSnapshot(tempVersion));
		}
		return isSnapshot.booleanValue();
	}

	public String getCurrentVersionForLabel(IWResourceBundle resourceBundle) {
		Pom tempPom = null;
		try {
			tempPom = getPom();
		}
		catch (IOException ex) {
			String problem = resourceBundle.getLocalizedString("man_manager_could_not_ figure_out_version","Could not figure out version");
			StringBuffer buffer = new StringBuffer();
			buffer.append(problem);
			buffer.append(" ");
			buffer.append(getCurrentVersion());
			return buffer.toString();
		}
		return tempPom.getCurrentVersionForLabel(resourceBundle);
	}
	
	public String getNameForLabel(IWResourceBundle resourceBundle) {
		Pom tempPom = null;
		try {
			tempPom = getPom();
		}
		catch (IOException ex) {
			String problem = resourceBundle.getLocalizedString("man_manager_could_not_ figure_out_name_for","Could not figure out name for");
			StringBuffer buffer = new StringBuffer();
			buffer.append(problem);
			buffer.append(" ");
			buffer.append(getArtifactId());
			buffer.append(" (");
			buffer.append(getGroupId());
			buffer.append(")");
			return buffer.toString();
		}
		return tempPom.getNameForLabel(resourceBundle);
	}

}
