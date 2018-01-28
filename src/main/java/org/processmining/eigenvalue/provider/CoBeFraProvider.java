package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;

public abstract class CoBeFraProvider extends AbstractPrecision implements RecallProvider{


    public Double getRecall(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier){
        return getRecall(context, getMapping(acceptingPetriNet, log), acceptingPetriNet, log, classifier);
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        return getPrecision(context, getMapping(acceptingPetriNet, log), acceptingPetriNet, log, classifier);
    }

    public static Mapping getMapping(AcceptingPetriNet acceptingPetriNet, XLog log) {
        Mapping mapping = new MemoryMapping(log, acceptingPetriNet);
        mapping.getPetrinetWithMarking()[1] = acceptingPetriNet.getInitialMarking();
        return mapping;
    }

    protected abstract Double getPrecision(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier);
    protected abstract Double getRecall(UIPluginContext context, Mapping mapping, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier);
}
