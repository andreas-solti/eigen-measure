package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.prom5compat.LogReaderFacade;
import org.processmining.plugins.kutoolbox.prom5compat.PetrinetConvertor;
import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.VisualPetrinetEvaluatorPlugin;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.logreplay.ConformanceAnalysisConfiguration;
import org.processmining.plugins.rozinatconformance.result.ConformanceLogReplayResult;

import java.util.Iterator;
import java.util.Set;

public class AdvancedBehavioralAppropriateness extends AbstractPrecision {
    @Override
    public String getName() {
        return "advBehavioralAppropriateness";
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        Petrinet onet = acceptingPetriNet.getNet();

        PetrinetLogMapper mapper = PetrinetLogMapper.getStandardMap(log, onet);
        ConformanceSettings settings = new ConformanceSettings();
        settings.timeoutStateSpaceExploration = 2;
        settings.timeoutLogReplay = 2;
        settings.maxDepth = 2;
        settings.findBestShortestSequence = true;
        mapper.applyMappingOnTransitions();
        ConformanceAnalysisConfiguration analysisOptions = VisualPetrinetEvaluatorPlugin.createConformanceAnalysisConfiguration(settings);
        VisualPetrinetEvaluatorPlugin calculator = new VisualPetrinetEvaluatorPlugin();


        PetriNet net;
        try {
            net = PetrinetConvertor.convertPetriNet(onet, acceptingPetriNet.getInitialMarking(), mapper);
        } catch (Exception var11) {
            var11.printStackTrace();
            if (context != null) {
                context.getFutureResult(0).cancel(true);
            }

            return null;
        }

        System.out.println("Original net contains transitions: " + onet.getTransitions().size());
        System.out.println("Original net contains places: " + onet.getPlaces().size());
        System.out.println("Original net contains edges: " + onet.getEdges().size());
        System.out.println("Converted net contains transitions: " + net.getTransitions().size());
        System.out.println("Converted net contains places: " + net.getPlaces().size());
        System.out.println("Converted net contains edges: " + net.getEdges().size());
        Iterator var10 = net.getTransitions().iterator();

        while(var10.hasNext()) {
            Transition t = (Transition)var10.next();
            System.out.println(t.getIdentifier() + " --> " + t.getLogEvent());
        }

        LogReaderFacade logReader = VisualPetrinetEvaluatorPlugin.applyMapping(mapper, log);
        boolean res = calculator.calculate(context, logReader, net, settings, analysisOptions);
        if (!res) {
            if (context != null) {
                context.getFutureResult(0).cancel(true);
            }
        } else {
            Set<AnalysisResult> results = analysisOptions.getResultObjects();
            for (AnalysisResult result : results){
                if (result instanceof ConformanceLogReplayResult){
                    ConformanceLogReplayResult conformanceLogReplayResult = (ConformanceLogReplayResult) result;
                    return new Double(conformanceLogReplayResult.getBehavioralAppropriatenessMeasure());
                }
            }
        }
        return null;
    }
}
