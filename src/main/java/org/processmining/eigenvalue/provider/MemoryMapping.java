package org.processmining.eigenvalue.provider;

import be.kuleuven.econ.cbf.input.Mapping;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.*;

public class MemoryMapping extends Mapping {
    protected XLog log;
    protected AcceptingPetriNet acceptingPetriNet;
    private List<Activity> activities;
    private List<String> transitions;
    private Map<String, MapElement> mapping;

    protected MemoryMapping(XLog log, AcceptingPetriNet acceptingPetriNet) {
        super(null, null, false, false);
        this.log = log;
        this.acceptingPetriNet = acceptingPetriNet;
        HashSet<Activity> activities = new HashSet();
        for (XTrace trace : log){
            for(XEvent event : trace){
                Mapping.Activity activity = new Mapping.Activity(event);
                if (!activities.contains(activity)) {
                    activities.add(activity);
                }
            }
        }
        this.activities = new ArrayList();
        this.activities.addAll(activities);
        Collections.sort(this.activities);
        this.transitions = new ArrayList();
        for (Transition t: acceptingPetriNet.getNet().getTransitions()){
            this.transitions.add(t.getLabel());
        }
        Collections.sort(this.transitions);
        this.mapping = new HashMap<>();
        this.doMapping();
    }

    @Override
    protected void doMapping() {
        for (String transition : this.transitions) {
            this.mapping.put(transition, this.getLikelyActivity(transition));
        }
    }

    private MapElement getLikelyActivity(String transition) {
        MapElement mapElement = new MapElement();
        mapElement.activity = null;
        mapElement.certaincy = 0.0F;
        mapElement.invisible = false;
        if (transition.equals("")) {
            return mapElement;
        } else {
            Iterator var4 = this.activities.iterator();

            while(var4.hasNext()) {
                Mapping.Activity activity = (Mapping.Activity)var4.next();
                String uTransition = transition.replace("\\n", "+");
                uTransition = uTransition.replace("\n", "+");
                uTransition = uTransition.trim();
                float f1 = this.prefixMatching(uTransition, activity.getName());
                float f2 = this.prefixMatching(uTransition, activity.getName() + " + " + activity.getType());
                float f3 = this.prefixMatching(uTransition, activity.getName() + "+" + activity.getType());
                float f4 = this.prefixMatching(uTransition, activity.getName() + " (" + activity.getType() + ")");
                float f12 = Math.max(f1, f2);
                float f34 = Math.max(f3, f4);
                float f = Math.max(f12, f34);
                if (f == 1.0F) {
                    mapElement.certaincy = 1.0F;
                    mapElement.activity = activity;
                    return mapElement;
                }

                if (f > mapElement.certaincy) {
                    mapElement.activity = activity;
                    mapElement.certaincy = f;
                }
            }

            if (mapElement.activity == null && mapElement.certaincy < 1.0F &&
                    (transition.matches("^(t[0-9]+)+$") ||
                     transition.matches("^tau .+"))) {
                mapElement.certaincy = 1.0F;
                mapElement.invisible = true;
            }

            if (mapElement.certaincy >= 0.9F) {
                return mapElement;
            } else {
                mapElement.certaincy = 0.0F;
                mapElement.activity = null;
                mapElement.invisible = false;
                return mapElement;
            }
        }
    }

    private class MapElement {
        Mapping.Activity activity;
        float certaincy;
        boolean invisible;

        private MapElement() {
        }
    }

    private float prefixMatching(String one, String two) {
        int minLength = Math.min(one.length(), two.length());

        int nbMatched;
        for(nbMatched = 0; nbMatched < minLength && one.charAt(nbMatched) == two.charAt(nbMatched); ++nbMatched) {
            ;
        }

        int maxLength = Math.max(one.length(), two.length());
        float matched = (float)nbMatched / (float)maxLength;
        return matched;
    }

    @Override
    public Object[] getPetrinetWithMarking() {
        return new Object[]{this.acceptingPetriNet.getNet(), this.acceptingPetriNet.getInitialMarking(), this.acceptingPetriNet.getFinalMarkings().iterator().next()};
    }

    public Mapping.Activity getActivity(String transition) {
        MapElement t = this.mapping.get(transition);
        return t == null ? null : t.activity;
    }

    public float getActivityCertaincy(String transition) {
        return this.mapping.get(transition).certaincy;
    }

    public boolean getActivityInvisible(String transition) {
        return this.mapping.get(transition).invisible;
    }

    @Override
    public XLog getLog() {
        return log;
    }

    @Override
    public Petrinet getPetrinet() {
        return acceptingPetriNet.getNet();
    }
}
