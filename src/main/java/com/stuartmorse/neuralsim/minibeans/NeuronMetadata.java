package com.stuartmorse.neuralsim.minibeans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class NeuronMetadata implements Serializable {
	
	private int dendriteCount = 1;
	private int terminalCount = 1;
	private int nodeCount = 8;
	private List<Map<String, String>> receptors;
	private List<Map<String, String>> therapeutics;
	private List<Map<String, String>> channels;
	private List<Map<String, String>> electrolytes;
	
	public NeuronMetadata() {
		super();
	}
	
	public List<Map<String, String>> getElectrolytes() {
		return electrolytes;
	}

	public void setElectrolytes(List<Map<String, String>> electrolytes) {
		this.electrolytes = electrolytes;
	}

	public List<Map<String, String>> getReceptors() {
		return receptors;
	}

	public void setReceptors(List<Map<String, String>> receptors) {
		this.receptors = receptors;
	}

	public List<Map<String, String>> getTherapeutics() {
		return therapeutics;
	}

	public void setChannels(List<Map<String, String>> channels) {
		this.channels = channels;
	}

	public List<Map<String, String>> getChannels() {
		return channels;
	}

	public void setTherapeutics(List<Map<String, String>> therapeutics) {
		this.therapeutics = therapeutics;
	}

	public int getDendriteCount() {
		return dendriteCount;
	}
	public void setDendriteCount(int dendriteCount) {
		this.dendriteCount = dendriteCount;
	}
	public int getTerminalCount() {
		return terminalCount;
	}
	public void setTerminalCount(int terminalCount) {
		this.terminalCount = terminalCount;
	}
	public int getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

}
