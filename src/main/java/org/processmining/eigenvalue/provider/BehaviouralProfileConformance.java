package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.metrics.recall.BehavioralConformance;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public class BehaviouralProfileConformance implements RecallProvider {
    @Override
    public String getRecallName() {
        return "behaviouralProfileConformance";
    }

    @Override
    public Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        BehavioralConformance conformance = new BehavioralConformance();
        conformance.load(CoBeFraProvider.getMapping(acceptingPetriNet, log));
        conformance.calculate();
        return conformance.getResult();
    }
}
