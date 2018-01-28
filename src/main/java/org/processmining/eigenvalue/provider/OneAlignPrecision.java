package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.plugins.alignetc.core.ReplayAutomaton;
import org.processmining.plugins.alignetc.result.AlignETCResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public class OneAlignPrecision extends AbstractAlignmentPrecisionFitness {

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        PNRepResult alignment = getAlignment(context, acceptingPetriNet, log, classifier, mapping);

        //Convert to n-alignments object
        double precision = 0;
        if(alignment!=null){
            Collection<AllSyncReplayResult> col = new ArrayList<AllSyncReplayResult>();
            for (SyncReplayResult rep : alignment) {

                //Get all the attributes of the 1-alignment result
                List<List<Object>> nodes = new ArrayList<List<Object>>();
                nodes.add(rep.getNodeInstance());

                List<List<StepTypes>> types = new ArrayList<List<StepTypes>>();
                types.add(rep.getStepTypes());

                SortedSet<Integer> traces = rep.getTraceIndex();
                boolean rel = rep.isReliable();

                //Create a n-alignment result with this attributes
                AllSyncReplayResult allRep = new AllSyncReplayResult(nodes, types, -1, rel);
                allRep.setTraceIndex(traces);//The creator not allow add the set directly
                col.add(allRep);
            }
            PNMatchInstancesRepResult alignments = new PNMatchInstancesRepResult(col);

            AlignETCResult res = new AlignETCResult();

            ReplayAutomaton ra = null;
            try {
                ra = new ReplayAutomaton(context, alignments, acceptingPetriNet.getNet());
            } catch (ConnectionCannotBeObtained e1) {
                e1.printStackTrace();
            }
            ra.cut(res.escTh);

            try {
                ra.extend(acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking());
            } catch (IllegalTransitionException e1) {
                e1.printStackTrace();
            }
            ra.conformance(res);
            precision = res.ap;
        }
        return precision;
    }

    @Override
    protected Double getFitness(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        throw new UnsupportedOperationException("OneAlignFitness does not exits");
    }

    @Override
    public String getName() {
        return "oneAlignPrecision";
    }

    @Override
    public String getRecallName() {
        return null;
    }
}
