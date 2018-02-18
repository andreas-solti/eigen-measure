package org.processmining.eigenvalue.test;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.PrecisionRecallComputer;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.provider.LanguagePrecisionRecall;
import org.processmining.eigenvalue.provider.PrecisionProvider;
import org.processmining.eigenvalue.tree.TreeUtils;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tests the BPI challenge logs
 */
public class BPITest extends PrecisionRecallTest {
    @Test
    public void testCanLoadAll() {
        for (XLog log : TestUtils.getBPILogs()){
            System.out.println(Utils.getName(log, "<log>"));
            System.out.println("traces: "+log.size());
            XLogInfo info = XLogInfoFactory.createLogInfo(log);
            System.out.println("events: "+ info.getNumberOfEvents());
            System.out.println("timebounds: "+ info.getLogTimeBoundaries().getStartDate()+" - "+info.getLogTimeBoundaries().getEndDate());
            System.out.println("***************************************");
        }
    }

    @Test
    public void testComputePrecisionRecall() {
        File outFolder = new File(TestUtils.TEST_OUTPUT_FOLDER);
        if (!outFolder.exists()){
            outFolder.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFolder, "real_logs_results.csv")))) {
            writer.write(EntropyPrecisionRecall.getHeader()+"\n");
            for (XLog log : TestUtils.getBPILogs()) {
                try {
                    writer.write(getResultString(log));
                    writer.flush();
                } catch (ProcessTree2Petrinet.NotYetImplementedException e) {
                    e.printStackTrace();
                } catch (ProcessTree2Petrinet.InvalidProcessTreeException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getResultString(XLog log) throws ProcessTree2Petrinet.NotYetImplementedException, ProcessTree2Petrinet.InvalidProcessTreeException {
        ProcessTree model = TreeUtils.mineTree(log);

        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(model, true);
        AcceptingPetriNet net = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        long startTime = System.currentTimeMillis();
        EntropyPrecisionRecall precisionRecall = PrecisionRecallComputer.getPrecisionAndRecall(this.context, Utils.NOT_CANCELLER, log,  net);
        System.out.println("Computing Precision and Recall for "+Utils.getName(log,"<log>")+" took "+((System.currentTimeMillis()-startTime)/1000.)+"s");

        return(precisionRecall.getCSVString());
    }

}
