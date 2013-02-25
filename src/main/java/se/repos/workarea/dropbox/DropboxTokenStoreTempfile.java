/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */

package se.repos.workarea.dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;

public class DropboxTokenStoreTempfile implements DropboxTokenStore{
	
	public static final Lgr logger = LgrFactory.getLogger();
	private HashMap<String,File> tokenFiles;
	
	public DropboxTokenStoreTempfile(){
		tokenFiles = new HashMap<String,File>();
	}
	

	@Override
	public List<String> read(String userName) {
		File tokensFile = tokenFiles.get(userName);
		List<String> tokens = null;
		if(tokensFile != null){
			tokens = new LinkedList<String>();
			try{
				FileInputStream in = new FileInputStream(tokensFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				while((strLine = br.readLine())!= null){
					tokens.add(strLine);
				}
				br.close();
			}catch(Exception e){
				String errorMsg = "Something went wrong while trying to read TOKENS file";
				logger.error(errorMsg,e);
				throw new RuntimeException(errorMsg,e);
			}
		}
		
		return tokens;
	}

	@Override
	public void write(String userName, List<String> tokens) {
		File tokensFile = new File("TOKENS");
		try {
			
			PrintWriter tokenWriter = new PrintWriter(tokensFile);
			tokenWriter.println(tokens.get(0));
			tokenWriter.println(tokens.get(1));
			tokenWriter.close();
		} catch (Exception e) {
			String errorMsg = "Something went wrong while writing tokens to file.";
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}
		
			tokenFiles.put(userName, tokensFile);
	}
}
