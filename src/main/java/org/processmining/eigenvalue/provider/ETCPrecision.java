package org.processmining.eigenvalue.provider;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.etconformance.ETCAlgorithm;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

public class ETCPrecision extends AbstractPrecision {


    @Override
    public String getName() {
        return "precisionETC";
    }

    @Override
    public Double getPrecision(UIPluginContext context, AcceptingPetriNet net, XLog log, XEventClassifier classifier) {
        TransEvClassMapping mapping = StochasticNetUtils.getEvClassMapping(net.getNet(), log);
        ETCResults res = new ETCResults();
        try {
            ETCAlgorithm.exec(context, log, net.getNet(), net.getInitialMarking(), mapping, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res.getEtcp();
    }
}
