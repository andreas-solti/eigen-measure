package org.processmining.eigenvalue.test.paper;

import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.State;
import dk.brics.automaton2.Transition;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.test.TestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutomatonTest {

    @Test
    public void testAutomaton1c() throws IOException {
        Automaton a = new Automaton();
        a.incorporateTrace(new short[]{}, Utils.NOT_CANCELLER);
        a.incorporateTrace(new short[]{0,1,3}, Utils.NOT_CANCELLER);

        List<State> states = new ArrayList<>(a.getStates());

        State sA = states.get(0);
        State sC = new State();
        State sB = states.get(1);
        sB.addTransition(new Transition("\u0001".charAt(0),sC));
        sC.addTransition(new Transition("\u0002".charAt(0),sB));

        State sE = states.get(3);
        sE.setAccept(false);
        sE.addTransition(new Transition("\u0004".charAt(0),sA));

        Assert.assertEquals(5,a.getNumberOfStates());
        Assert.assertEquals(6,a.getNumberOfTransitions());

        TestUtils.outputPNG(a, "fig1c");
        a.setDeterministic(false);
        a.determinize(Utils.NOT_CANCELLER);

        Assert.assertEquals(4, a.getNumberOfStates());
        Assert.assertEquals(5, a.getNumberOfTransitions());
        TestUtils.outputPNG(a, "fig1c_det");

        a.minimize(Utils.NOT_CANCELLER);
        TestUtils.outputPNG(a, "fig1c_min");

    }

//    @Test
//    public void testAutomaton1cOrig() throws IOException {
//        dk.brics.automaton.Automaton a = new dk.brics.automaton.Automaton();
//        dk.brics.automaton.State sA = new dk.brics.automaton.State();
//        dk.brics.automaton.State sB = new dk.brics.automaton.State();
//        dk.brics.automaton.State sC = new dk.brics.automaton.State();
//        dk.brics.automaton.State sD = new dk.brics.automaton.State();
//        dk.brics.automaton.State sE = new dk.brics.automaton.State();
//        a.setInitialState(sA);
//        sA.setAccept(true);
//        sA.addTransition(new dk.brics.automaton.Transition('a',sB));
//        sB.addTransition(new dk.brics.automaton.Transition('b',sC));
//        sC.addTransition(new dk.brics.automaton.Transition('c',sB));
//        sB.addTransition(new dk.brics.automaton.Transition('b',sD));
//        sD.addTransition(new dk.brics.automaton.Transition('d',sE));
//        sE.addTransition(new dk.brics.automaton.Transition('e',sA));
//
//        outputPNG(a, "fig1c_orig");
//        a.setDeterministic(false);
//        a.determinize();
//        outputPNG(a, "fig1c_orig_det");
//
//        a.minimize();
//        outputPNG(a, "fig1c_orig_min");
//    }
//
//
//    private void outputPNG(dk.brics.automaton.Automaton a, String name) throws IOException {
//        String dotFile = name+".dot";
//        String pngFile = name+".png";
//        FileWriter fw = new FileWriter(dotFile);
//        fw.write(a.toDot());
//        fw.close();
//
//        File output = new File(pngFile);
//        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFile);
//        pb.redirectOutput(output);
//        pb.start();
//    }
}
