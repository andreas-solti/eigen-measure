package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.ETConformanceBestAlign;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsGraphILPAlg;

public class BestAlignProvider extends CoBeFraProvider {
    @Override
    public String getName() {
        return "bestAlignPrecision";
    }

    @Override
    protected Double getPrecision(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        ETConformanceBestAlign bestAlign = new ETConformanceBestAlign();

        bestAlign.load(mapping);
        bestAlign.setChosenAlgorithm(new AllOptAlignmentsGraphILPAlg());
        bestAlign.calculate();
        return bestAlign.getResult();
    }

    @Override
    protected Double getRecall(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        return null;
    }

    @Override
    public String getRecallName() {
        return null;
    }
}
