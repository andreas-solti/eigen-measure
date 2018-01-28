package org.processmining.plugins.kutoolbox.prom5compat;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

import java.util.HashMap;
import java.util.Map;

public class PetrinetConvertor {
    public static PetriNet convertPetriNet(Petrinet net, Marking marking, PetrinetLogMapper mapper) throws Exception {
        if (marking == null) {
            marking = PetrinetUtils.getInitialMarking(net);
        }

        Map<PetrinetNode, PNNode> nodes = new HashMap();
        PetriNet newNet = new PetriNet();
        for(Place p : net.getPlaces()){
            org.processmining.framework.models.petrinet.Place newPlace = new org.processmining.framework.models.petrinet.Place(p.getId().toString() + " " + p.getLabel(), newNet);
            nodes.put(p, newPlace);

            for(int j = 0; j < marking.occurrences(p).intValue(); ++j) {
                newPlace.addToken(new Token());
            }

            newNet.addAndLinkPlace(newPlace);
        }

        Map<String, Integer> seen = new HashMap();

        for (Transition t : net.getTransitions()){
            String name = t.isInvisible() ? "inv" : t.getLabel();
            if (!seen.containsKey(name)) {
                seen.put(name, Integer.valueOf(0));
            }
            seen.put(name, seen.get(name) + 1);
            if (seen.get(name) > 1) {
                name = name + " #" + seen.get(name);
            }
            org.processmining.framework.models.petrinet.Transition transition = new org.processmining.framework.models.petrinet.Transition(name, newNet);
            nodes.put(t, transition);

            newNet.addAndLinkTransition(transition);

            if (mapper != null) {
                XEventClass ec = (XEventClass)mapper.get(t);
                if (ec == null) {
                    ((org.processmining.framework.models.petrinet.Transition)nodes.get(t)).setLogEvent((LogEvent)null);
                } else if (ec.equals(PetrinetLogMapper.BLOCKING_CLASS)) {
                    ((org.processmining.framework.models.petrinet.Transition)nodes.get(t)).setLogEvent(new LogEvent("\u0000", "\u0000"));
                } else {
                    String[] names = ec.getId().split("\\+");
                    if (names.length == 0) {
                        names = new String[]{"__UNKNOWN_ACT_ERR__", "complete"};
                    }

                    if (names.length == 1) {
                        names = new String[]{names[0], "complete"};
                    }

                    LogEvent e = new LogEvent(names[0], names[1]);
                    ((org.processmining.framework.models.petrinet.Transition)nodes.get(t)).setLogEvent(e);
                }
            }
        }

        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getEdges()){
            PNNode source = (PNNode)nodes.get(e.getSource());
            PNNode target = (PNNode)nodes.get(e.getTarget());
            PNEdge newEdge;
            if (e.getSource() instanceof Place) {
                newEdge = new PNEdge((org.processmining.framework.models.petrinet.Place)source, (org.processmining.framework.models.petrinet.Transition)target);
                newNet.addAndLinkEdge(newEdge, (org.processmining.framework.models.petrinet.Place)source, (org.processmining.framework.models.petrinet.Transition)target);
            } else {
                if (!(e.getSource() instanceof Transition)) {
                    throw new Exception("Could not add edge in PetrinetFacade");
                }

                newEdge = new PNEdge((org.processmining.framework.models.petrinet.Transition)source, (org.processmining.framework.models.petrinet.Place)target);
                newNet.addAndLinkEdge(newEdge, (org.processmining.framework.models.petrinet.Transition)source, (org.processmining.framework.models.petrinet.Place)target);
            }
        }

        return newNet;
    }
}
