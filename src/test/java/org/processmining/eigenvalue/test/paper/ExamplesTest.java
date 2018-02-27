package org.processmining.eigenvalue.test.paper;

import com.google.common.base.Joiner;
import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.State;
import dk.brics.automaton2.Transition;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.PrecisionRecallComputer;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.eigenvalue.test.TestUtils;

import javax.rmi.CORBA.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Creates examples used in the publication "Quotients for Behavioural Comparison -
 *                                          Framework and Applications in Process Mining"
 *
 * Stores the examples in {@link TestUtils#TEST_OUTPUT_FOLDER}.
 */
public class ExamplesTest {

    public static Map<Character, Character> charMap = new HashMap<>();
    static{
        charMap.put('a',"\u0000".charAt(0));
        charMap.put('b',"\u0001".charAt(0));
        charMap.put('c',"\u0002".charAt(0));
        charMap.put('d',"\u0003".charAt(0));
        charMap.put('e',"\u0004".charAt(0));
        charMap.put('f',"\u0005".charAt(0));
    }

    public static void wire(State from, char label, State to){
        from.addTransition(new Transition(charMap.get(label),to));
    }

    public static Automaton getABC() {
        Automaton a = new Automaton();
        State sI = new State();
        State sII = new State();
        State sIII = new State();
        State sIV = new State();
        a.setInitialState(sI);
        sIV.setAccept(true);
        wire(sI, 'a', sII);
        wire(sII, 'b', sIII);
        wire(sIII, 'c', sIV);
        return a;
    }

    public static Automaton getABC_D() {
        Automaton a = new Automaton();
        State sI = new State();
        State sII = new State();
        State sIII = new State();
        State sIV = new State();
        State sV = new State();
        a.setInitialState(sI);
        sIV.setAccept(true);
        sV.setAccept(true);
        wire(sI, 'a', sII);
        wire(sII, 'b', sIII);
        wire(sIII, 'c', sIV);
        wire(sIV, 'd', sV);
        return a;
    }

    public static Automaton getABC_D_or_E() {
        Automaton a = new Automaton();
        State sI = new State();
        State sII = new State();
        State sIII = new State();
        State sIV = new State();
        State sV = new State();
        State sVI = new State();
        a.setInitialState(sI);
        sIV.setAccept(true);
        sV.setAccept(true);
        sVI.setAccept(true);
        wire(sI, 'a', sII);
        wire(sII, 'b', sIII);
        wire(sIII, 'c', sIV);
        wire(sIV, 'd', sV);
        wire(sIV, 'e', sVI);
        return a;
    }

    public static Automaton getS1() {
        Automaton a = new Automaton();
        State sI = new State();
        State sII = new State();
        State sIII = new State();
        State sIV = new State();
        a.setInitialState(sI);
        sI.setAccept(true);
        wire(sI, 'a', sII);
        wire(sII, 'b', sII);
        wire(sII, 'c', sII);
        wire(sII, 'f', sIII);
        wire(sIII, 'e', sI);
        wire(sII, 'd', sIV);
        wire(sIV, 'e', sI);
        return a;
    }

    public static Automaton getS2() {
        Automaton a = new Automaton();
        State sA = new State();
        State sB = new State();
        State sC = new State();
        State sD = new State();
        State sE = new State();
        a.setInitialState(sA);
        sA.setAccept(true);
        wire(sA, 'a', sB);
        wire(sB, 'b', sC);
        wire(sC, 'c', sB);
        wire(sB, 'b', sD);
        wire(sD, 'd', sE);
        wire(sE, 'e', sA);
        return a;
    }

    public static Automaton getS3() {
        Automaton a = new Automaton();
        State s1 = new State();
        State s2 = new State();
        State s3 = new State();
        State s4 = new State();
        State s5 = new State();
        State s6 = new State();
        a.setInitialState(s1);
        s6.setAccept(true);
        wire(s1, 'a', s2);
        wire(s2, 'b', s3);
        wire(s3, 'c', s4);
        wire(s3, 'd', s5);
        wire(s4, 'd', s5);
        wire(s5, 'e', s6);
        return a;
    }

    public static Automaton getS4() {
        Automaton a = new Automaton();
        State sF = new State();
        State sG = new State();
        State sH = new State();
        State sI = new State();
        a.setInitialState(sF);
        sF.setAccept(true);
        wire(sF, 'a', sG);
        wire(sG, 'b', sH);
        wire(sH, 'c', sG);
        wire(sH, 'd', sI);
        wire(sI, 'e', sF);
        return a;
    }

    public static Automaton getS5() {
        Automaton a = new Automaton();
        State s1 = new State();
        State s2 = new State();
        State s3 = new State();
        State s4 = new State();
        State s5 = new State();
        State s6 = new State();
        a.setInitialState(s1);
        s1.setAccept(true);
        wire(s1, 'a', s2);
        wire(s2, 'b', s3);
        wire(s3, 'c', s4);
        wire(s4, 'b', s5);
        wire(s5, 'c', s4);
        wire(s5, 'd', s6);
        wire(s6, 'e', s1);
        return a;
    }

    public static Automaton getXStar() {
        Automaton a = new Automaton();
        State s1 = new State();
        s1.setAccept(true);
        a.setInitialState(s1);
        wire(s1, 'a', s1);
        wire(s1, 'b', s1);
        wire(s1, 'c', s1);
        wire(s1, 'd', s1);
        wire(s1, 'e', s1);
        return a;
    }

    public static Automaton getL1() {
        return TestUtils.getLogAutomaton("abde", "abcbcde");
    }

    public static Automaton getL2() {
        return TestUtils.getLogAutomaton("abde","abcbcde","abccde","afe","afe");
    }

    public static Automaton getL3() {
        return TestUtils.getLogAutomaton("abcbcde", "abbf", "afe");
    }

    @Test
    public void testS1() {
        TestUtils.outputPNG(getS1(),"S1_orig");
    }

    @Test
    public void testS1L1() {
        String mName = "S1";
        String lName = "L1";

        Automaton aM = getS1();
        Automaton aL = getL1();


        getPrecisionAndRecall(mName, lName, aM, aL);
    }

    @Test
    public void testS1L2() {
        String mName = "S1";
        String lName = "L2";

        Automaton aM = getS1();
        Automaton aL = getL2();


        getPrecisionAndRecall(mName, lName, aM, aL);
    }

    @Test
    public void testS1L3() {
        String mName = "S1";
        String lName = "L3";

        Automaton aM = getS1();
        Automaton aL = getL3();

        Automaton aMaL = aM.intersection(aL, Utils.NOT_CANCELLER);
        aMaL.minimize(Utils.NOT_CANCELLER);
        TestUtils.outputPNG(aMaL, "S1_intersect_L3");

        getPrecisionAndRecall(mName, lName, aM, aL);
    }

    @Test
    public void testRelations() {
        // check if S5 in S4 in S1

        // check S5 in S4:
        Automaton s1 = getS1();
        Automaton s4 = getS4();
        Automaton s5 = getS5();

        // s4 in s1, if intersection = s4
        Automaton s1s4 = s1.intersection(s4, Utils.NOT_CANCELLER);
        Assert.assertEquals(s1s4, s4);
        Assert.assertEquals(s1s4.getNumberOfStates(), s4.getNumberOfStates());

        // s5 in s4 -> s4 cap s5 = s5
        Automaton s4s5 = s4.intersection(s5, Utils.NOT_CANCELLER);
        Assert.assertEquals(s4s5, s5);

        // s5 in s4 in s1 -> s1 cap s5 = s5
        Automaton s1s5 = s1.intersection(s5, Utils.NOT_CANCELLER);
        Assert.assertEquals(s1s5, s5);
    }

    @Test
    public void testQuotientsSection4() {
        Automaton s1 = getS1();
        Automaton s4 = getS4();
        Automaton s5 = getS5();

        Automaton sX = getXStar();

        EntropyResult resultS1 = PrecisionRecallComputer.getResult("S1", s1.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s1, "s1", Utils.NOT_CANCELLER, 0.0));
        EntropyResult resultS4 = PrecisionRecallComputer.getResult("S4", s4.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s4, "s4", Utils.NOT_CANCELLER, 0.0));
        EntropyResult resultS5 = PrecisionRecallComputer.getResult("S5", s5.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s5, "s5", Utils.NOT_CANCELLER, 0.0));

        EntropyResult resultSX = PrecisionRecallComputer.getResult("X", sX.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(sX, "X", Utils.NOT_CANCELLER, 0.0));

        double u = resultS4.largestEigenvalue/resultS1.largestEigenvalue;
        double v = resultS5.largestEigenvalue/resultS1.largestEigenvalue;
        double w = resultS5.largestEigenvalue/resultS4.largestEigenvalue;

        double x = resultS5.largestEigenvalue/resultSX.largestEigenvalue;

        System.out.println("u = "+u);
        System.out.println("v = "+v);
        System.out.println("w = "+w);

        System.out.println("x = "+x);

        Assert.assertTrue("v smaller than w (Lemma 4.2)", v < w);
        Assert.assertTrue("v smaller than u (Lemma 4.3)", v < u);
        Assert.assertTrue("x smaller than w (Lemma 4.3)", x < w);

        System.out.println("X = "+resultS4.largestEigenvalue);
        System.out.println("Y = "+resultS5.largestEigenvalue);

        toCSV(s4, "S4");
        toCSV(s5, "S5");
    }

    @Test
    public void testLittleRecallExperiment() {
        Automaton sAbc = getABC();
        Automaton lAbcd = getABC_D();
        Automaton lAbcd_or_e = getABC_D_or_E();

        Automaton sl1 = sAbc.intersection(lAbcd, Utils.NOT_CANCELLER);
        Automaton sl2 = sAbc.intersection(lAbcd_or_e, Utils.NOT_CANCELLER);


        printResult(sAbc, "abc", lAbcd, "abc_d", 3/5.);

        printResult(sAbc, "abc", lAbcd_or_e, "abc_d_or_e", 3/5.);



        EntropyPrecisionRecall result2 = PrecisionRecallComputer.getPrecisionAndRecall(sAbc, "abc", lAbcd, "abc_d", sl1, 3/5., Utils.NOT_CANCELLER);
    }

    private void printResult(Automaton m, String mName, Automaton l, String lName, double fittingTracesFraction) {
        EntropyPrecisionRecall result = PrecisionRecallComputer.getPrecisionAndRecall(m, mName, l, lName, m.intersection(l, Utils.NOT_CANCELLER), fittingTracesFraction, Utils.NOT_CANCELLER);
        System.out.println("---------------------------------");
        System.out.println("Results of "+mName+" and "+lName);
        System.out.println("Precision: "+result.getPrecision());
        System.out.println("Recall: "+result.getRecall());
        System.out.println("Largest Eigenvalue LM: "+result.getLogModelResult().largestEigenvalue);
    }

    @Test
    public void testCombinations() throws IOException {
        Map<String,Automaton> models = new TreeMap<>();
        Map<String,Automaton> logs = new TreeMap<>();

        models.put("S1", getS1());
        models.put("S2", getS2());
        models.put("S3", getS3());

        logs.put("L1", getL1());
        logs.put("L2", getL2());
        logs.put("L3", getL3());

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(TestUtils.TEST_OUTPUT_FOLDER,"example_results.csv")));

        writer.write("Model;Log;Recall;Precision\n");

        //Table<String, String, EntropyPrecisionRecall> results = HashBasedTable.create();
        for (String model : models.keySet()){
            for (String log : logs.keySet()){
                EntropyPrecisionRecall result = getPrecisionAndRecall(model, log, models.get(model), logs.get(log));
                writer.write(Joiner.on(";").join(new Object[]{model, log, result.getRecall(), result.getPrecision()})+"\n");
                //results.put(model, log, result);
            }
        }
        writer.close();
    }

    @Test
    public void testS1Matrix() {
        toCSV(getS1(), "S1");
    }

    @Test
    public void testS3Matrix() {
        toCSV(getS3(), "S3");
    }

    @Test
    public void testL3Matrix() {
        toCSV(getL3(), "L3");
    }


    private void toCSV(Automaton a, String name) {
        a.setDeterministic(false);
        a.determinize(Utils.NOT_CANCELLER);
        a.minimize(Utils.NOT_CANCELLER);
        TestUtils.outputPNG(a,name+"_matrix");

        CompColMatrix matrix = TopologicalEntropyComputer.getCompressedSparseMatrix(a).getMatrix();

        EntropyResult topologicalEntropy = TopologicalEntropyComputer.getTopologicalEntropy(a, name, Utils.NOT_CANCELLER);

        String filename = name+"_matrix.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(TestUtils.TEST_OUTPUT_FOLDER, filename)))) {
            writer.write("largest Eigenvalue:;"+topologicalEntropy.largestEigenvalue+"\n");

            writer.write("Adjacency Matrix:\n");

            writer.write(";to "); // first value is the row
            writer.write(Joiner.on(";to ").join(IntStream.range(0, matrix.numColumns()).iterator()));
            writer.write("\n");
            for (int row = 0; row < matrix.numRows(); row++){
                writer.write("from "+row+":");
                for (int col = 0; col < matrix.numColumns(); col++){
                    writer.write(";"+ matrix.get(col,row));
                }
                writer.write("\n");
            }


        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private EntropyPrecisionRecall getPrecisionAndRecall(String mName, String lName, Automaton aM, Automaton aL) {
        TestUtils.outputPNG(aM, mName);

        TestUtils.outputPNG(aL, lName);

        Automaton aLM = aM.intersection(aL, Utils.NOT_CANCELLER);

        TestUtils.outputPNG(aL, lName+"_intersect_"+mName);

        EntropyPrecisionRecall result = PrecisionRecallComputer.getPrecisionAndRecall(aM, mName, aL, lName, aLM, 1.0, Utils.NOT_CANCELLER);
        System.out.println("---------------------------------");
        System.out.println("Results of "+mName+" and "+lName);
        System.out.println("Precision: "+result.getPrecision());
        System.out.println("Recall: "+result.getRecall());
        System.out.println("Largest Eigenvalue LM: "+result.getLogModelResult().largestEigenvalue);
        return result;
    }

    public static EntropyResult getTopologicalEntropy(Automaton a, String name) {
        return TopologicalEntropyComputer.getTopologicalEntropy(a, name, Utils.NOT_CANCELLER);
    }
}
