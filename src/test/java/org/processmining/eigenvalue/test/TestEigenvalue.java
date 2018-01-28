package org.processmining.eigenvalue.test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.junit.Before;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.eigenvalue.generator.GenerateLogAndModel;
import org.processmining.eigenvalue.generator.NAryTreeGenerator;
import org.processmining.eigenvalue.generator.NoiseInserter;
import org.processmining.eigenvalue.provider.*;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TestEigenvalue extends PrecisionRecallTest{

    public static final int ALPHABET_SIZE = 9;
    public static final int LOG_SIZE = 100;
    public static final XEventClassifier CLASSIFIER = XLogInfoImpl.NAME_CLASSIFIER;




    @Test
    public void runRecallsWithGeneratedModels() throws Exception {
        NAryTreeGenerator generator = new NAryTreeGenerator();
        int alphabetSize = 7;
        NAryTree tree = generator.generate(alphabetSize);
        tree.setType(0,NAryTree.SEQ);
        System.out.println(tree);
        XEventClasses eventClasses = TestUtils.getxEventClasses(CLASSIFIER, alphabetSize);

        ProcessTree processTree = NAryTreeToProcessTree.convert(tree, eventClasses);
        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree, true);
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : petrinetWithMarkings.petrinet.getTransitions()){
            if(t.getLabel().startsWith("tau ")){
                t.setInvisible(true);
            }
        }
        AcceptingPetriNet acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        XLog fittingCompleteLog = GenerateLogAndModel.generateLog(tree, LOG_SIZE, eventClasses);

        Table<Integer, Integer, List<PrecisionResult>> resultTable = HashBasedTable.create();
        int chaos = 20; // 20% of events in a noisy trace get corrupted

        // compute recall and add noise successively:
        for (int noise = 0; noise <= 95; noise+=5){
            NoiseInserter inserter = new NoiseInserter((noise / 100.), (chaos/100.));
            XLog noisyLog = inserter.insertNoise(fittingCompleteLog);
            resultTable.put(noise,chaos, new ArrayList<PrecisionResult>());
            for (RecallProvider provider : recallProviders) {
                long now = System.currentTimeMillis();
                Double recall = provider.getRecall(context, acceptingPetriNet, noisyLog, CLASSIFIER);
                resultTable.get(noise, chaos).add(new PrecisionResult(recall, provider.getRecallName(), System.currentTimeMillis() - now));
            }
        }
        writeResultMatrix(resultTable, "recalls_artificial.csv","noiseRatio","chaosInTrace", recallProviders, (Object p) -> ((RecallProvider) p).getRecallName());
    }


    private void writeResultMatrix(Table<Integer, Integer, List<PrecisionResult>> resultTable, String fileName, String rowName, String colName){
        writeResultMatrix(resultTable, fileName, rowName, colName, precisionProviders, o -> ((PrecisionProvider)o).getName());
    }

    /**
     * Writes the results into a comma separated matrix
     * @param resultTable a table containing the results
     * @param fileName the name of the output csv file
     * @param rowName the name of the rows in the matrix
     * @param colName the name of the columns in the matrix
     * @param columns
     */
    private void writeResultMatrix(Table<Integer, Integer, List<PrecisionResult>> resultTable, String fileName, String rowName, String colName, Object[] columns, Function<Object, String> mapper){
        System.out.println("********  DONE  *********");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            //writer.write(EntropyPrecisionRecall.getHeader());

            writer.write(rowName);
            if (colName != null) {
                writer.write(EntropyResult.SEPARATOR);
                writer.write(colName);
            }

            for (Object column : columns){
                String name = mapper.apply(column);
                writer.write(EntropyResult.SEPARATOR);
                writer.write(name);
                writer.write(EntropyResult.SEPARATOR);
                writer.write(name+"_time");
            }

            writer.newLine();

            for (Integer c : resultTable.columnKeySet()){
                Map<Integer, List<PrecisionResult>> column = resultTable.column(c);
                for (Integer r : column.keySet()){
                    writer.write(String.valueOf(r)); // i = fullLog

                    if (colName!=null) {
                        writer.write(EntropyResult.SEPARATOR);
                        writer.write(String.valueOf(c)); // j = sublog
                    }

                    List<PrecisionResult> precisionResults = column.get(r);
                    for (PrecisionResult precRes : precisionResults){
                        writer.write(EntropyResult.SEPARATOR);
                        writer.write(String.valueOf(precRes.getPrecision())); // precision
                        writer.write(EntropyResult.SEPARATOR);
                        writer.write(String.valueOf(precRes.getMillis())); // time
                    }
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class PrecisionResult {
        private double precision;
        private String name;
        private long millis;

        public PrecisionResult(Double precision, String name, long millis) {
            this.precision = precision;
            this.name = name.intern();
            this.millis = millis;
        }

        public double getPrecision() {
            return precision;
        }
        public String getName() {
            return name;
        }
        public long getMillis() {
            return millis;
        }
    }
}
