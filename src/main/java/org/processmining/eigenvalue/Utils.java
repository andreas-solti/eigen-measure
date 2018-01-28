package org.processmining.eigenvalue;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.*;
import org.jgraph.JGraph;
import org.processmining.eigenvalue.converter.RelaxedPT2PetrinetConverter;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.petrinets.analysis.gedsim.utils.StringEditDistance;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.visualization.tree.TreeLayoutBuilder;
import org.progressmining.xeslite.plugin.OpenLogFileLiteImplPlugin;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Utils {
	
	public static final String PRECISION_MEASURE = "Precision";
	public static final String GENERALIZATION_MEASURE = "Generalization";


	public static final String TEST_FOLDER = "test/testfiles/";

	public static final ProMCanceller NOT_CANCELLER = new ProMCanceller() {
		@Override
		public boolean isCancelled() {
			return false;
		}
	};

	private static final String[] nameAlphabet = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","v","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9"};
	

	public static ProcessTree mineProcessTree(XLog inputLog, double noiseThreshold) {
		MiningParameters parameters = new MiningParametersIMi();
		parameters.setNoiseThreshold((float)noiseThreshold);
		ProcessTree tree = IMProcessTree.mineProcessTree(inputLog, parameters);
		RelaxedPT2PetrinetConverter.postProcessMinedTree(tree);
		return tree;
	}
	
	public static XLog cloneLog(XLog log){
		XLog logClone = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		for (XTrace trace : log){
			XTrace traceClone = XFactoryRegistry.instance().currentDefault().createTrace((XAttributeMap) trace.getAttributes().clone());
			for (XEvent event : trace){
				XEvent eventClone = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) event.getAttributes().clone());
				traceClone.add(eventClone);
			}
			logClone.add(traceClone);
		}
		return logClone;
	}
	
	
	public static XLog flattenLifecycles(XLog log) {
		XLog merged = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log){
			XTrace newTrace =  XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			for (XEvent event : trace){
				XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
				String name = XConceptExtension.instance().extractName(event);
				String lc = XLifecycleExtension.instance().extractTransition(event);
				XConceptExtension.instance().assignName(newEvent, name+"_"+lc);
				newTrace.add(newEvent);
			}
			merged.add(newTrace);
		}
		return merged;
	}

	
	public static JGraph getGraphForTree(ProcessTree tree) {
		TreeLayoutBuilder builder = new TreeLayoutBuilder(tree);
		JGraph graph = builder.getJGraph();
		graph.setPreferredSize(new Dimension(1200,500));
		return graph;
	}
	
	public static Double getTraceSimilarity(XLog inputLog, XLog resultLog) {
		int numEventsDifferent = 0;
		int allEvents = 0;
		if (inputLog.size() != resultLog.size()){
			throw new IllegalArgumentException("Logs must have equal number of traces!");
		}
		Map<String,String> eventEncoding = new HashMap<>();
		for (int trIndex = 0 ; trIndex < inputLog.size(); trIndex++){
			XTrace trInput = inputLog.get(trIndex);
			XTrace trResult = resultLog.get(trIndex);
			String inputString = getTraceString(trInput, eventEncoding);
			String resultString = getTraceString(trResult, eventEncoding);
			allEvents += inputString.length();
			allEvents += resultString.length();
			numEventsDifferent += StringEditDistance.editDistance(inputString, resultString);
		}
		return 1 - (numEventsDifferent / (double)allEvents);
	}
	
	private static String getTraceString(XTrace trInput, Map<String, String> eventEncoding) {
		StringBuffer buf = new StringBuffer();
		for (XEvent e : trInput){
			String evName = XConceptExtension.instance().extractName(e);
			if (!eventEncoding.containsKey(evName)){
				eventEncoding.put(evName, nameAlphabet[eventEncoding.size()]);
			}
			buf.append(eventEncoding.get(evName));
		}
		return buf.toString();
	}

    /**
     * Extracts a name of an object (e.g. XLog) and returns the parameter defaultName if the name is not set
     * @param object {@link XAttributable} some object that potentially has a name
     * @param defaultName String default name
     * @return String
     */
	public static String getName(XAttributable object, String defaultName){
        return getOrDefault(XConceptExtension.instance().extractName(object), defaultName);
    }
    public static String getName(PetrinetGraph net, String defaultName){
	    return getOrDefault(net.getLabel(), defaultName);
    }
    private static String getOrDefault(String name, String defaultName) {
        if (name == null || name.trim().isEmpty()){
            name = defaultName;
        }
        return name;
    }



	/**
	 * Loads a log from the tests/testfiles folder of the plugin.
	 *
	 * @param name the file name (including the suffix such as .xes, .xes.gz, or .mxml)
	 * @return the loaded {@link XLog}
	 * @throws Exception
	 */
	public static XLog loadLog(String name) throws Exception {
		return loadLog(new File(TEST_FOLDER+name));
	}

	/**
	 * Opens a Log from a given file.
	 *
	 * @param file {@link File} containing the log.
	 * @return the loaded {@link XLog}
	 * @throws Exception
	 */
	public static XLog loadLog(File file) throws Exception {
		OpenLogFileLiteImplPlugin openPlugin = new OpenLogFileLiteImplPlugin();
		return (XLog) openPlugin.importFile(StochasticNetUtils.getDummyUIContext(), file);
	}
}
