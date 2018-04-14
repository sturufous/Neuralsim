package com.stuartmorse.neuralsim.minibeans;

public class ReceptorData {
	
	private String id;
	private int count;
	private int vesicles;
	
	public ReceptorData() {
		super();
	}

	public ReceptorData(String id, int count, int vesicles) {
		this.id = id;
		this.count = count;
		this.vesicles = vesicles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getVesicles() {
		return vesicles;
	}

	public void setVesicles(int vesicles) {
		this.vesicles = vesicles;
	}
}
