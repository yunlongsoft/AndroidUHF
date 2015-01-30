package com.example.common;

import java.util.UUID;

public class CreateGuid {
	public static final String GenerateGUID(){
		  UUID uuid = UUID.randomUUID();
		  return uuid.toString();  
		 }
}
