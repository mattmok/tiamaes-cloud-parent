package com.tiamaes.cloud;


public interface Enum<E extends java.lang.Enum<E>> {
	
	public String getName();
	
	public String getValue();
	
	public int getIndex();
}
