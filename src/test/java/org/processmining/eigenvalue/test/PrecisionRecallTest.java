package org.processmining.eigenvalue.test;

import org.junit.Before;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.provider.*;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

public class PrecisionRecallTest {

    protected UIPluginContext context;

    public static PrecisionProvider[] precisionProviders = new PrecisionProvider[]{
            new ETCPrecision(),
            new AlignmentPrecisionFitness(),
            new AntiAlignmentPrecision(),
            new OneAlignPrecision(),
            new ProjectedPrecision(),
            new LanguagePrecisionRecall(),
            new BestAlignProvider(),
            new KUAdvancedBehavioralAppropriateness(),
            new KUSimpleBehavioralAppropriateness(),
            new KUNegativeEventProvider()
    };

    public static RecallProvider[] recallProviders = new RecallProvider[]{
            new KUAdvancedBehavioralAppropriateness(),
            new AlignmentPrecisionFitness(),
            new BehaviouralProfileConformance(),
            new LanguagePrecisionRecall(),
            new KUNegativeEventProvider(),
            new KUSimpleBehavioralAppropriateness(),
            new ParsingMeasure()
    };


    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception{
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[])usrPathsField.get(null);

        //check if the path to add is already present
        for(String path : paths) {
            if(path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length-1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }

    @Before
    public void setUp() throws Exception {
        final String dir = System.getProperty("user.dir");
        String lpsolvePath = new File(dir,"lib/lib/ux64/").getAbsolutePath();
        String lpsolvePathWin = new File(dir,"lib/lib/win64/").getAbsolutePath();
        String lpsolvePathMac = new File(dir,"lib/lib/mac/").getAbsolutePath();

        addLibraryPath("/usr/lib/lp_solve");
        addLibraryPath(lpsolvePath);
        addLibraryPath(lpsolvePathWin);
        addLibraryPath(lpsolvePathMac);
        try {
            System.loadLibrary("lpsolve55");
        } catch (Exception e){
            e.printStackTrace();
        }
        this.context = StochasticNetUtils.getDummyUIContext();
    }
}
