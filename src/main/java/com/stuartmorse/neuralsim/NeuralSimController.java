package com.stuartmorse.neuralsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.stuartmorse.neural.IonChannelType;
import com.stuartmorse.neural.LigandType;
import com.stuartmorse.neural.Therapeutic;
import com.stuartmorse.neural.Voltage;
import com.stuartmorse.neural.ionchannel.CAMPIonChannel;
import com.stuartmorse.neural.ionchannel.IonChannel;
import com.stuartmorse.neural.ionchannel.VGCalciumIonChannel;
import com.stuartmorse.neural.neuron.Neuron;
import com.stuartmorse.neural.neuron.PNSNeuron;
import com.stuartmorse.neural.neuron.Synapse;
import com.stuartmorse.neural.receptor.DopamineD2Receptor;
import com.stuartmorse.neural.receptor.Receptor;
import com.stuartmorse.neuralsim.minibeans.IonPotential;
import com.stuartmorse.neuralsim.minibeans.NeuronMetadata;
import com.stuartmorse.neuralsim.minibeans.PostTransductionStats;

@Controller
@RequestMapping(value = "/")
public class NeuralSimController {
	
	class OutputVoltage {
		
		public double voltage;

		public double getVoltage() {
			return voltage;
		}

		public void setVoltage(double voltage) {
			this.voltage = voltage;
		}
	}
	
	NeuralSimDefaults defaults =  new NeuralSimDefaults();
	
	ServletContext context; 
	
	@Autowired
	NeuralSimController(NeuralSimDefaults neuralSimDefaults, ServletContext context) {
		this.defaults = neuralSimDefaults;
		this.context = context;
		
		Map<String, Double> ionPotentials = new HashMap<>();
		ionPotentials.put("Na+", defaults.getSodiumPotential());
		context.setAttribute("ion-potentials", ionPotentials);
	}
	
	@RequestMapping(value = "test", method = RequestMethod.GET)
	public ModelAndView showMessage(HttpServletRequest request) {

		System.out.println("Hello Stuart Morse!");
		StuTest st = new StuTest("Hello New!", 1.111);
		st.setNum1(1.11111);
		ModelAndView response = new ModelAndView("jsp/firstone.jsp", "model", st);
		return response;
	}

	@RequestMapping(value = "marshall", method = RequestMethod.POST, consumes="application/json", produces="application/json")
	public ResponseEntity<OutputVoltage> marshallList(@RequestBody ArrayList<StuTest> input) {

		HttpHeaders responseHeaders = new HttpHeaders();
		System.out.println("Input.msg1 = " + input.get(0).getMsg1());
		OutputVoltage volt = new OutputVoltage();
		volt.setVoltage(0.084);
		return new ResponseEntity<OutputVoltage>(volt, responseHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(value="/addneuronchain", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public ResponseEntity<NeuronMetadata> addNeuronChain(@RequestBody StuTest input) throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
		final int CHAIN_LENGTH = 1;
		OutputVoltage volt;
		Neuron prev = null;
		double level = 0;
		
		// Get external ion potentials
		Map<String, Double> ionPotentials = (Map<String, Double>) context.getAttribute("ion-potentials");
		if (ionPotentials == null) {
			level = defaults.getSodiumPotential();
		} else {
			level = ionPotentials.get("Na+");	
		}

		@SuppressWarnings("unchecked")
		List<Neuron> neuronChain = new ArrayList<Neuron>();
		
		// Create chain of neurons
		for (int idx=0; idx < CHAIN_LENGTH; idx++) {
			PNSNeuron next = new PNSNeuron(1, 8, 1, idx);
			neuronChain.add(next);
			
			Synapse synapse = new Synapse(prev, next);
			synapse.addReceptors(60, DopamineD2Receptor.class);
			synapse.addSynapticVesicles(LigandType.DOPAMINE, 20);
			//synapse.setPreSynapticConcentration(GabaPentin.class, 0.35);
			synapse.addPreSynapticIonChannels(100, IonChannelType.VGCA_ION_CHANNEL, VGCalciumIonChannel.class);
			next.setExternalSodiumPotential(level);
			next.addCNGIonChannels(960, CAMPIonChannel.class);
			next.setHeadSynapse(synapse);
			
			if (prev != null) {
				prev.setTailSynapse(synapse);
			}
			prev = next;
		}
		
		PNSNeuron head = (PNSNeuron) neuronChain.get(0);
		Synapse rootSynapse = new Synapse(null, head);
		rootSynapse.addReceptors(60, DopamineD2Receptor.class);
		rootSynapse.addSynapticVesicles(LigandType.DOPAMINE, 20);
		//synapse.setPreSynapticConcentration(GabaPentin.class, 0.35);
		rootSynapse.addPreSynapticIonChannels(100, IonChannelType.VGCA_ION_CHANNEL, VGCalciumIonChannel.class);

		context.setAttribute("root-synapse", rootSynapse);
		context.setAttribute("neuron-chain", neuronChain);

		head.setHeadSynapse(rootSynapse);
		rootSynapse.transduceSignal(Voltage.FIRING_THRESHOLD.getValue());
		double internalVoltage = head.epspController(rootSynapse);
		
		volt = new OutputVoltage();
		volt.setVoltage(internalVoltage);
		
		return new ResponseEntity<NeuronMetadata>(new NeuronMetadata(), responseHeaders, HttpStatus.CREATED);
	}
	
	@RequestMapping(value="/addneuron", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public ResponseEntity<PostTransductionStats> addNeuron(@RequestBody NeuronMetadata input) throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
				
		// Standardize on single neuron for now
		PNSNeuron head = new PNSNeuron(input.getDendriteCount(), 
									   input.getNodeCount(),
									   input.getTerminalCount(), 1);
		
		Synapse rootSynapse = new Synapse(null, head);
		
		// Add receptors to the neuron
		for (Map<String, String> receptor : input.getReceptors()) {
			String fullName = "com.stuartmorse.neural.receptor." + receptor.get("id");
			@SuppressWarnings("unchecked")
			Class<? extends Receptor> current = (Class<? extends Receptor>) Class.forName(fullName);
			rootSynapse.addReceptors(new Integer(receptor.get("count")).intValue(), current);
			int vesicleCount = Integer.parseInt(receptor.get("vesicles"));
			rootSynapse.addSynapticVesicles(current.newInstance().getLigandType(), vesicleCount);
		}
		
		// Add therapeutics to the substrate
		for (Map<String, String> therapeutic : input.getTherapeutics()) {
			String fullName = "com.stuartmorse.neural.therapeutics." + therapeutic.get("id");
			Class<? extends Therapeutic> current = (Class<? extends Therapeutic>) Class.forName(fullName);
			// Convert from % bound to fraction
			double concentration = (Double.parseDouble(therapeutic.get("value")) / 100);
			if (therapeutic.get("id").equals("GabaPentin")) {
				// TODO: Genericise this conditional
				rootSynapse.setPreSynapticConcentration(current, concentration);	
			} else {
				rootSynapse.setTherapeuticConcentration(current, concentration);
			}
		}
		
		// Add ion channels to the neuron
		for (Map<String, String> channel : input.getChannels()) {
			String fullName = "com.stuartmorse.neural.ionchannel." + channel.get("id");
			Class<? extends IonChannel> current = (Class<? extends IonChannel>) Class.forName(fullName);
			if (channel.get("id").equals("CAMPIonChannel")) {
				head.addCNGIonChannels(new Integer(channel.get("count")).intValue(), current);
			} else if (channel.get("id").equals("VGCalciumIonChannel")) {
				rootSynapse.addPreSynapticIonChannels(Integer.parseInt(channel.get("count")), IonChannelType.VGCA_ION_CHANNEL, current);	
			}
		}
		
		// Add electrolytes to the substrate
		for (Map<String, String> electrolyte : input.getElectrolytes()) {
			String ion = electrolyte.get("id");
			if (ion.equals("Na+")) {
				double sodiumPotential = (Double.parseDouble(electrolyte.get("level")) / 1000);
				head.setExternalSodiumPotential(sodiumPotential);
			}
		}
		
		//synapse.setPreSynapticConcentration(GabaPentin.class, 0.35);

		context.setAttribute("root-synapse", rootSynapse);
		//context.setAttribute("neuron-chain", neuronChain);

		head.setHeadSynapse(rootSynapse);
		rootSynapse.transduceSignal(Voltage.FIRING_THRESHOLD.getValue());
		double internalVoltage = head.epspController(rootSynapse);
		
		boolean success = internalVoltage >= Voltage.FIRING_THRESHOLD.getValue();
		PostTransductionStats stats = new PostTransductionStats(success, internalVoltage);
		stats.compileResults(rootSynapse);
		
		return new ResponseEntity<PostTransductionStats>(stats, responseHeaders, HttpStatus.CREATED);
	}
	
	@RequestMapping(value="/setionpotentials", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public ResponseEntity<Map<String, Double>> pushJson(@RequestBody ArrayList<IonPotential> input) throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
		@SuppressWarnings("unchecked")
		Map<String, Double>ionPotentials = (Map<String, Double>) context.getAttribute("ion-potentials");
		
		for(IonPotential potential : input) {
			ionPotentials.put(potential.getIonType(), potential.getPotential());
		}
		
		context.setAttribute("ion-potentials", ionPotentials);
		return new ResponseEntity<Map<String, Double>>(ionPotentials, responseHeaders, HttpStatus.CREATED);		
	}
}