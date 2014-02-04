package com.carlncarl.isi.project;

import java.io.Serializable;

public class Fact implements Serializable{

	private static final long serialVersionUID = 5971025828782619877L;
	private ObjectInfo a;
	private String rel;
	private ObjectInfo b;

	public Fact(ObjectInfo a, String rel, ObjectInfo b) {
		this.a = a;
		this.rel = rel;
		this.b = b;
	}

	public ObjectInfo getA() {
		return a;
	}

	public String getRel() {
		return rel;
	}

	public ObjectInfo getB() {
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
