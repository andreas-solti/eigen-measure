package org.processmining.eigenvalue.generator;

import gnu.trove.map.TIntShortMap;
import gnu.trove.map.hash.TIntShortHashMap;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;

import java.util.Random;

/**
 * Created by andreas on 4/25/17.
 */
public class NAryTreeGenerator {

    private final Random random;

    public NAryTreeGenerator() {
        this.random = new Random();
    }

    /**
     * @param seed random seed for the generator
     */
    public NAryTreeGenerator(long seed) {
        this.random = new Random(seed);
    }

    public void setSeed(long seed){
        this.random.setSeed(seed);
    }

    /**
     * Generates a random tree of size numLeafs and ensures that the alphabet is within the size (creates duplicates in this way)
     * @param numLeafs number of leafs
     * @param alphabetSize size of the alphabet
     * @return
     */
    public NAryTree generateTreeWithRestrictedAlphabet(int numLeafs, int alphabetSize){
        NAryTree randomTree = generate(numLeafs);

        for (int i = 0; i < randomTree.size(); i++){
            int leaf = randomTree.getNextLeaf(i);
            if (randomTree.getType(leaf) >= alphabetSize){
                randomTree.setType(leaf, (short)random.nextInt(alphabetSize));
            }
        }
        return randomTree;
    }

    public NAryTree generate(int numLeafs){
        // tau tree with one node:
        NAryTree tree = new NAryTreeImpl(new int[]{1},new short[]{NAryTree.TAU}, new int[]{NAryTree.NONE});

        // add randomly until we have numLeafs leafs
        int leafs = 0;
        while (leafs < numLeafs){
            tree = addRandomNode(tree, numLeafs-leafs);
            leafs = tree.numLeafs();
        }
        TIntShortMap map = new TIntShortHashMap();
        // rename nodes in order:
        for (int i = 0; i < tree.size(); i++){
            if (tree.isLeaf(i)){
                if (!map.containsKey(i)){
                    map.put(i, (short) map.size());
                }
                tree.setType(i, map.get(i));
            }
        }
        return tree;
    }

    private NAryTree addRandomNode(NAryTree tree, int maxChildren) {
        int nChildren = Math.min(1+random.nextInt(maxChildren), 4);

        short[] choices = new short[]{NAryTree.XOR, NAryTree.SEQ, NAryTree.AND, //NAryTree.OR,
                                         NAryTree.LOOP, NAryTree.TAU};
        short choice = choices[random.nextInt(choices.length)];

        int nodeToChange = random.nextInt(tree.size());
        NAryTree subTree;
        int i = 1;
        switch (choice){
            case NAryTree.LOOP:
                subTree = new NAryTreeImpl(new int[] { 3, 2, 3}, new short[] { NAryTree.LOOP,
                        NAryTree.TAU, NAryTree.TAU }, new int[] { NAryTree.NONE, 0, 0 });
                i = nChildren; // do not add stuff to the loop here additionally
                break;
            case NAryTree.TAU:
                subTree = new NAryTreeImpl(new int[]{2, 2}, new short[]{NAryTree.XOR, NAryTree.TAU}, new int[]{NAryTree.NONE, 0});
                break;
            case NAryTree.XOR:
            case NAryTree.SEQ:
            case NAryTree.AND:
            case NAryTree.OR:
            default:
                // select a node and make it exclusive to a number of leaves:
                subTree = new NAryTreeImpl(new int[]{2, 2}, new short[]{choice, (short)tree.numLeafs()}, new int[]{NAryTree.NONE, 0});
                break;
        }

        while (nChildren > i){
            subTree = subTree.addChild(0, i, (short)(subTree.nChildren(0)), Configuration.NOTCONFIGURED);
            i++;
        }
        subTree = subTree.add(tree, nodeToChange, 0, 0);
        tree = tree.replace(nodeToChange, subTree, 0);
        tree = TreeUtils.normalize(tree);
        tree = org.processmining.eigenvalue.tree.TreeUtils.removeUnnecessaryTausAndLeafs(tree);
        return tree;

    }

}
