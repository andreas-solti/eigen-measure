package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public interface RecallProvider {
    /**
     * @return The name of the recall / fitness measure.
     */
    String getRecallName();

    /**
     * Returns the recall for a net, a log and a classifier
     * @param context ProM context ( A dummy context can be used )
     * @param acceptingPetriNet the Petri net reflecting the model
     * @param log the log to measure fitness/recall for
     * @param classifier the classifier tying the model to the log
     * @return a single recall value.
     */
    Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier);
}
