package org.processmining.eigenvalue.converter;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.*;
import org.processmining.processtree.Event.Message;
import org.processmining.processtree.Event.TimeOut;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.util.*;

/**
 * Plugin to convert a Process Tree to a Petri Net. - Needs work on the OR: can
 * be expressed nonexponentially. - Not all operators are implemented: throws a
 * NotYetImplementedException if fed with such a construct. - Does not make
 * connections between XEventClasses/Leaves and Transitions, but preserves
 * labels.
 * 
 * For non-plugin use, use static call to convert(ProcessTree tree)
 * 
 * @see ProcessTree2Petrinet for original code!
 */

public class RelaxedPT2PetrinetConverter extends ProcessTree2Petrinet {

	public static int getPositionInParent(Node origNode, Node newParentNode) {
		if (newParentNode instanceof Block){
			Block parent = (Block) newParentNode;
			List<Node> children = parent.getChildren();
			for (int i = 0; i < children.size(); i++){
				Node child = children.get(i);
				if (origNode.equals(child)){
					return i;
				}
			}
		}
		System.out.println("Debug me! - Why is it not a child?");
		return 0;
	}

	/*
	 * Input: ProcessTree Output: PetrinetWithMarkings (contains Petri net,
	 * initial Marking, final Marking)
	 */
	public static PetrinetWithMarkings convert(ProcessTree tree)
			throws NotYetImplementedException, InvalidProcessTreeException {
		return convert(tree, false);
	}

	public static PetrinetWithMarkings convertKeepStructure(ProcessTree tree)
			throws NotYetImplementedException, InvalidProcessTreeException {
		return convert(tree, true);
	}

	public static PetrinetWithMarkings convert(ProcessTree tree, boolean keepStructure)
			throws NotYetImplementedException, InvalidProcessTreeException {
		Petrinet petrinet = new PetrinetImpl(tree.getName());
		Place source = petrinet.addPlace("source " + placeCounter.incrementAndGet());
		Place sink = petrinet.addPlace("sink " + placeCounter.incrementAndGet());
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		Map<UnfoldedNode, Set<Transition>> mapPath2Transitions = new HashMap<UnfoldedNode, Set<Transition>>();
		Map<Transition, UnfoldedNode> mapTransition2Path = new HashMap<Transition, UnfoldedNode>();
		UnfoldedNode root = new UnfoldedNode(tree.getRoot());

		convertNode(root, source, sink, petrinet, false, keepStructure, mapPath2Transitions, mapTransition2Path);

		PetrinetWithMarkings result = new PetrinetWithMarkings();
		result.petrinet = petrinet;
		result.initialMarking = initialMarking;
		result.finalMarking = finalMarking;
		result.mapPath2Transitions = mapPath2Transitions;
		result.mapTransition2Path = mapTransition2Path;
		return result;
	}

	private static void convertNode(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                    boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                    Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		Node node = unode.getNode();
		if (node instanceof AbstractBlock && ((AbstractBlock)node).getChildren().isEmpty()){
			convertTau(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
					mapTransition2Path);
		} else {
			if (node instanceof AbstractTask.Automatic) {
				convertTau(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractTask.Manual) {
				convertTask(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractBlock.And) {
				convertAnd(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractBlock.Seq) {
				convertSeq(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractBlock.Xor) {
				convertXor(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractBlock.XorLoop) {
				convertXorLoop(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure,
						mapPath2Transitions, mapTransition2Path);
			} else if (node instanceof AbstractBlock.Or) {
				convertOr(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure, mapPath2Transitions,
						mapTransition2Path);
			} else if (node instanceof AbstractBlock.Def) {
				convertDeferredChoice(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure,
						mapPath2Transitions, mapTransition2Path);
			} else if (node instanceof AbstractBlock.DefLoop) {
				convertDeferredLoop(unode, source, sink, petrinet, forbiddenToPutTokensInSource, keepStructure,
						mapPath2Transitions, mapTransition2Path);
			} else {
				debug("operator of node " + node.getName() + " not supported in translation");
				throw new NotYetImplementedException();
			}
		}
	}

	private static void convertTau(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                   boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                   Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path) {
		Transition t = petrinet.addTransition("tau from tree");
		addTransition(unode, t, mapPath2Transitions, mapTransition2Path);
		t.setInvisible(true);
		petrinet.addArc(source, t);
		petrinet.addArc(t, sink);
	}

	private static void convertTask(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                    boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                    Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path) {
		Transition t = petrinet.addTransition(unode.getNode().getName());
		addTransition(unode, t, mapPath2Transitions, mapTransition2Path);
		petrinet.addArc(source, t);
		petrinet.addArc(t, sink);
	}

	private static void convertXor(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                   boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                   Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		Block node = unode.getBlock();
		for (Node child : node.getChildren()) {
			convertNode(unode.unfoldChild(child), source, sink, petrinet, true, keepStructure, mapPath2Transitions,
					mapTransition2Path);
		}
	}

	private static void convertSeq(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                   boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                   Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		Block node = unode.getBlock();
		int last = node.getChildren().size();
		int i = 0;
		Place lastSink = source;
		for (Node child : node.getChildren()) {
			Place childSink;
			if (i == last - 1) {
				childSink = sink;
			} else {
				childSink = petrinet.addPlace("sink " + placeCounter.incrementAndGet());
			}

			convertNode(unode.unfoldChild(child), lastSink, childSink, petrinet, false, keepStructure,
					mapPath2Transitions, mapTransition2Path);
			lastSink = childSink;
			i++;
		}
	}

	private static void convertAnd(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                   boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                   Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		//add split tau
		Transition t1 = petrinet.addTransition("tau split");
		addTransition(unode, t1, mapPath2Transitions, mapTransition2Path);
		t1.setInvisible(true);
		petrinet.addArc(source, t1);

		//add join tau
		Transition t2 = petrinet.addTransition("tau join");
		addTransition(unode, t2, mapPath2Transitions, mapTransition2Path);
		t2.setInvisible(true);
		petrinet.addArc(t2, sink);

		//add for each child a source and sink place
		for (Node child : unode.getBlock().getChildren()) {
			Place childSource = petrinet.addPlace("source " + placeCounter.incrementAndGet());
			petrinet.addArc(t1, childSource);

			Place childSink = petrinet.addPlace("sink " + placeCounter.incrementAndGet());
			petrinet.addArc(childSink, t2);

			convertNode(unode.unfoldChild(child), childSource, childSink, petrinet, false, keepStructure,
					mapPath2Transitions, mapTransition2Path);
		}
	}

	private static void convertXorLoop(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                       boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                       Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {

		// we adopt the definition of "Discovering Block-Structured Process Models from Incomplete Event Logs"
		// Thus, loops have 1 forward path, and n backwards paths, resulting in:
		// L(XorLoop(M1, ... , Mn)) = L(M1) (L(Xor(M2,..., Mn) L(M1))*

		Place middlePlace = petrinet.addPlace("middle " + placeCounter.incrementAndGet());
		if (forbiddenToPutTokensInSource || keepStructure) {
			//add an extra tau
			Transition t = petrinet.addTransition("tau start");
			addTransition(unode, t, mapPath2Transitions, mapTransition2Path);
			t.setInvisible(true);
			petrinet.addArc(source, t);
			//replace the source
			source = petrinet.addPlace("replacement source " + placeCounter.incrementAndGet());
			petrinet.addArc(t, source);
		}

		Node body = new AbstractTask.Automatic("tau");
		Node redo = new AbstractTask.Automatic("tau");
		Node exit = new AbstractTask.Automatic("tau");
		
		//body
		if (unode.getBlock().getChildren().size() >= 1){
			body = unode.getBlock().getChildren().get(0);
		}
		convertNode(unode.unfoldChild(body), source, middlePlace, petrinet, true,
				keepStructure, mapPath2Transitions, mapTransition2Path);

		//redo
		if(unode.getBlock().getChildren().size()<2){
			// make sure that there is a loop, even though there is no explicit back loop!
			convertNode(unode.unfoldChild(redo), middlePlace, source, petrinet, true,
					keepStructure, mapPath2Transitions, mapTransition2Path);
		} else {
			for (int i = 1; i < unode.getBlock().getChildren().size(); i++){
				redo = unode.getBlock().getChildren().get(1);
				convertNode(unode.unfoldChild(redo), middlePlace, source, petrinet, true,
						keepStructure, mapPath2Transitions, mapTransition2Path);	
			}
		}
		// always add a tau exit:
		
		//exit
		convertNode(unode.unfoldChild(exit), middlePlace, sink, petrinet, true,
				keepStructure, mapPath2Transitions, mapTransition2Path);
	}

	private static void convertOr(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                  boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                  Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {

		Transition start = petrinet.addTransition("tau start");
		addTransition(unode, start, mapPath2Transitions, mapTransition2Path);
		start.setInvisible(true);
		petrinet.addArc(source, start);

		Place notDoneFirst = petrinet.addPlace("notDoneFirst " + placeCounter.incrementAndGet());
		petrinet.addArc(start, notDoneFirst);

		Place doneFirst = petrinet.addPlace("doneFirst " + placeCounter.incrementAndGet());
		Transition end = petrinet.addTransition("tau finish");
		addTransition(unode, end, mapPath2Transitions, mapTransition2Path);
		end.setInvisible(true);
		petrinet.addArc(doneFirst, end);
		petrinet.addArc(end, sink);

		for (Node child : unode.getBlock().getChildren()) {
			Place childSource = petrinet.addPlace("childSource " + placeCounter.incrementAndGet());
			petrinet.addArc(start, childSource);
			Place childSink = petrinet.addPlace("childSink " + placeCounter.incrementAndGet());
			petrinet.addArc(childSink, end);
			Place doChild = petrinet.addPlace("doChild " + placeCounter.incrementAndGet());

			//skip
			Transition skipChild = petrinet.addTransition("tau skipChild");
			addTransition(unode, skipChild, mapPath2Transitions, mapTransition2Path);
			skipChild.setInvisible(true);
			petrinet.addArc(childSource, skipChild);
			petrinet.addArc(skipChild, childSink);
			petrinet.addArc(skipChild, doneFirst);
			petrinet.addArc(doneFirst, skipChild);

			//first do
			Transition firstDoChild = petrinet.addTransition("tau firstDoChild");
			addTransition(unode, firstDoChild, mapPath2Transitions, mapTransition2Path);
			firstDoChild.setInvisible(true);
			petrinet.addArc(childSource, firstDoChild);
			petrinet.addArc(notDoneFirst, firstDoChild);
			petrinet.addArc(firstDoChild, doneFirst);
			petrinet.addArc(firstDoChild, doChild);

			//later do
			Transition laterDoChild = petrinet.addTransition("tau laterDoChild");
			addTransition(unode, laterDoChild, mapPath2Transitions, mapTransition2Path);
			laterDoChild.setInvisible(true);
			petrinet.addArc(childSource, laterDoChild);
			petrinet.addArc(laterDoChild, doChild);
			petrinet.addArc(laterDoChild, doneFirst);
			petrinet.addArc(doneFirst, laterDoChild);

			convertNode(unode.unfoldChild(child), doChild, childSink, petrinet, false, keepStructure,
					mapPath2Transitions, mapTransition2Path);
		}
	}

	@SuppressWarnings("unused")
	private static String getEventLabel(Event e) throws NotYetImplementedException {
		if (e instanceof Message) {
			return "message " + e.getMessage();
		} else if (e instanceof TimeOut) {
			return "time out " + e.getMessage();
		}
		throw new NotYetImplementedException();
	}

	private static void convertDeferredChoice(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                              boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                              Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		for (Node c : unode.getBlock().getChildren()) {
			if (!(c instanceof Event)) {
				//a deferred choice can only have events as its children
				throw new InvalidProcessTreeException();
			}

			Event child = (Event) c;

			if (child.getChildren().size() != 1) {
				//an event can only have one child
				throw new InvalidProcessTreeException();
			}

			//create a tau-transition/event and sink for each event
			Transition t = petrinet.addTransition("tau");
			addTransition(unode, t, mapPath2Transitions, mapTransition2Path);
			t.setInvisible(true);
			Place childSource = petrinet.addPlace("child sink " + placeCounter.incrementAndGet());
			petrinet.addArc(source, t);
			petrinet.addArc(t, childSource);

			//convert the child of the event
			convertNode(unode.unfoldChild(child.getChildren().get(0)), childSource, sink, petrinet, false,
					keepStructure, mapPath2Transitions, mapTransition2Path);
		}
	}

	private static void convertDeferredLoop(UnfoldedNode unode, Place source, Place sink, Petrinet petrinet,
                                            boolean forbiddenToPutTokensInSource, boolean keepStructure,
                                            Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path)
			throws NotYetImplementedException, InvalidProcessTreeException {
		if (unode.getBlock().getChildren().size() != 3) {
			//a loop must have precisely three children: body, redo and exit
			throw new InvalidProcessTreeException();
		}
		if (!(unode.getBlock().getChildren().get(1) instanceof Event)
				|| !(unode.getBlock().getChildren().get(2) instanceof Event)) {
			//children two and three should be events
			throw new InvalidProcessTreeException();
		}

		Event redoEvent = (Event) unode.getBlock().getChildren().get(1);
		if (redoEvent.getChildren().size() != 1) {
			//an event should have precisely one child 
			throw new InvalidProcessTreeException();
		}

		Event exitEvent = (Event) unode.getBlock().getChildren().get(2);
		if (exitEvent.getChildren().size() != 1) {
			//an event should have precisely one child
			throw new InvalidProcessTreeException();
		}

		Place middlePlace = petrinet.addPlace("middle " + placeCounter.incrementAndGet());
		if (forbiddenToPutTokensInSource || keepStructure) {
			//add an extra tau
			Transition t = petrinet.addTransition("tau start");
			addTransition(unode, t, mapPath2Transitions, mapTransition2Path);
			t.setInvisible(true);
			petrinet.addArc(source, t);
			//replace the source
			source = petrinet.addPlace("replacement source " + placeCounter.incrementAndGet());
			petrinet.addArc(t, source);
		}

		//body
		convertNode(unode.unfoldChild(unode.getBlock().getChildren().get(0)), source, middlePlace, petrinet, true,
				keepStructure, mapPath2Transitions, mapTransition2Path);

		//redo
		Transition tRedoEvent = petrinet.addTransition("tau");
		addTransition(unode, tRedoEvent, mapPath2Transitions, mapTransition2Path);
		tRedoEvent.setInvisible(true);
		petrinet.addArc(middlePlace, tRedoEvent);
		Place redoSource = petrinet.addPlace("redo source " + placeCounter.incrementAndGet());
		petrinet.addArc(tRedoEvent, redoSource);
		convertNode(unode.unfoldChild(redoEvent.getChildren().get(0)), redoSource, source, petrinet, false,
				keepStructure, mapPath2Transitions, mapTransition2Path);

		//exit
		Transition tExitEvent = petrinet.addTransition("tau");
		addTransition(unode, tExitEvent, mapPath2Transitions, mapTransition2Path);
		tExitEvent.setInvisible(true);
		Place exitSource = petrinet.addPlace("exit source " + placeCounter.incrementAndGet());
		petrinet.addArc(middlePlace, tExitEvent);
		petrinet.addArc(tExitEvent, exitSource);
		convertNode(unode.unfoldChild(exitEvent.getChildren().get(0)), exitSource, sink, petrinet, false, keepStructure,
				mapPath2Transitions, mapTransition2Path);
	}

	protected static void addTransition(UnfoldedNode unode, Transition t,
                                        Map<UnfoldedNode, Set<Transition>> mapPath2Transitions, Map<Transition, UnfoldedNode> mapTransition2Path) {
		if (mapPath2Transitions.get(unode) == null) {
			mapPath2Transitions.put(unode, new HashSet<Transition>());
		}
		mapPath2Transitions.get(unode).add(t);

		mapTransition2Path.put(t, unode);
	}

	/**
	 * Removes unnecessary tau transitions from XorLoop Blocks.
	 * Does that on the input without returning a new one.
	 * @param tree
	 */
	public static void postProcessMinedTree(ProcessTree tree) {
		for (Node node : new ArrayList<Node>(tree.getNodes())){
			if (node instanceof XorLoop){
				XorLoop loop = (XorLoop) node;
				if (loop.getChildren().size()>1){
					// remove tau nodes (automatic tasks)
					for (int i = loop.getChildren().size()-1; i > 0; i--){
						Node n = loop.getChildren().get(i);
						if (n instanceof Automatic){
							Edge toRemove = loop.getOutgoingEdges().get(i);
							loop.removeOutgoingEdge(toRemove);
							n.removeIncomingEdge(toRemove);
							tree.removeEdge(toRemove);
							tree.removeNode(n);
						}
					}
				}
			}
		}
	}
	
	public static void prepareForQualityCheck(ProcessTree bestTree) {
		for (Node node : new ArrayList<Node>(bestTree.getNodes())){
			if (node instanceof XorLoop){
				XorLoop loop = (XorLoop) node;
				if (loop.getChildren().size()<3){
					// fill in tau nodes (automatic tasks)
					for (int i = loop.getChildren().size(); i < 3; i++){
						Node tau = new Automatic("tau");
						bestTree.addNode(tau);
						Edge e = loop.addChild(tau);
						tau.setProcessTree(bestTree);
						tau.addIncomingEdge(e);
						
					}
				}
			}
			if (node instanceof AbstractBlock){
				Block nodeBlock = (Block) node;
				if (nodeBlock.getChildren().size()<2){
					if (!(node instanceof XorLoop)){
						// remove this node from the tree, as parallel/Xor/Sequence of 1 child = 1 child
						Block parentBlock = null;
						int parentPosition = 0;
						if (node.getParents().size()>0){
							// delete this node and move children to the parent.
							Edge edgeToParent = node.getIncomingEdges().get(0);
							parentBlock = edgeToParent.getSource();
							parentPosition = getPositionInParent(node, parentBlock);
							parentBlock.removeOutgoingEdge(edgeToParent);
							nodeBlock.removeIncomingEdge(edgeToParent);
						}
						List<Edge> edgesToRelocate = new ArrayList<>(nodeBlock.getOutgoingEdges());
						
						int i = 0;
						if (parentBlock != null){
							for (Edge e : edgesToRelocate){
								parentBlock.addChildAt(e.getTarget(), parentPosition++);
								e.getTarget().removeIncomingEdge(e);
								e.getSource().removeOutgoingEdge(e);
								bestTree.removeEdge(e);
							}
						}
						bestTree.removeNode(node);
					}
				}
			}
		}
	}
}
