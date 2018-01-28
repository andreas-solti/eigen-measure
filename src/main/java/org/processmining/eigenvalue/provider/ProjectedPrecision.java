package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.eigenvalue.Utils;
import org.processmining.projectedrecallandprecision.framework.CompareLog2PetriNet;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;

public class ProjectedPrecision extends AbstractPrecision {
    @Override
    public String getName() {
        return "projectedPrecision";
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet acceptingPetriNet, XLog log, XEventClassifier classifier) {
        CompareLog2PetriNet comparer = new CompareLog2PetriNet();
        CompareParameters compareParameters = new CompareParameters();
        EfficientLog efficientLog = new EfficientLog(log, classifier);
        try {
            return comparer.apply(acceptingPetriNet, efficientLog, compareParameters, Utils.NOT_CANCELLER).getPrecision();
        } catch (ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException e) {
            e.printStackTrace();
        } catch (AutomatonFailedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
