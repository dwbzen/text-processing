package org.dwbzen.text.util;


/**
 * Predictably enough, something that has a name.
 * @author don_bacon
 *
 */
public interface INameable  {
	
	static final String NAME = "name";
	
	void setName(String name);
	String getName();

}
