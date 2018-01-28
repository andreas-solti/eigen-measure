package org.processmining.eigenvalue.tree;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class TreeUtils {

	public static ProcessTree getClone(ProcessTree orig){
		ProcessTree clone = new ProcessTreeImpl(orig);
		return clone;
	}

	/**
	 * Flattens a tree by allowing non-LOOP nodes to absorb children that are of
	 * the same operator type as they and remove operators that have only 1
	 * child
	 *
	 * @param tree
	 *            NAryTree to flatten
	 * @return NAryTree instance which is the flattened version of tree
	 */
	public static NAryTree removeUnnecessaryTausAndLeafs(NAryTree tree) {
		NAryTree normalizedTree = new NAryTreeImpl(tree);

		/*
		 * Taus inside Sequence or parallel nodes don't make sense
		 * multiple taus inside a xor / or don't make sense either
		 */
		for (int i = 0; i < normalizedTree.size(); i++) {
			//Only for non-leafs and non-loops
			if (!normalizedTree.isLeaf(i)){
				int next = normalizedTree.getNext(i);
				int child = normalizedTree.getChildAtIndex(i, 0);
				boolean changed = true;

				boolean isXOR = normalizedTree.getType(i) == NAryTree.XOR;

				if (isXOR || normalizedTree.getType(i) == NAryTree.OR ){
					// only allow up to one tau leaf:
					boolean containsTau = false;
					TIntSet leafChildren = new TIntHashSet();
					while (child < next && changed) {
						int childType = normalizedTree.getType(child);
						if (isXOR && normalizedTree.isLeaf(child)){
							if (childType != NAryTree.TAU){
								if (leafChildren.contains(childType)) {
									// remove additional same choices
									normalizedTree = normalizedTree.remove(child);
									next--;
								} else {
									leafChildren.add(childType);
								}
							}
						}
						if (containsTau && childType == NAryTree.TAU) {
							// remove additional taus
							normalizedTree = normalizedTree.remove(child);
							next--;
						}
						containsTau = containsTau || childType == NAryTree.TAU;

						int newChild = normalizedTree.getNext(child);
						changed = newChild != child;
						child = newChild;
					}
				} else if (normalizedTree.getType(i) == NAryTree.SEQ || normalizedTree.getType(i) == NAryTree.AND){
					while (child < next && changed) {
						if (normalizedTree.getType(child)== NAryTree.TAU){
							// remove all taus
							normalizedTree = normalizedTree.remove(child);
						}
						int newChild = normalizedTree.getNext(child);
						changed = newChild != child;
						child = newChild;
					}
				}
			}
		}
		return org.processmining.plugins.etm.model.narytree.TreeUtils.normalize(normalizedTree);
	}

	public static String getOperator(short type) {
		String label;
		switch (type) {
			case NAryTree.SEQ:
				label = "->";
				break;
			case NAryTree.REVSEQ:
				label = "<-";
				break;
			case NAryTree.XOR:
				label = "X";
				break;
			case NAryTree.AND:
				label = "AND";
				break;
			case NAryTree.OR:
				label = "OR";
				break;
			case NAryTree.ILV:
				label = "<->";
				break;
			case NAryTree.LOOP:
				label = "↺";
				break;
			default:
				label = "$%@$";
				break;
		}
		return label;
	}

	public static NAryTree cleanupTree(NAryTree tree) {
		int oldTreeSize = Integer.MAX_VALUE;
		int treeSize = tree.size();
		while (oldTreeSize != treeSize) {
			oldTreeSize = treeSize;
			tree = org.processmining.plugins.etm.model.narytree.TreeUtils.normalize(tree);
			tree = removeUnnecessaryTausAndLeafs(tree);
			treeSize = tree.size();
		}
		return tree;
	}
}
