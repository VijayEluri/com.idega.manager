/*
 * Created on Dec 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.idega.manager.view;

import javax.faces.context.FacesContext;
import com.idega.core.view.ApplicationViewNode;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.repository.data.Instantiator;
import com.idega.repository.data.Singleton;
import com.idega.repository.data.SingletonRepository;

/**
 * @author thomas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ManagerViewManager implements Singleton {
	
	private static Instantiator instantiator = new Instantiator() {
		public Object getInstance(Object parameter) {
			IWMainApplication iwma = null;
			if (parameter instanceof FacesContext) {
				iwma = IWMainApplication.getIWMainApplication((FacesContext) parameter);
			}
			else {
				iwma = (IWMainApplication) parameter;
			}
			return new ManagerViewManager(iwma);
		}
	};
	
	private static final String MANAGER_ID="manager";
	private static final String BUNDLE_IDENTIFIER="com.idega.manager";
	
	private ViewNode managerRootNode;
	private IWMainApplication iwma;
	
	private ManagerViewManager(IWMainApplication iwma){
		this.iwma=iwma;
	}
	
	public static ManagerViewManager getInstance(IWMainApplication iwma){
		return (ManagerViewManager) SingletonRepository.getRepository().getInstance(ManagerViewManager.class, instantiator, iwma);
	}
	
	public static ManagerViewManager getInstance(FacesContext context){
		return (ManagerViewManager) SingletonRepository.getRepository().getInstance(ManagerViewManager.class, instantiator, context);
	}
	
	public ViewManager getViewManager(){
		return ViewManager.getInstance(iwma);
	}
	
	
	public ViewNode getContentNode(){
		IWBundle iwb = iwma.getBundle(BUNDLE_IDENTIFIER);
		//ViewNode content = root.getChild(CONTENT_ID);
		if(managerRootNode==null){
			managerRootNode = initalizeContentNode(iwb);
		}
		return managerRootNode;
	}
	
	public ViewNode initalizeContentNode(IWBundle contentBundle){
		ViewNode root = getViewManager().getWorkspaceRoot();
		DefaultViewNode contentNode = new ApplicationViewNode(MANAGER_ID,root);
		contentNode.setJspUri(contentBundle.getJSPURI("InstallOrUpdateManager.jsp"));
		managerRootNode = contentNode;
		return managerRootNode;
	}
	
	
	public void initializeStandardNodes(IWBundle bundle){
		ViewNode contentNode = initalizeContentNode(bundle);
		
		// update/step 2
		DefaultViewNode updateNode = new DefaultViewNode("update",contentNode);
		updateNode.setJspUri(bundle.getJSPURI("UpdateListManager.jsp"));
		
		// update/step 3
		DefaultViewNode commitNode = new DefaultViewNode("commit",contentNode);
		commitNode.setJspUri(bundle.getJSPURI("ModuleManager.jsp"));
		
		// update/step 4
		DefaultViewNode installNode = new DefaultViewNode("install",contentNode);
		installNode.setJspUri(bundle.getJSPURI("InstallManager.jsp"));
	
		//install/step 2
		DefaultViewNode newModulesNode = new DefaultViewNode("newModules",contentNode);
		newModulesNode.setJspUri(bundle.getJSPURI("InstallListManager.jsp"));
		
		//install/step 3
		DefaultViewNode newModuleVersionNode = new DefaultViewNode("newModuleVersion",contentNode);
		newModuleVersionNode.setJspUri(bundle.getJSPURI("InstallNewModuleListManager.jsp"));
		
		
		
		
//		DefaultViewNode searchNode = new DefaultViewNode("search",contentNode);
//		searchNode.setJspUri(bundle.getJSPURI("search.jsp"));	
//		
//		DefaultViewNode uploadNode = new DefaultViewNode("upload",contentNode);
//		uploadNode.setJspUri(bundle.getJSPURI("upload.jsp"));	
		
	}
}



