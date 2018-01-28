package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentParameters;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentPlugin;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class AntiAlignmentPrecision extends AbstractAlignmentPrecisionFitness {

    AntiAlignmentParameters antiAlignmentParameters;
    AntiAlignmentPlugin antiAlignmentPlugin;

    public AntiAlignmentPrecision() {
        this.antiAlignmentParameters = getDefaultAntiAlignmentParameters();
        this.antiAlignmentPlugin = new AntiAlignmentPlugin();
    }

    public AntiAlignmentParameters getDefaultAntiAlignmentParameters() {
        AntiAlignmentParameters parameters = new AntiAlignmentParameters(5, 1.0, 1, 2.0);
        return parameters;
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        PNRepResult alignment = getAlignment(context, acceptingPetriNet, log, classifier, mapping);
        PNRepResult antiAlignmentResult = antiAlignmentPlugin.basicCodeStructureWithAlignments(context.getProgress(), acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking(), acceptingPetriNet.getFinalMarkings().iterator().next(), log, alignment, mapping, antiAlignmentParameters);
        return Double.parseDouble(String.valueOf(antiAlignmentResult.getInfo().get("Precision")));
    }

    @Override
    protected Double getFitness(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        throw new UnsupportedOperationException("There is no anti-alignment based fitness!");
    }

    @Override
    public String getName() {
        return "antiAlignPrecision";
    }

    @Override
    public String getRecallName() {
        return null;
    }
}
