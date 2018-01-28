package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.AdvancedBehaviouralAppropriateness;
import be.kuleuven.econ.cbf.metrics.recall.ProperCompletion;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public class KUAdvancedBehavioralAppropriateness extends CoBeFraProvider {
    @Override
    protected Double getPrecision(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        AdvancedBehaviouralAppropriateness behaviouralAppropriateness = new AdvancedBehaviouralAppropriateness();

        behaviouralAppropriateness.load(mapping);
        behaviouralAppropriateness.calculate();
        return behaviouralAppropriateness.getResult();
    }

    @Override
    protected Double getRecall(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        ProperCompletion properCompletion = new ProperCompletion();
        properCompletion.load(mapping);
        properCompletion.calculate();
        return properCompletion.getResult();
    }

    @Override
    public String getName() {
        return "advBehAppropriateness";
    }

    @Override
    public String getRecallName() {
        return "properCompletion";
    }
}
