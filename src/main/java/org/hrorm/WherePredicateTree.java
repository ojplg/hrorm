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

    private interface WherePredicateNode {
        String render(String prefix);
        List<WherePredicate> asList();
    }

    private static class WherePredicateBranch implements WherePredicateNode {
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

    private static class WherePredicateLeaf implements WherePredicateNode {
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

    private static class WherePredicateGroup implements WherePredicateNode {
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

    private static class EmptyNode implements WherePredicateNode {
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

    public WherePredicateTree(WherePredicateTree subTree){
        this.rootNode = new WherePredicateGroup(subTree.rootNode);
    }

    public void addAtom(Conjunction conjunction, WherePredicate atom){
        if( isEmpty() ){
            throw new HrormException("Cannot extend an empty where clause");
        }
        WherePredicateLeaf newLeaf = new WherePredicateLeaf(atom);
        rootNode = new WherePredicateBranch(rootNode, conjunction, newLeaf);
    }

    public void addSubtree(Conjunction conjunction, WherePredicateTree subTree){
        if(isEmpty() ){
            throw new HrormException("Cannot extend an empty where clause");
        }
        rootNode = new WherePredicateBranch(rootNode, conjunction, new WherePredicateGroup(subTree.rootNode));
    }

    public String render(String prefix){
        return rootNode.render(prefix);
    }

    public List<WherePredicate> asList(){
        return rootNode.asList();
    }

    public boolean isEmpty(){
        if ( rootNode == null ){
            return true;
        }
        return EMPTY == this;
    }
}
