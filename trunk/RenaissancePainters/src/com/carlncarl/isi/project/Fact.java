package com.carlncarl.isi.project;

import java.io.Serializable;

public class Fact implements Serializable{

	private static final long serialVersionUID = 5971025828782619877L;
	private String a;
	private String rel;
	private String b;

	public Fact(String a, String rel, String b) {
		this.a = a;
		this.rel = rel;
		this.b = b;
	}

	public String getA() {
		return a;
	}

	public String getRel() {
		return rel;
	}

	public String getB() {
		return b;
	}

	@Override
	public boolean equals(Object obj) {
		Fact factB = (Fact) obj;
		if (this.a.equals(factB.a) 
				&& this.rel.equals(factB.rel)
				&& this.b.equals(factB.b)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return a + " : " + rel + " : " + b;
	}
}
