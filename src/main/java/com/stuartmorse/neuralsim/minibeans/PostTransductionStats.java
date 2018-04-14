package com.stuartmorse.neuralsim.minibeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.stuartmorse.neural.Ligand;
import com.stuartmorse.neural.LigandType;
import com.stuartmorse.neural.neuron.Synapse;
import com.stuartmorse.neural.receptor.Receptor;

public class PostTransductionStats {
	
	private boolean success = false;
	private double voltage = 0.0;
	private Map<String, HashMap<String, HashMap<String, Integer>>> bound = new HashMap<>();
	private Map<String, HashMap<String, HashMap<String, Integer>>> blocked = new HashMap<>();
	private Map<String, Integer> totalReceptors = new HashMap<>();
	private Map<String, Integer> electrolytes = new HashMap<>();
	
	
	public PostTransductionStats(boolean success, double voltage) {
		this.success = success;
		this.voltage = voltage;
	}
	
	public PostTransductionStats compileResults(Synapse synapse) {
		
		Map<LigandType, ArrayList<Receptor>> ligandTypes = synapse.getReceptors();
		Set<LigandType> keys = ligandTypes.keySet();
		Map<String, Map<String, Map<String, Integer>>> ligandReceptors = new HashMap<>();
		String ligandName;
		int boundCount;
		int blockedCount;
		
		for (LigandType key : keys) {
			ArrayList<Receptor> receptors = ligandTypes.get(key);
			Map<String, HashMap<String, HashMap<String, Integer>>> currentLigand = new HashMap<>();
			HashMap<String, HashMap<String, Integer>> boundReceptors = new HashMap<>();
			HashMap<String, HashMap<String, Integer>> blockedReceptors = new HashMap<>();
			HashMap<String, Integer> currentTherapeutic = new HashMap<>();
			
			boundCount = 0;
			blockedCount = 0;
			
			for (Receptor receptor : receptors) {
				
				String receptorName = receptor.getClass().getSimpleName();
				String lastReceptor;
				
				if (!boundReceptors.containsKey(receptorName)) {
					currentLigand.put(receptorName, null);
				}
				
				if (receptor.isBound()) {
					Class<? extends Ligand> boundTo = receptor.getBoundTo();
					ligandName = boundTo.getSimpleName();
					
					if (!boundReceptors.containsKey(receptorName)) {
						HashMap<String, Integer> counter = new HashMap<String, Integer>();
						counter.put(ligandName, new Integer(0));
						boundReceptors.put(receptorName, counter);
					}
					
					// Check out redundant code from previous if
					if (!currentTherapeutic.containsKey(ligandName)) {
						currentTherapeutic.put(ligandName, new Integer(0));
						boundReceptors.put(receptorName, currentTherapeutic);
					}
					
					Integer therapeuticCount = boundReceptors.get(receptorName).get(ligandName);
					HashMap<String, Integer> counter = new HashMap<String, Integer>();
					boundCount = therapeuticCount.intValue();
					boundCount++;
					counter.put(ligandName, new Integer(boundCount));
					boundReceptors.put(receptorName, counter);
				};
				
				if (receptor.isBlocked()) {
					Class<? extends Ligand> blockedBy = receptor.getBlockedBy();
					ligandName = blockedBy.getSimpleName();
					
					if (!blockedReceptors.containsKey(receptorName)) {
						HashMap<String, Integer> counter = new HashMap<String, Integer>();
						counter.put(ligandName, new Integer(0));
						blockedReceptors.put(receptorName, counter);
					}
					
					// Check out redundant code from previous if
					if (!currentTherapeutic.containsKey(ligandName)) {
						currentTherapeutic.put(ligandName, new Integer(0));
						blockedReceptors.put(receptorName, currentTherapeutic);
					}
					
					Integer therapeuticCount = blockedReceptors.get(receptorName).get(ligandName);
					HashMap<String, Integer> counter = new HashMap<String, Integer>();
					blockedCount = therapeuticCount.intValue();
					blockedCount++;
					counter.put(ligandName, new Integer(blockedCount));
					blockedReceptors.put(receptorName, counter);
				}
			}
			totalReceptors.put(key.toString(), receptors.size());
			bound.put(key.toString(), boundReceptors);
			blocked.put(key.toString(), blockedReceptors);
		}
		
		return this;
	}
	
	public Map<String, Integer>getBindings(Receptor receptor) {
		
		return new HashMap<String, Integer>();
		
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public Map<String, HashMap<String, HashMap<String, Integer>>> getBound() {
		return bound;
	}

	public void setBound(Map<String, HashMap<String, HashMap<String, Integer>>> bound) {
		this.bound = bound;
	}

	public Map<String, HashMap<String, HashMap<String, Integer>>> getBlocked() {
		return blocked;
	}

	public void setBlocked(Map<String, HashMap<String, HashMap<String, Integer>>> blocked) {
		this.blocked = blocked;
	}

	public Map<String, Integer> getTotalReceptors() {
		return totalReceptors;
	}

	public void setTotalReceptors(Map<String, Integer> totalReceptors) {
		this.totalReceptors = totalReceptors;
	}

}
