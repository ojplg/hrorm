package org.hrorm;

public class WherePredicateTree {

    public enum Conjunction {
        AND,OR
    }

    interface WherePredicateNode {

        String render(String prefix);

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

    }

    class WherePredicateLeaf implements WherePredicateNode {
        private final WherePredicateAtom atom;

        public WherePredicateLeaf(WherePredicateAtom atom){
            this.atom = atom;
        }

        public String render(String prefix){
            return atom.render(prefix);
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
}
