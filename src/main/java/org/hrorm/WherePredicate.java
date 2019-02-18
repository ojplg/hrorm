package org.hrorm;

import java.util.ArrayList;
import java.util.List;

public class WherePredicate {

    public enum Conjunction {
        START,AND,OR
    }


    public static class JoinedPredicateAtom<T> {
        private final WherePredicateAtom<T> atom;
        private final Conjunction conjunction;

        JoinedPredicateAtom(Conjunction conjunction, WherePredicateAtom<T> atom){
            this.atom = atom;
            this.conjunction = conjunction;
        }
    }

    private final List<JoinedPredicateAtom> atoms = new ArrayList<>();

    public void addJoinedPredicate(JoinedPredicateAtom joinedPredicateAtom){
        this.atoms.add(joinedPredicateAtom);
    }


}
