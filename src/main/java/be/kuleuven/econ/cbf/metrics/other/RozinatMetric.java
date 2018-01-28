package be.kuleuven.econ.cbf.metrics.other;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.rozinat.ui.RozinatJComponent;
import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.VisualPetrinetEvaluatorPlugin;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;

import java.util.Map;
import java.util.TreeMap;

public abstract class RozinatMetric extends AbstractSimpleMetric {

    protected XLog log;
    protected Petrinet net;
    protected PetrinetLogMapper mapper;

    protected XEventClassifier classifier;

    protected boolean findBestShortestSequence;
    protected int maxdepth;
    protected int timeoutLogReplay;
    protected int timeoutStateSpaceExploration;

    protected ConformanceSettings settings;

    public RozinatMetric() {
        findBestShortestSequence = false;
        maxdepth = 1;
        timeoutLogReplay = 0;
        timeoutStateSpaceExploration = 0;
        settings = new ConformanceSettings();

        log = null;
        net = null;
        this.classifier = MappingUtils.DEFAULT_CLASSIFIER;
    }

    public RozinatMetric(XEventClassifier classifier){
        findBestShortestSequence = false;
        maxdepth = 1;
        timeoutLogReplay = 0;
        timeoutStateSpaceExploration = 0;
        settings = new ConformanceSettings();

        log = null;
        net = null;
        this.classifier = classifier;
    }

    @Override
    public synchronized void calculate() throws IllegalStateException {
        settings.findBestShortestSequence = this.findBestShortestSequence;
        settings.maxDepth = this.maxdepth;
        settings.timeoutLogReplay = this.timeoutLogReplay;
        settings.timeoutStateSpaceExploration = this.timeoutStateSpaceExploration;
        this.setMetric();

        RozinatJComponent result = VisualPetrinetEvaluatorPlugin.main(new FakePluginContext(),
                log, net, PetrinetUtils.getInitialMarking(net), mapper, settings);
        this.obtainResult((ConformanceAnalysisResults)result.getInside());
    }

    @Override
    protected Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("findBestShortestSequence",
                Boolean.toString(findBestShortestSequence));
        map.put("maxdepth", Integer.toString(maxdepth));
        map.put("timeoutLogReplay", Integer.toString(timeoutLogReplay));
        map.put("timeoutStateSpaceExploration",
                Integer.toString(timeoutStateSpaceExploration));
        return map;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public synchronized void load(Mapping mapping) {
        net = mapping.getPetrinet();
        log = mapping.getLog();
        mapper = MappingUtils.getPetrinetLogMapper(mapping, net, log, this.classifier);
    }

    protected abstract void obtainResult(ConformanceAnalysisResults result);
    protected abstract void setMetric();

    public boolean getFindBestShortestSequence() {
        return findBestShortestSequence;
    }

    public int getMaxDepth() {
        return maxdepth;
    }

    public int getTimeoutLogReplay() {
        return timeoutLogReplay;
    }

    public int getTimeoutStateSpaceExploration() {
        return timeoutStateSpaceExploration;
    }

    public void setFindBestShortestSequence(boolean b) {
        this.findBestShortestSequence = b;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth >= -1)
            this.maxdepth = maxDepth;
        else
            throw new IllegalArgumentException();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        setFindBestShortestSequence(Boolean.parseBoolean(properties
                .get("findBestShortestSequence")));
        setMaxDepth(Integer.parseInt(properties.get("maxdepth")));
        setTimeoutLogReplay(Integer
                .parseInt(properties.get("timeoutLogReplay")));
        setTimeoutStateSpaceExploration(Integer.parseInt(properties
                .get("timeoutStateSpaceExploration")));
    }

    public void setTimeoutLogReplay(int timeoutLogReplay) {
        if (timeoutLogReplay >= 0)
            this.timeoutLogReplay = timeoutLogReplay;
        else
            throw new IllegalArgumentException();
    }

    public void setTimeoutStateSpaceExploration(int timeoutStateSpaceExploration) {
        if (timeoutStateSpaceExploration >= 0)
            this.timeoutStateSpaceExploration = timeoutStateSpaceExploration;
        else
            throw new IllegalArgumentException();
    }
}