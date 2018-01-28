package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public interface PrecisionProvider {

    /**
     * @return The name of the precision measure.
     */
    String getName();

    /**
     * Returns the precision for a net, a log and a classifier
     * @param context Prom context ( A dummy context can be used )
     * @param acceptingPetriNet the petri net reflecting the model
     * @param log the log to measure precision for
     * @param classifier the classifier tying the model to the log
     * @return a single precision value.
     */
    Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier);
}
