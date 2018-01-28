package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.alignment.override.PrecisionAligner;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

public class AlignmentPrecisionFitness extends AbstractAlignmentPrecisionFitness {
    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        PNRepResult alignment = getAlignment(context, acceptingPetriNet, log, classifier, mapping);

        PrecisionAligner aligner = new PrecisionAligner();
        AlignmentPrecGenRes precisionResult = aligner.measureConformanceAssumingCorrectAlignment(context, mapping, alignment, acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking(), false);
        return precisionResult.getPrecision();
    }

    @Override
    protected Double getFitness(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier, TransEvClassMapping mapping) {
        PNRepResult alignment = getAlignment(context, acceptingPetriNet, log, classifier, mapping);
        return Double.parseDouble(String.valueOf(alignment.getInfo().get("Trace Fitness")));
    }

    @Override
    public String getName() {
        return "alignmentPrecision";
    }

    @Override
    public String getRecallName() {
        return "alignmentFitness";
    }


}
