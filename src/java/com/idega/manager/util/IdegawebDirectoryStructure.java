/*
 * Created on Jan 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.idega.manager.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.idega.idegaweb.IWMainApplication;
import com.idega.io.ZipInstaller;
import com.idega.manager.data.Module;
import com.idega.presentation.IWContext;

/**
 * @author thomas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IdegawebDirectoryStructure {
	
	// keys
	private static final String WORKING_DIRECTORY_KEY= "auxiliaryManagerFolder";
	private static final String APPLICATION_KEY = "application";
	private static final String WEB_INF_KEY = "WEB_INF";
	private static final String LIBRARY_KEY = "library";
	private static final String TAG_LIBRARY_KEY ="tagLibrary";
	private static final String FACES_CONFIG_FILE_KEY ="facesConfig";
	private static final String DEPLOYMENT_DESCRIPTOR_FILE_KEY = "web";
	private static final String APPLICATION_SPECIAL_KEY = "applicationSpecial";
	private static final String BUNDLES_KEY = "bundles";
	
	// real existing folders or files
	private static final String WORKING_FOLDER = "auxiliaryManagerFolder";
	private static final String FACES_CONFIG_FILE = "faces-config.xml";
	private static final String WEB_DEPLOYMENT_FILE = "web.xml";
	private static final String WEB_INF_FOLDER = "WEB-INF";
	private static final String WEB_LIBRARY_FOLDER = "lib";
	private static final String WEB_TAG_LIBRARY_FOLDER = "tld";

	private IWContext context = null;
	private Map pathes = null;

	public IdegawebDirectoryStructure(IWContext context) {
		this.context = context;
	}
	
	public File getDeploymentDescriptor() {
		return getPath(DEPLOYMENT_DESCRIPTOR_FILE_KEY);
	}
	
	public File getFacesConfig() {
		return getPath(FACES_CONFIG_FILE_KEY);
	}
	
	public File getBundlesRealPath()	{
		return getPath(BUNDLES_KEY);
	}
	
	
	public File getLibrary() {
		return getPath(LIBRARY_KEY);
	}
	
	public File getTagLibrary()	{
		return getPath(TAG_LIBRARY_KEY);
	}
	
	public File getWorkingDirectory() {
		File workingDir = getPath(WORKING_DIRECTORY_KEY);
		if (! workingDir.exists()) {
			workingDir.mkdir();
		}
		return workingDir;
	}	
	
	public File getDeploymentDescriptor(Module module) throws IOException {
		File webInf = getWebInf(module);
		return new File(webInf, WEB_DEPLOYMENT_FILE);
		
	}
	
	public File getFacesConfig(Module module) throws IOException {
		File webInf = getWebInf(module);
		return new File(webInf, FACES_CONFIG_FILE);
	}
	
	public File getLibrary(Module module) throws IOException {
		File webInf = getWebInf(module);
		return new File(webInf, WEB_LIBRARY_FOLDER);
	}
	
	public File getTagLibrary(Module module) throws IOException {
		File webInf = getWebInf(module);
		return getTagLibraryFromWebInf(webInf);
	}
	
	public File getTagLibrary(File bundleFolder) {
		File webInf = getWebInf(bundleFolder);
		return getTagLibraryFromWebInf(webInf);
	}
	
	public File getExtractedArchive(Module module) throws IOException {
		File temporaryInstallationFolder = module.getBundleArchive().getParentFile();
		String artifactId = module.getArtifactId();
		File extractedArchive = new File (temporaryInstallationFolder, artifactId);
		if (! extractedArchive.exists()) {
			extractedArchive.mkdir();
			File bundleArchive = module.getBundleArchive();
			ZipInstaller zipInstaller = new ZipInstaller();
			zipInstaller.extract(bundleArchive, extractedArchive);
		}
		return extractedArchive;
	}
	
	public File getCorrespondingFileFromWebInf(Module module, File fileInWebInf) throws IOException {
		String name = fileInWebInf.getName();
		File webInf = getWebInf(module);
		return new File(webInf, name);
		
	}
	
	private File getWebInf(Module module) throws IOException {
		File extractedArchive = getExtractedArchive(module);
		return getWebInf(extractedArchive);
	}
	
	private File getWebInf(File bundleFolder) {
		return new File(bundleFolder, WEB_INF_FOLDER);
	}
	
	private File getTagLibraryFromWebInf(File webInf) {
		return new File(webInf, WEB_TAG_LIBRARY_FOLDER);
	}
	
	private File getPath(String key) {
		if (pathes == null) {
			initializePathes();
		}
		return (File) pathes.get(key);
	}
	
	private void initializePathes() {
		pathes = new HashMap();
		
		IWMainApplication mainApplication = context.getIWMainApplication();
		File application = new File(mainApplication.getApplicationRealPath());
		pathes.put(APPLICATION_KEY, application);
		
		File bundles = new File(mainApplication.getBundlesRealPath());
		pathes.put(BUNDLES_KEY, bundles);
		
		File applicationSpecial = new File(mainApplication.getApplicationSpecialRealPath());
		pathes.put(APPLICATION_SPECIAL_KEY, applicationSpecial);
		pathes.put(WORKING_DIRECTORY_KEY, new File(applicationSpecial, WORKING_FOLDER));
		
		File webInf =  new File(application, WEB_INF_FOLDER);
		pathes.put(WEB_INF_KEY, webInf);
		pathes.put(FACES_CONFIG_FILE_KEY, new File(webInf, FACES_CONFIG_FILE));
		pathes.put(DEPLOYMENT_DESCRIPTOR_FILE_KEY, new File(webInf, WEB_DEPLOYMENT_FILE));
		pathes.put(LIBRARY_KEY, new File(webInf, WEB_LIBRARY_FOLDER));
		pathes.put(TAG_LIBRARY_KEY, new File(webInf, WEB_TAG_LIBRARY_FOLDER));
	}
}
