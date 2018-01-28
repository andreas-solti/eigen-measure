package org.processmining.eigenvalue.provider;

import dk.brics.automaton2.Automaton;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.Utils;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.projectedrecallandprecision.helperclasses.AcceptingPetriNet2automaton;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.helperclasses.ProjectPetriNetOntoActivities;

import java.util.HashSet;
import java.util.Set;

public class ParsingMeasure implements RecallProvider {
    @Override
    public String getRecallName() {
        return "parsingMeasure";
    }

    public static String[] getTransitionNames(AcceptingPetriNet net, String[] activities) {
        Set<String> transitionNames = new HashSet<>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : net.getNet().getTransitions()) {
            if (!t.isInvisible() && t.getLabel() != null) {
                transitionNames.add(t.getLabel());
            }
        }
        for (int i = 0; i<activities.length; i++)
            transitionNames.add(activities[i]);
        return transitionNames.toArray(new String[transitionNames.size()]);
    }

    @Override
    public Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        EfficientLog efficientLog = new EfficientLog(log, classifier);
        String[] activities = efficientLog.getActivities();
        short[] actKeys = efficientLog.getProjectionKey(activities);
        int parsedTraces = 0;
        int notParsedTraces = 0;
        try {
            for (Transition t : acceptingPetriNet.getNet().getTransitions()){
                if(t.getLabel().startsWith("tau ")){
                    t.setInvisible(true);
                }
            }

            String[] names = getTransitionNames(acceptingPetriNet, activities);
            AcceptingPetriNet projectedNet = ProjectPetriNetOntoActivities.project(acceptingPetriNet, Utils.NOT_CANCELLER, names);

            Automaton automaton = AcceptingPetriNet2automaton.convert(projectedNet, 500000, Utils.NOT_CANCELLER);
            for (int i = 0; i < efficientLog.size(); i++){
                short[] trace = efficientLog.getProjectedTrace(i, actKeys);
                if (automaton.run(trace)){
                    parsedTraces += 1;
                } else {
                    notParsedTraces += 1;
                }
            }
        } catch (AutomatonFailedException e) {
            e.printStackTrace();
        }
        return parsedTraces / (double)(parsedTraces+notParsedTraces);
    }
}
