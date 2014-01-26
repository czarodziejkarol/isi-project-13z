package com.carlncarl.isi.project;

import java.io.Serializable;

public class Action implements Serializable {

	private static final long serialVersionUID = -23014994073080336L;
	public final static int ALL_PATHS = 0;
	public final static int ONE_PATH = 1;
	public final static int ADD_FACT = 2;

	private int method;
	private String arg;
	private String question;

	private String[] groups;

	@Override
	public String toString() {

		String result = method + "_" + arg;
		for (int i = 0; i < groups.length; i++) {
			result += "_" + groups[i];
		}
		return result;
	}

	public Action(int method, String arg) {
		this.method = method;
		this.arg = arg;
	}

	public Action(String content) {
		String[] splCon = content.split("_");
		try{
		this.method = Integer.parseInt(splCon[0]);
		this.arg = splCon[1];
		groups = new String[splCon.length-2];
		
		for (int i = 2; i < splCon.length; i++) {
			groups[i-2] = splCon[i];
		} }catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String[] getGroups() {
		return groups;
	}

	public void setGroups(String[] groups) {
		this.groups = groups;
	}
}
