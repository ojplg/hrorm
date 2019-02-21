package org.hrorm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A very simple syntax tree for representing where clauses with possibly
 * nested predicates joined by AND and OR.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class WherePredicateTree {

    public enum Conjunction {
        AND,OR
    }

    interface WherePredicateNode {
        String render(String prefix);
        List<WherePredicate> asList();
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
        public List<WherePredicate> asList() {
            ArrayList<WherePredicate> atoms = new ArrayList<>();
            atoms.addAll(leftNode.asList());
            atoms.addAll(rightNode.asList());
            return atoms;
        }
    }

    class WherePredicateLeaf implements WherePredicateNode {
        private final WherePredicate atom;

        public WherePredicateLeaf(WherePredicate atom){
            this.atom = atom;
        }

        public String render(String prefix){
            return atom.render(prefix);
        }

        @Override
        public List<WherePredicate> asList() {
            return Collections.singletonList(atom);
        }
    }

    public static class WherePredicateGroup implements WherePredicateNode {
        private final WherePredicateNode node;

        public WherePredicateGroup(WherePredicateNode node){
            this.node = node;
        }

        public String render(String prefix){
            return " ( " + node.render(prefix) + " ) ";
        }

        @Override
        public List<WherePredicate> asList() {
            return node.asList();
        }
    }

    public static class EmptyNode implements WherePredicateNode {
        @Override
        public String render(String prefix) {
            return "";
        }

        @Override
        public List<WherePredicate> asList() {
            return Collections.emptyList();
        }
    }

    public static final WherePredicateTree EMPTY = new WherePredicateTree(new EmptyNode());

    private WherePredicateNode rootNode;

    public WherePredicateTree(WherePredicate atom){
        this.rootNode = new WherePredicateLeaf(atom);
    }

    public WherePredicateTree(WherePredicateNode node){
        this.rootNode = node;
    }

    public void addAtom(Conjunction conjunction, WherePredicate atom){
        WherePredicateLeaf newLeaf = new WherePredicateLeaf(atom);
        rootNode = new WherePredicateBranch(rootNode, conjunction, newLeaf);
    }

    public void addNode(Conjunction conjunction, WherePredicateNode node){
        rootNode = new WherePredicateBranch(rootNode, conjunction, new WherePredicateGroup(node));
    }

    public String render(String prefix){
        return rootNode.render(prefix);
    }

    public WherePredicateNode getRootNode(){
        return rootNode;
    }

    public List<WherePredicate> asList(){
        return rootNode.asList();
    }
}
