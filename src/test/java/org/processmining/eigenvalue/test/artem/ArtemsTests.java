package org.processmining.eigenvalue.test.artem;

import org.processmining.eigenvalue.test.TestUtils;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.automata.PrecisionRecallComputer;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

public class ArtemsTests {

    protected static ProMCanceller canceller = new ProMCanceller() {
        @Override
        public boolean isCancelled() {
            return false;
        }
    };

    protected EntropyResult resultL;
    protected UIPluginContext context;
    protected XLog log;
    protected String logName;
    protected XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;

    protected File parentFolder;
    protected File[] pnmlFiles;

    protected void cacheResultForLogAutomaton() throws Exception {
        Object[] netAndMarking = TestUtils.loadModel(pnmlFiles[0].getName(), false, parentFolder);
        AcceptingPetriNet net = getAcceptingPetriNet(netAndMarking);

        EfficientLog elog = new EfficientLog(log, eventClassifier);
        String[] activities = elog.getActivities();
        String[] names = PrecisionRecallComputer.getTransitionNames(net, activities);

        resultL = PrecisionRecallComputer.getEntropyLogResult(context, canceller, log, logName,  elog, names);
    }

    protected AcceptingPetriNet getAcceptingPetriNet(Object[] netAndMarking) {
        AcceptingPetriNet net = new AcceptingPetriNetImpl((Petrinet)netAndMarking[0], (Marking) netAndMarking[1], (Marking)netAndMarking[2]);
        for (Transition t : net.getNet().getTransitions()){
            if (t.getLabel().matches("([a-z0-9]+-){4}[a-z0-9]+")){
                t.setInvisible(true);
            }
        }
        return net;
    }

    protected void runTest(String fileName) throws Exception {
        Map<String, EntropyPrecisionRecall> results = new TreeMap<>();
        int counter = 1;
        for (File pnmlFile : pnmlFiles) {
            long startTime = System.currentTimeMillis();
            System.out.println("***********************");
            System.out.println("Computing precision of " + pnmlFile.getName());
            System.out.println("***********************");

            Object[] netAndMarking = TestUtils.loadModel(pnmlFile.getName(), false, parentFolder);
//            Utils.showModel((Petrinet) netAndMarking[0]);
            System.out.println(StochasticNetUtils.debugTrace(log.get(0)));
            if (netAndMarking[2] == null || ((Marking)netAndMarking[2]).isEmpty()){
                netAndMarking[2] = StochasticNetUtils.getFinalMarking(context, (Petrinet) netAndMarking[0]);
                if (((Marking)netAndMarking[2]).isEmpty()){
                    for (Place p : ((Petrinet) netAndMarking[0]).getPlaces()){
                        if (p.getLabel().toLowerCase().endsWith("_end")){
                            ((Marking)netAndMarking[2]).add(p);
                        }
                    }
                }
            }
            AcceptingPetriNet net = getAcceptingPetriNet(netAndMarking);

            EntropyPrecisionRecall precision = PrecisionRecallComputer.getPrecisionAndRecall(context, canceller, log, net, eventClassifier, resultL);
            results.put(pnmlFile.getName(), precision);
            System.out.println("***********************");
            System.out.println("Eigenvalue-based precision of " + pnmlFile.getName() + " is: " + precision.getPrecision()+"\t comp. took: "+(int)((System.currentTimeMillis()-startTime)/1000.)+"s");
            System.out.println("********  Finished "+(counter++)+" of "+pnmlFiles.length+" *********");
            System.out.println("***********************");
        }
        System.out.println("********  DONE  *********");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(EntropyPrecisionRecall.getHeader());
        writer.newLine();
        for (String name : results.keySet()) {
            EntropyPrecisionRecall result = results.get(name);
            writer.write(result.getCSVString());
            writer.newLine();
        }
        writer.flush();
        writer.close();

    }
}
