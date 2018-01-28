package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

/**
 * Handles conversion into petri nets for other models.
 */
public abstract class AbstractPrecision implements PrecisionProvider {

    public final Double getPrecision(UIPluginContext context, ProcessTree processTree, XLog log, XEventClassifier classifier){
        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = null;
        try {
            petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree, true);
            AcceptingPetriNet acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);
            return getPrecision(context, acceptingPetriNet, log, classifier);
        } catch (ProcessTree2Petrinet.NotYetImplementedException e) {
            e.printStackTrace();
        } catch (ProcessTree2Petrinet.InvalidProcessTreeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final Double getPrecision(UIPluginContext context, NAryTree nAryTree, XLog log, XEventClassifier classifier, XEventClasses eventClasses){
        ProcessTree processTree = NAryTreeToProcessTree.convert(nAryTree, eventClasses);
        return getPrecision(context, processTree, log, classifier);
    }
}
