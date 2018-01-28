package org.processmining.eigenvalue.test;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.importing.StochasticNetDeserializer;
import org.processmining.plugins.pnml.simple.PNMLRoot;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collection;

public class TestUtils {

    public static final String TEST_FOLDER = "test/testfiles/";

    /**
     * Loads a Petri net model from the test files folder.
     *
     * @param name String filename without the suffix ".pnml" tries to load a {@link StochasticNet} from
     * the tests/testfiles folder of the installation.
     *
     * @return size 2 Object[] containing the {@link StochasticNet} and the initial {@link Marking} of the net.
     * @throws Exception
     */
    public static Object[] loadModel(String name, boolean addMarkingsToCache) throws Exception {
        return loadModel(name, addMarkingsToCache, new File(TEST_FOLDER));
    }
    /**
     * Loads a Petri net model from the test files folder.
     *
     * @param name String filename without the suffix ".pnml" tries to load a {@link StochasticNet} from
     * the tests/testfiles folder of the installation.
     * @param addMarkingsToCache indicates whether marking should be cached to avoid searching for plausible
     *                              markings later
     * @param folder
     *
     * @return size 2 Object[] containing the {@link StochasticNet} and the initial {@link Marking} of the net.
     * @throws Exception
     */
    public static Object[] loadModel(String name, boolean addMarkingsToCache, File folder) throws Exception {
        Serializer serializer = new Persister();
        String newname = name;
        if (!name.endsWith("pnml")){
            newname = name + ".pnml";
            if (!new File(folder, newname).exists()){
                newname = name + ".apnml";
            }
        }
        File source = new File(folder, newname);

        PNMLRoot pnml = serializer.read(PNMLRoot.class, source);

        StochasticNetDeserializer converter = new StochasticNetDeserializer();
        Object[] netAndMarking = converter.convertToNet(null, pnml, name, false);
        if (addMarkingsToCache){
            if (netAndMarking[1] != null){
                StochasticNetUtils.cacheInitialMarking((StochasticNet)netAndMarking[0], (Marking) netAndMarking[1]);
            }
            if (netAndMarking[2] != null){
                StochasticNetUtils.cacheFinalMarking((StochasticNet)netAndMarking[0], (Marking) netAndMarking[2]);
            }
        }
        return netAndMarking;
    }

    /**
     * Creates Event classes according a given size (number of classes)
     * @param classifier
     * @param size
     * @return
     */
    public static XEventClasses getxEventClasses(XEventClassifier classifier, int size) {
        XEventClasses eventClasses = new XEventClasses(classifier);
        for (int i = 0; i < size; i++){
            eventClasses.register(i+"");
        }
        return eventClasses;
    }


    public static boolean hasInvisibleTransitions(Collection<Transition> transitions) {
        for (Transition transition : transitions){
            if (transition.isInvisible()){
                return true;
            }
        }
        return false;
    }
}
