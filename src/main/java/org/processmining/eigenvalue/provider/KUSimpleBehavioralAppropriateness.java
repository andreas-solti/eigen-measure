package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.SimpleBehaviouralAppropriateness;
import be.kuleuven.econ.cbf.metrics.recall.RozinatFitness;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public class KUSimpleBehavioralAppropriateness extends CoBeFraProvider {
    @Override
    protected Double getPrecision(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        SimpleBehaviouralAppropriateness behaviouralAppropriateness = new SimpleBehaviouralAppropriateness();

        behaviouralAppropriateness.load(mapping);
        behaviouralAppropriateness.calculate();
        return behaviouralAppropriateness.getResult();
    }

    @Override
    protected Double getRecall(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        RozinatFitness fitness = new RozinatFitness();
        fitness.load(mapping);
        fitness.calculate();
        return fitness.getResult();
    }

    @Override
    public String getName() {
        return "simpleBehAppropriateness";
    }

    @Override
    public String getRecallName() {
        return "tokenBasedFitness";
    }
}
