package org.processmining.eigenvalue.provider.override;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker;
import org.processmining.plugins.behavioralconformance.metrics.*;
import org.processmining.plugins.behavioralconformance.profiles.BehavioralProfileEventtrace;
import org.processmining.plugins.behavioralconformance.profiles.BehavioralProfilePetrinet;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;

public class BehaviouralConformance extends BehavioralConformanceChecker {

    public BehaviouralConformance(XLog log, Petrinet net, PetrinetLogMapper mapper) {
        super(log, net, mapper);
    }

    public double getComplianceMetric(BehavioralConformanceChecker.ComplianceMetrics m, XTrace trace) {
        BehavioralProfilePetrinet bpPetri = this.getBehavioralProfilePetrinet();
        BehavioralProfileEventtrace bpTrace = this.getBehavioralProfileEventtrace(trace);
        TraceMetric metric = null;
        switch (m) {
            case CC_CAUSAL_COUPLING:
                metric = new CausalCouplingComplianceMetric(this.mapper);
                break;
            case EC_EXECUTION_ORDER:
                metric = new ExecutionOrderComplianceMetric(this.mapper);
                break;
            case MC_MANDATORY_EXECUTION:
                metric = new MandatoryExecutionComplianceMetric(this.mapper);
                break;
            case CBC_CONSTRAINT_RELATIVE:
                metric = new ConstraintRelativeBehavioralComplianceMetric(this.mapper);
                break;
            case MBC_MODEL_RELATIVE:
                metric = new ModelRelativeBehavioralComplianceMetric(this.mapper);
                break;
            case CCC_CONSTRAINT_RELATIVE:
                metric = new ConstraintRelativeCoOccurrenceComplianceMetric(this.mapper);
                break;
            case MCC_MODEL_RELATIVE:
                metric = new ModelRelativeCoOccurrenceComplianceMetric(this.mapper);
                break;
            case CC_CONSTRAINT_RELATIVE:
                metric = new ConstraintRelativeCaseComplianceMetric(this.mapper);
                break;
            case MC_MODEL_RELATIVE:
                metric = new ModelRelativeCaseComplianceMetric(this.mapper);
                break;
            case C_LOG_COMPLIANCE:
                TraceMetric metric1 = new ExecutionOrderComplianceMetric(this.mapper);
                TraceMetric metric2 = new MandatoryExecutionComplianceMetric(this.mapper);
                TraceMetric metric3 = new CausalCouplingComplianceMetric(this.mapper);
                double value1 = metric1.evaluate(bpPetri, bpTrace, trace);
                double value2 = metric2.evaluate(bpPetri, bpTrace, trace);
                double value3 = metric3.evaluate(bpPetri, bpTrace, trace);
                return 0.3333333333333333D * (value1 + value2 + value3);
            default:
                System.err.println("getComplianceMetric method did not find metric!");
                return -1.0D;
        }

        double value = metric.evaluate(bpPetri, bpTrace, trace);
        return value;
    }
}

