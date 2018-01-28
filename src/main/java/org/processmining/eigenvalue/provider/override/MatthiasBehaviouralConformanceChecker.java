package org.processmining.eigenvalue.provider.override;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.alignment.LabelEntity;
import org.jbpt.bp.CBPForTraceLabelAbstractor;
import org.jbpt.bp.CBPRestrictedLabelAbstractor;
import org.jbpt.bp.CausalBehaviouralProfile;
import org.jbpt.bp.construct.CBPCreatorTrace;
import org.jbpt.bp.construct.CBPCreatorUnfolding;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.conform.ConformanceAnalysis;
import org.jbpt.petri.conform.ConformanceAnalysis.TraceAnalysisTask;
import org.jbpt.petri.log.Trace;
import org.jbpt.petri.log.TraceEntry;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker;
import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker.ComplianceMetrics;
import org.processmining.plugins.behavioralconformance.metrics.*;
import org.processmining.plugins.behavioralconformance.utils.ProMtoJbptConvertUtils;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatthiasBehaviouralConformanceChecker extends BehavioralConformanceChecker {
    private Map<XTrace, ConformanceAnalysis> conformanceAnalysisTrace;
    private CausalBehaviouralProfile<NetSystem, Node> baseProfile;
    private CausalBehaviouralProfile<NetSystem, LabelEntity> baseProfileOnLabels;

    public MatthiasBehaviouralConformanceChecker(XLog log, Petrinet net, PetrinetLogMapper mapper) {
        super(log, net, mapper);
        NetSystem jbptNet = ProMtoJbptConvertUtils.convertPetrinet(net, mapper);
        this.baseProfile = CBPCreatorUnfolding.getInstance().deriveCausalBehaviouralProfile(jbptNet);
        this.baseProfileOnLabels = CBPRestrictedLabelAbstractor.abstractCBPToLabels(this.baseProfile);
        this.conformanceAnalysisTrace = new HashMap();
    }

    private ConformanceAnalysis getConformanceAnalysis(XTrace trace) {
        if (!this.conformanceAnalysisTrace.containsKey(trace)) {
            ConformanceAnalysis conformanceAnalysis = new ConformanceAnalysis(this.baseProfileOnLabels);
            Trace jbptTrace = ProMtoJbptConvertUtils.convertTrace(trace, this.mapper.getEventClassifier());
            jbptTrace.setId(1);
            CausalBehaviouralProfile<Trace, TraceEntry> traceProfile = CBPCreatorTrace.getInstance().deriveCausalBehaviouralProfile(jbptTrace);
            CausalBehaviouralProfile<Trace, LabelEntity> traceProfileOnLabel = CBPForTraceLabelAbstractor.abstractCBPForTraceToLabels(traceProfile);
            conformanceAnalysis.addTrace(traceProfileOnLabel);
            conformanceAnalysis.computeBPConformance();
            conformanceAnalysis.computeCooccurrenceConformance();
            conformanceAnalysis.computeOverallConformance();
            this.conformanceAnalysisTrace.put(trace, conformanceAnalysis);
        }

        return (ConformanceAnalysis)this.conformanceAnalysisTrace.get(trace);
    }

    public double getComplianceMetric(ComplianceMetrics m, XTrace trace) {
        ConformanceAnalysis ca = this.getConformanceAnalysis(trace);
        TraceAnalysisTask ta = null;
        Iterator var6 = ca.getAnalysisTasks().iterator();

        while(var6.hasNext()) {
            TraceAnalysisTask p = (TraceAnalysisTask)var6.next();
            if (((Trace)p.getTraceProfile().getModel()).getId() == 1) {
                ta = p;
                break;
            }
        }

        switch(m) {
            case CC_CAUSAL_COUPLING:
                return -1.0D;
            case EC_EXECUTION_ORDER:
                return -1.0D;
            case MC_MANDATORY_EXECUTION:
                return -1.0D;
            case CBC_CONSTRAINT_RELATIVE:
                return (double)ta.getConstraintRelativeBehaviouralProfileConformance();
            case MBC_MODEL_RELATIVE:
                return (double)ta.getModelRelativeBehaviouralProfileConformance();
            case CCC_CONSTRAINT_RELATIVE:
                return (double)ta.getConstraintRelativeCooccurrenceConformance();
            case MCC_MODEL_RELATIVE:
                return (double)ta.getModelRelativeCooccurrenceConformance();
            case CC_CONSTRAINT_RELATIVE:
                return (double)ta.getConstraintRelativeConformance();
            case MC_MODEL_RELATIVE:
                return (double)ta.getModelRelativeConformance();
            case C_LOG_COMPLIANCE:
                return -1.0D;
            default:
                System.err.println("getComplianceMetric method did not find metric!");
                return -1.0D;
        }
    }
}
