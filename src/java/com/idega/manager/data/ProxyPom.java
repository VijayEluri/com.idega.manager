/*
 * $Id: ProxyPom.java,v 1.1 2004/11/26 17:19:09 thomas Exp $
 * Created on Nov 22, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.manager.data;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.idega.manager.business.RepositoryBrowser;
import com.idega.manager.util.ManagerUtils;
import com.idega.util.IWTimestamp;
import com.idega.util.StringHandler;
import com.sun.rsasign.b;


/**
 * 
 *  Last modified: $Date: 2004/11/26 17:19:09 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.1 $
 */
public class ProxyPom implements Pom {
	
	// see examples: 
	// com.idega.block.article-20041109.112340.pom
	// com.idega.core-1.9.1.pom 
	// com.idega.content-SNAPSHOT.pom
	private static final String ARTIFACT_ID_VERSION_SEPARATOR = "-";
	public static final String EXTENSION = ".pom";
	public static final String POM_TIMESTAMP_FORMAT = "yyyyMMdd.HHmmss";
	
	private static SimpleDateFormat dateParser = null;
	
	private static SimpleDateFormat getDateParser() {
		if (dateParser == null) {
			dateParser = new SimpleDateFormat(POM_TIMESTAMP_FORMAT);
		}
		return dateParser;
	}
	
	static private Logger getLogger(){
		 return Logger.getLogger(ProxyPom.class.getName());
	 }
	
	private RealPom realSubject = null;
	private RepositoryBrowser repositoryBrowser = null;
	
	private String fileName = null;
	
	private String artifactId = null;
	private String currentVersion = null;
	
	private boolean snapshot = false;
	private IWTimestamp timestamp = null;
	
	public ProxyPom(String nameOfFile, RepositoryBrowser repositoryBrowser) {
		this.repositoryBrowser = repositoryBrowser;
		initialize(nameOfFile);
	}
		
		
	private void initialize(String nameOfFile) {
		this.fileName = nameOfFile;
		String[] partOfFileName = fileName.split(ARTIFACT_ID_VERSION_SEPARATOR);
		artifactId = partOfFileName[0];
		String tempVersion = null; 
		if (partOfFileName.length < 2) {
			tempVersion = "no version available";
		}
		else {
			tempVersion = StringHandler.cutExtension(partOfFileName[1]);
		}
		// is it a snapshot?
		// com.idega.content-SNAPSHOT.pom
		if (RealPom.isSnapshot(tempVersion)) { 
			snapshot = true;
			currentVersion = tempVersion;
		}
		else {
			// is it a snapshot with a timestamp?
			// com.idega.block.article-20041109.112340.pom
			// parse timestamp
			SimpleDateFormat parser = ProxyPom.getDateParser();
			try {
				Date date = parser.parse(tempVersion);
				timestamp = new IWTimestamp(date);
				currentVersion = "";
				snapshot = true;
			}
			catch (ParseException ex) {
				// do nothing
				timestamp = null;
			}
		}
		// is it a version?  
		// com.idega.core-1.9.1.pom 
		if (timestamp == null) {
			currentVersion = tempVersion;
		}
	}
	public String getArtifactId() {
		return artifactId;
	}
	public String getCurrentVersion() {
		return currentVersion;
	}
	public boolean isSnapshot() {
		return snapshot;
	}
	public IWTimestamp getTimestamp() {
		return timestamp;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	private RealPom getRealSubject() {
		if (realSubject == null) {
			File pomFile = repositoryBrowser.getPom(getFileName());
			try {
				realSubject = RealPom.getPom(pomFile);
			}
			catch (IOException ex) {
				getLogger().log(Level.WARNING, "[PomProxy] Could not download real subject: "+ getFileName() , ex);
				realSubject = null;
			}
		}
		return realSubject;
	}
	
	public Pom getPom(Dependency dependency) throws IOException {
		if (! dependency.isBundle()) {
			throw new IOException("Dependency does not belong to group bundles");
		}
		String dependencyArtifactId = dependency.getArtifactId();
		StringBuffer buffer = new StringBuffer(dependencyArtifactId);
		buffer.append(ARTIFACT_ID_VERSION_SEPARATOR);
		String version = dependency.getVersion();
		version = RealPom.isSnapshot(version) ? RealPom.SNAPSHOT : version;
		buffer.append(version).append(EXTENSION);
		String pomFileName = repositoryBrowser.convertPomNameIfNecessary(buffer.toString());
		ProxyPom proxy = new ProxyPom(pomFileName, repositoryBrowser);
		return proxy;
	}
	
	public List getDependencies() {
		RealPom pom = getRealSubject();
		return (pom == null) ? null : pom.getDependencies();
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(artifactId).append(" ").append(fileName);
		return buffer.toString();
	}

}
