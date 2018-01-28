package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.PrecisionRecallComputer;

public class LanguagePrecisionRecall extends AbstractPrecision implements RecallProvider {
    @Override
    public String getName() {
        return "precisionEig";
    }

    @Override
    public String getRecallName() {
        return "recallEig";
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        return PrecisionRecallComputer.getPrecisionAndRecall(null, Utils.NOT_CANCELLER, log,  acceptingPetriNet).getPrecision();
    }

    @Override
    public Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        return PrecisionRecallComputer.getPrecisionAndRecall(null, Utils.NOT_CANCELLER, log,  acceptingPetriNet).getRecall();
    }
}
