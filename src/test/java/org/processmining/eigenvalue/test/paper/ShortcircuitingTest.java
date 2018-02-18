package org.processmining.eigenvalue.test.paper;

import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.State;
import dk.brics.automaton2.Transition;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.test.TestUtils;

import java.util.ArrayList;
import java.util.List;

public class ShortcircuitingTest {

    @Test
    public void testShortCircuit1() {
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

        System.out.println("Before short-circuiting: " + (a.getNumberOfTransitions()/(double)a.getNumberOfStates()));
        TopologicalEntropyComputer.MatrixAndDegreeStats stats = TopologicalEntropyComputer.getCompressedSparseMatrix(a);
        System.out.println("After short-circuiting:" + stats.getAvgDegree());

    }

    @Test
    public void testMinimizedInput() {
        Automaton a = new Automaton();
        a.incorporateTrace(new short[]{0,1,3,4}, Utils.NOT_CANCELLER);
        a.incorporateTrace(new short[]{0,1,2,1,2,3,4}, Utils.NOT_CANCELLER);

        Assert.assertEquals(10, a.getNumberOfStates());
        Assert.assertEquals(9, a.getNumberOfTransitions());

        TestUtils.outputPNG(a,"logModel");
        a.determinize(Utils.NOT_CANCELLER);
        TestUtils.outputPNG(a,"logModel_det");
        a.minimize(Utils.NOT_CANCELLER);
        TestUtils.outputPNG(a,"logModel_min");

        Assert.assertEquals(8, a.getNumberOfStates());
        Assert.assertEquals(8, a.getNumberOfTransitions());


    }
}
