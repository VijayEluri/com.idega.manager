package com.idega.manager.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.util.StringUtil;

@DataTransferObject
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MigrationProgress implements Serializable, DWRAnnotationPersistance {

	private static final long serialVersionUID = -1746288169702496590L;

	@RemoteProperty
	private String progress;

	private Map<String, Boolean> copiedFiles = new HashMap<String, Boolean>();

	@RemoteProperty
	private Collection<String> failures;

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public Collection<String> getFailures() {
		return failures;
	}

	public void setFailures(Collection<String> failures) {
		this.failures = failures;
	}

	public void addFailure(String failure) {
		if (StringUtil.isEmpty(failure))
			return;

		if (failures == null)
			failures = new ArrayList<String>();

		failures.add(failure);
	}

	public boolean isFileCopied(String file) {
		if (StringUtil.isEmpty(file))
			return false;

		Boolean copied = copiedFiles.get(file);
		return copied == null ? Boolean.FALSE : Boolean.TRUE;
	}

	public void doMarkFileAsCopied(String file) {
		if (StringUtil.isEmpty(file))
			return;

		copiedFiles.put(file, Boolean.TRUE);
	}

	public void doClearFailures() {
		if (failures != null)
			failures.clear();
	}
}