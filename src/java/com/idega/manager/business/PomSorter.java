/*
 * $Id: PomSorter.java,v 1.13 2005/03/16 17:49:41 thomas Exp $
 * Created on Nov 22, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.business;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import com.idega.manager.data.Module;
import com.idega.manager.data.Pom;
import com.idega.manager.data.ProxyPom;
import com.idega.manager.data.RealPom;
import com.idega.manager.data.RepositoryLogin;
import com.idega.manager.util.VersionComparator;


/**
 * 
 *  Last modified: $Date: 2005/03/16 17:49:41 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.13 $
 */
public class PomSorter {

	//key: artifactId String value: List of Files
	Map bundlesTagLibraries = null;
	
	// key: artifactId String value: Pom 
	SortedMap sortedInstalledPom = null;
	
	// key: artifactId String value (TreeSet of PomProxy) 
	Map sortedRepositoryPomAvailableUpdates = null;
	
	// key: artifactId String value (TreeSet of PomProxy) 
	SortedMap sortedRepositoryPomAvailableNewModules = null;
	
	// key: fileName String value: PomProxy
	Map fileNameRepositoryPom = null;
	
	// 
	SortedMap toBeInstalledPoms = null;
	
	Map necessaryPoms = null;
	
	List errorMessages = null;
	
	// version comparator works with caches, that is 
	// a reused comparator works faster when dealing with the same versions again
	VersionComparator usedVersionComparator = null;
	
	public void initializeInstalledPomsAndAvailableUpdates(RepositoryLogin repositoryLogin) throws IOException {
		VersionComparator versionComparator = getUsedVersionComparator();
		findInstalledPoms();
		findAvailableUpdates(repositoryLogin, versionComparator);
	}
	
	public void initializeInstalledPomsAndAvailableNewModules(RepositoryLogin repositoryLogin) throws IOException {
		VersionComparator versionComparator = getUsedVersionComparator();
		findInstalledPoms();
		findAvailableNewModules(repositoryLogin, versionComparator);
	}
		
		
		
	private void findInstalledPoms() {
		LocalBundlesBrowser localBrowser = new LocalBundlesBrowser();
		bundlesTagLibraries = localBrowser.getTagLibrariesOfInstalledModules();
		List installedPoms = localBrowser.getPomOfInstalledModules();
		sortedInstalledPom = new TreeMap();
		Iterator installedPomsIterator = installedPoms.iterator();
		while (installedPomsIterator.hasNext()) {
			RealPom pom = (RealPom) installedPomsIterator.next();
			String artifactId = pom.getArtifactId();
			sortedInstalledPom.put(artifactId, pom);
		}
	}
		
	private void findAvailableUpdates(RepositoryLogin repositoryLogin, VersionComparator versionComparator) throws IOException {
		//if (true) throw new IOException("test");
		RepositoryBrowser repositoryBrowser = RepositoryBrowser.getInstanceForIdegaRepository(repositoryLogin);
		List allPoms = repositoryBrowser.getPomsSynchronizingBundleArchivesFolderAndPomsFolder();
		sortedRepositoryPomAvailableUpdates = new HashMap();
		Iterator allPomsIterator = allPoms.iterator();
		while (allPomsIterator.hasNext()) {
			ProxyPom proxy = (ProxyPom) allPomsIterator.next();
			String artifactId = proxy.getArtifactId();
			if (sortedInstalledPom.containsKey(artifactId)) {
				RealPom pom = (RealPom) sortedInstalledPom.get(artifactId);
				// fetch only poms that are newer than the installed ones
				// and fetch additionally versions if the installed one is a snapshot
				if (proxy.compare(pom, versionComparator) > 0 || (pom.isSnapshot() && ! proxy.isSnapshot())) {
					putPom(artifactId, proxy, sortedRepositoryPomAvailableUpdates, versionComparator);
				}
			}
		}
	}
	
	
	private void findAvailableNewModules(RepositoryLogin repositoryLogin, VersionComparator versionComparator) throws IOException {
		//if (true) throw new IOException("test");
		RepositoryBrowser repositoryBrowser = RepositoryBrowser.getInstanceForIdegaRepository(repositoryLogin);
		List allPoms= repositoryBrowser.getPomsSynchronizingBundleArchivesFolderAndPomsFolder();
		sortedRepositoryPomAvailableNewModules = new TreeMap();
		Iterator allPomsIterator = allPoms.iterator();
		while (allPomsIterator.hasNext()) {
			ProxyPom proxy = (ProxyPom) allPomsIterator.next();
			String artifactId = proxy.getArtifactId();
			if (! sortedInstalledPom.containsKey(artifactId)) {
				putPom(artifactId, proxy, sortedRepositoryPomAvailableNewModules, versionComparator);
			}
		}
	
	}
	
	private void putPom(String key, ProxyPom value, Map pomMap, final VersionComparator versionComparator) {
		// first store in fileNameMap
		if (fileNameRepositoryPom == null) {
			fileNameRepositoryPom = new HashMap();
		}
		String fileName = value.getFileName();
		fileNameRepositoryPom.put(fileName, value);
		// second store in sorted map
		SortedSet pomSet = (SortedSet) pomMap.get(key);
		if (pomSet == null) {
			Comparator comparator = new Comparator() {
				
				public int compare(Object proxy1, Object proxy2) {
					Pom pom1 = (Pom) proxy1;
					Pom pom2 = (Pom) proxy2;
					return pom1.compare(pom2, versionComparator);
				}
			};
			 pomSet = new TreeSet(comparator);
			 pomMap.put(key, pomSet);
		}
		pomSet.add(value);
	}
		
	public Map getBundlesTagLibraries() {
		return bundlesTagLibraries;
	}
	
	public Map getRepositoryPoms() {
		return fileNameRepositoryPom;
	}
		
	public SortedMap getSortedInstalledPoms() {
		return sortedInstalledPom;
	}
	
	public Map getSortedRepositoryPomsOfAvailableUpdates() {
		return sortedRepositoryPomAvailableUpdates;
	}
	
	public Map getSortedRepositoryPomsOfAvailableNewModules() { 
		return sortedRepositoryPomAvailableNewModules;
	}
	
	
	public SortedMap getToBeInstalledPoms() {
		return toBeInstalledPoms;
	}
	
	public Map getNecessaryPoms() {
		return necessaryPoms;
	}
	
	public void setNecessaryPoms(List necessaryPoms) {
		this.toBeInstalledPoms = new TreeMap();
		this.necessaryPoms = new HashMap();
		Iterator iterator = necessaryPoms.iterator();
		while (iterator.hasNext()) {
			Module module = (Module) iterator.next();
			String key = module.getArtifactId();
			if (! (module.isInstalled() || module.isIncluded())) { 
				this.toBeInstalledPoms.put(key, module);
			}
			this.necessaryPoms.put(key, module);
		}
	}

	public List getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(List errorMessages) {
		this.errorMessages = errorMessages;
	}
	
	/**
	 * 
	 * <p>
	 * 	A version comparator works with a cache, that is a 
	*  	reused comparator works faster 
	*  when dealing with the same versions again
	 * </p>
	 * @return
	 */
	public VersionComparator getUsedVersionComparator() {
		if (usedVersionComparator == null) {
			usedVersionComparator = new VersionComparator();
		}
		return usedVersionComparator;
	}
}
