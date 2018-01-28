package org.processmining.eigenvalue.provider;

import nl.tue.astar.AStarException;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNPartialOrderAwareReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAlignmentPrecisionFitness extends AbstractPrecision implements RecallProvider{

    @Override
    public final Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        TransEvClassMapping mapping = StochasticNetUtils.getEvClassMapping(acceptingPetriNet.getNet(), log, classifier);
        return getPrecision(context, acceptingPetriNet, log, classifier, mapping);
    }

    protected abstract Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping);

    @Override
    public Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        TransEvClassMapping mapping = StochasticNetUtils.getEvClassMapping(acceptingPetriNet.getNet(), log, classifier);
        return getFitness(context, acceptingPetriNet, log, classifier, mapping);
    }
    protected abstract Double getFitness(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping);


    public PNRepResult getAlignment(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping){
        IPNReplayParameter parameters = getParameters(acceptingPetriNet, log, classifier);
        IPNPartialOrderAwareReplayAlgorithm algorithm = new PetrinetReplayerWithILP();
        PNLogReplayer replayer1 = new PNLogReplayer();

        PNRepResult alignment = null;
        try {
            alignment = replayer1.replayLog(context, acceptingPetriNet.getNet(), log, mapping, algorithm, parameters);
        } catch (AStarException e) {
            e.printStackTrace();
        }
        return alignment;
    }

    public IPNReplayParameter getParameters(AcceptingPetriNet net, XLog log, XEventClassifier classifier){
        IPNReplayParameter parameters = new CostBasedCompleteParam(constructMOTCostFunction(log, classifier), constructMOSCostFunction(net.getNet()));
        parameters.setInitialMarking(net.getInitialMarking());
        parameters.setFinalMarkings(net.getFinalMarkings().toArray(new Marking[net.getFinalMarkings().size()]));
        parameters.setGUIMode(false);
        parameters.setCreateConn(false);
        parameters.setNumThreads(1);
        ((CostBasedCompleteParam) parameters).setMaxNumOfStates(1000 * 1000);
        return parameters;
    }

    protected Map<Transition, Integer> constructMOSCostFunction(PetrinetGraph net) {
        Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();
        for (Transition t : net.getTransitions())
            if (t.isInvisible())
                costMOS.put(t, 0);
            else
                costMOS.put(t, 1);

        return costMOS;
    }

    protected Map<XEventClass, Integer> constructMOTCostFunction(XLog log, XEventClassifier eventClassifier) {
        Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

        for (XEventClass evClass : summary.getEventClasses().getClasses()) {
            costMOT.put(evClass, 1);
        }
        costMOT.put(EvClassLogPetrinetConnectionFactoryUI.DUMMY,0);

        return costMOT;
    }
}
