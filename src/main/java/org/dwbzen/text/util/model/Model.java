package org.dwbzen.text.util.model;

import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

public class Model implements IJson, INameable  {
	private static final long serialVersionUID = 1L;
	private String name = null;
	private String content = null;
	
	public Model(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String aname) {
		name = aname;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
