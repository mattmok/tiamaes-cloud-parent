package com.tiamaes.cloud.util;

import java.util.UUID;
/**
 * 
 * @author Chen
 *
 */
public final class UUIDGenerator {

	private UUIDGenerator() {
	}
	
	public static String getUUID(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static void main(String[] args) {
		System.out.println(UUIDGenerator.getUUID());
	}
}
