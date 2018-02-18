package org.processmining.eigenvalue.test;

import dk.brics.automaton2.Automaton;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.processmining.eigenvalue.Utils;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.importing.StochasticNetDeserializer;
import org.processmining.plugins.pnml.simple.PNMLRoot;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUtils {

    public static final String LOGS_FOLDER = "test/logs/";
    public static final String TEST_FOLDER = "test/testfiles/";
    public static final String TEST_OUTPUT_FOLDER = "test/out/";

    public static String ACTIVITIES = "abcdefghijklmnopqrstuvwxyz";

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

    private static short[] getShortArray(String trace) {
        short[] arr = new short[trace.length()];
        for (int i = 0; i < trace.length(); i++){
            arr[i] = (short)ACTIVITIES.indexOf(trace.substring(i,i+1));
        }
        return arr;
    }

    public static Automaton getLogAutomaton(String...traces){
        Automaton a = new Automaton();
        for (String trace : traces) {
            a.incorporateTrace(getShortArray(trace), Utils.NOT_CANCELLER);
        }
        a.determinize(Utils.NOT_CANCELLER);
        a.minimize(Utils.NOT_CANCELLER);
        return a;
    }

    public static void outputPNG(Automaton a, String name) {
        try {
            String dotFileName = name + ".dot";
            String pngFileName = name + ".png";
            File outfolder = new File(TEST_OUTPUT_FOLDER);
            if(!outfolder.exists()){
                outfolder.mkdirs();
            }
            File dotFile = new File(outfolder,dotFileName);
            FileWriter fw = new FileWriter(dotFile);
            fw.write(a.toDot());
            fw.close();

            File pngFile = new File(outfolder, pngFileName);
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath());
            pb.redirectOutput(pngFile);
            pb.start();
        } catch (Exception e){
            // If converting the automaton to PNG fails, then we
            System.err.println("Cannot convert automaton to .png. Possibly 'dot' is not available on this system.");
        }
    }

    /**
     * Iterates the BPI logs
     * @return
     */
    public static Iterable<XLog> getBPILogs(){
        File folder = new File(LOGS_FOLDER);
        if(folder.exists()) {
            List<File> files = Arrays.asList(folder.listFiles());
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            final List<File> finalFiles = files;
            final XUniversalParser parser = new XUniversalParser();
            final AtomicInteger counter = new AtomicInteger(0);
            return new Iterable<XLog>() {
                @Override
                public Iterator<XLog> iterator() {
                    return new Iterator<XLog>() {
                        @Override
                        public boolean hasNext() {
                            return counter.get() < finalFiles.size();
                        }

                        @Override
                        public XLog next() {
                            XLog log = null;
                            try {
                                log = parser.parse(finalFiles.get(counter.getAndIncrement())).iterator().next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return log;
                        }
                    };
                }
            };
        }
        return null;
    }
}
