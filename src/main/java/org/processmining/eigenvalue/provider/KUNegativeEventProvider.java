package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.NegativeEventPrecisionMetric;
import be.kuleuven.econ.cbf.metrics.recall.NegativeEventRecallMetric;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public class KUNegativeEventProvider extends CoBeFraProvider
{
    @Override
    public String getName() {
        return "negativeEventPrecision";
    }

    @Override
    protected Double getPrecision(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        NegativeEventPrecisionMetric negativeEventPrecisionMetric = new NegativeEventPrecisionMetric();
        negativeEventPrecisionMetric.load(mapping);
        negativeEventPrecisionMetric.calculate();
        return negativeEventPrecisionMetric.getResult();
    }

    @Override
    protected Double getRecall(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        NegativeEventRecallMetric negativeEventRecallMetric = new NegativeEventRecallMetric();
        negativeEventRecallMetric.load(mapping);
        negativeEventRecallMetric.calculate();
        return negativeEventRecallMetric.getResult();
    }

    @Override
    public String getRecallName() {
        return "negativeEventRecall";
    }
}
