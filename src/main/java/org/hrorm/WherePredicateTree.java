package org.hrorm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WherePredicateTree {

    public enum Conjunction {
        AND,OR
    }

    interface WherePredicateNode {

        String render(String prefix);

        List<WherePredicateAtom> asAtomList();

    }

    class WherePredicateBranch implements WherePredicateNode {
        private final Conjunction conjunction;
        private final WherePredicateNode leftNode;
        private final WherePredicateNode rightNode;

        public WherePredicateBranch(WherePredicateNode leftNode, Conjunction conjunction, WherePredicateNode rightNode){
            this.leftNode = leftNode;
            this.conjunction = conjunction;
            this.rightNode = rightNode;
        }

        public String render(String prefix){
            return leftNode.render(prefix) + " " + conjunction + " " + rightNode.render(prefix);
        }

        @Override
        public List<WherePredicateAtom> asAtomList() {
            ArrayList<WherePredicateAtom> atoms = new ArrayList<>();
            atoms.addAll(leftNode.asAtomList());
            atoms.addAll(rightNode.asAtomList());
            return atoms;
        }
    }

    class WherePredicateLeaf implements WherePredicateNode {
        private final WherePredicateAtom atom;

        public WherePredicateLeaf(WherePredicateAtom atom){
            this.atom = atom;
        }

        public String render(String prefix){
            return atom.render(prefix);
        }

        @Override
        public List<WherePredicateAtom> asAtomList() {
            return Collections.singletonList(atom);
        }
    }

    class WherePredicateGroup implements WherePredicateNode {
        private final WherePredicateNode node;

        public WherePredicateGroup(WherePredicateNode node){
            this.node = node;
        }

        public String render(String prefix){
            return " ( " + node.render(prefix) + " ) ";
        }

        @Override
        public List<WherePredicateAtom> asAtomList() {
            return node.asAtomList();
        }
    }

    private WherePredicateNode rootNode;

    public WherePredicateTree(WherePredicateAtom atom){
        this.rootNode = new WherePredicateLeaf(atom);
    }

    public void addAtom(Conjunction conjunction, WherePredicateAtom atom){
        WherePredicateLeaf newLeaf = new WherePredicateLeaf(atom);
        WherePredicateBranch branch = new WherePredicateBranch(rootNode, conjunction, newLeaf);
        rootNode = branch;
    }

    public void addNode(Conjunction conjunction, WherePredicateNode node){
        WherePredicateBranch branch = new WherePredicateBranch(rootNode, conjunction, new WherePredicateGroup(node));
        rootNode = branch;
    }

    public String render(String prefix){
        return rootNode.render(prefix);
    }

    public WherePredicateNode getRootNode(){
        return rootNode;
    }

    public List<WherePredicateAtom> asAtomList(){
        return rootNode.asAtomList();
    }
}
