/*
 * $Id: LocalBundlesBrowser.java,v 1.3 2005/01/07 11:03:35 thomas Exp $
 * Created on Nov 22, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.business;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.idega.manager.data.RealPom;
import com.idega.manager.util.IdegawebDirectoryStructure;
import com.idega.manager.util.ManagerUtils;
import com.idega.util.FileUtil;
import com.idega.util.StringHandler;


/**
 * 
 *  Last modified: $Date: 2005/01/07 11:03:35 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.3 $
 */
public class LocalBundlesBrowser {
	
	 /**
	  * Gets the default Logger. By default it uses the package and the class name to get the logger.<br>
	  * This behaviour can be overridden in subclasses.
	  * @return the default Logger
	  */
	static private Logger getLogger(){
		 return Logger.getLogger(ManagerUtils.class.getName());
	 }
	

	public Map getTagLibrariesOfInstalledModules() {
		IdegawebDirectoryStructure idegawebDirectoryStructure = ManagerUtils.getInstanceForCurrentContext().getIdegawebDirectoryStructure();
		File tempBundlesPath = idegawebDirectoryStructure.getBundlesRealPath();
		List bundles = FileUtil.getDirectoriesInDirectory(tempBundlesPath);
		if (bundles == null) {
			return null;
		}
		Map tagLibraries = new HashMap(bundles.size());
		Iterator bundlesIterator = bundles.iterator();
		while (bundlesIterator.hasNext()) {
			File bundleFolder = (File) bundlesIterator.next();
			File library = idegawebDirectoryStructure.getTagLibrary(bundleFolder);
			List tlds = null;
			if (library.exists()) {
				tlds = FileUtil.getDirectoriesInDirectory(library);
				if (tlds.isEmpty()) {
					tlds = null;
				}
			}
			String bundleName = bundleFolder.getName();
			// get rid of the bundle extension 
			String artifactId = StringHandler.cutExtension(bundleName);
			tagLibraries.put(artifactId, tlds);
		}
		return tagLibraries;
	}
				
	
	public List  getPomOfInstalledModules() {
		File tempBundlesPath = ManagerUtils.getInstanceForCurrentContext().getBundlesRealPath();
		List bundles = FileUtil.getDirectoriesInDirectory(tempBundlesPath);
		if (bundles == null) {
			return null;
		}
		List poms = new ArrayList(bundles.size());
		Iterator bundlesIterator = bundles.iterator();
		while (bundlesIterator.hasNext()) {
			File folder = (File) bundlesIterator.next();
			File projectFile = new File(folder, RealPom.POM_FILE);
			if (projectFile.exists()) {
				try {
					RealPom pom = RealPom.getInstalledPomOfGroupBundles(projectFile);
					poms.add(pom);
				}
				catch (IOException ex) {
					Logger logger = getLogger();
					logger.log(Level.WARNING, "[ManagerUtils] POM file for module "+folder.getName()+" could not be parsed");
				}
			}
		}
		return poms;
	}
}
