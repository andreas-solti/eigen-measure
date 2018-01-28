package org.processmining.eigenvalue.test;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.generator.GenerateLogAndModel;
import org.processmining.eigenvalue.provider.KUAdvancedBehavioralAppropriateness;
import org.processmining.eigenvalue.provider.LanguagePrecisionRecall;
import org.processmining.eigenvalue.provider.ParsingMeasure;
import org.processmining.eigenvalue.provider.RecallProvider;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.util.Collection;

public class TestNetInvisibility extends PrecisionRecallTest {

    public static final XEventClassifier CLASSIFIER = XLogInfoImpl.NAME_CLASSIFIER;

    private UIPluginContext context;

    @Test
    public void testChangeInvisibility() throws Exception {
        NAryTree tree = TreeUtils.fromString("SEQ( LOOP( LEAF: 0 , LEAF: 1 , LEAF: 2 ) , LEAF: 3 ) [ ]");
        XEventClasses eventClasses = TestUtils.getxEventClasses(CLASSIFIER, 4);

        ProcessTree processTree = NAryTreeToProcessTree.convert(tree, eventClasses);
        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree, true);
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : petrinetWithMarkings.petrinet.getTransitions()){
            if(t.getLabel().startsWith("tau ")){
                t.setInvisible(true);
            }
        }
        AcceptingPetriNet acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        XLog log = GenerateLogAndModel.generateLog(tree, 100, eventClasses);

        Assert.assertTrue(TestUtils.hasInvisibleTransitions(acceptingPetriNet.getNet().getTransitions()));

        KUAdvancedBehavioralAppropriateness advancedBehavioralAppropriateness = new KUAdvancedBehavioralAppropriateness();

        Double recall = advancedBehavioralAppropriateness.getRecall(context, acceptingPetriNet, log, CLASSIFIER);
        System.out.println("Advanced behavioral appropriateness recall: "+recall);

        Assert.assertTrue(TestUtils.hasInvisibleTransitions(acceptingPetriNet.getNet().getTransitions()));

        ParsingMeasure parsingMeasure = new ParsingMeasure();

        Double parsingMeasureRecall = parsingMeasure.getRecall(context, acceptingPetriNet, log, CLASSIFIER);
        System.out.println("Parsing measure recall: "+parsingMeasureRecall);

        LanguagePrecisionRecall eigProvider = new LanguagePrecisionRecall();

        Double eigRecall = eigProvider.getRecall(context, acceptingPetriNet, log, CLASSIFIER);
        System.out.println("eigenvalue recall: "+eigRecall);

    }


}
