package org.dwbzen.text.util.model;
import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

public class ModelCollection implements IJson, INameable {
	private static final long serialVersionUID = 1L;
	private String name = null;
	private List<Model> models = new ArrayList<>();
	
	public ModelCollection(String name) {
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

	public List<Model> getModels() {
		return models;
	}

	public boolean addModel(Model m) {
		return models.add(m);
	}
}
