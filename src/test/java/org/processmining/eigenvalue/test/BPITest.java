package org.processmining.eigenvalue.test;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.eigenvalue.provider.LanguagePrecisionRecall;
import org.processmining.eigenvalue.provider.PrecisionProvider;
import org.processmining.eigenvalue.tree.TreeUtils;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

/**
 * Tests the BPI challenge logs
 */
public class BPITest extends PrecisionRecallTest {
    @Test
    public void testBPI2017() throws Exception {
        XLog bpiLog = TestImport.getBPILogs().iterator().next();

        ProcessTree model = TreeUtils.mineTree(bpiLog);

        PrecisionProvider provider = new LanguagePrecisionRecall();

        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(model, true);
        AcceptingPetriNet net = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        long startTime = System.currentTimeMillis();
        Double recall = provider.getPrecision(this.context, net, bpiLog, MiningParameters.getDefaultClassifier());

        System.out.println("Computing Precision took "+(System.currentTimeMillis()-startTime)+"ms");

    }
}
