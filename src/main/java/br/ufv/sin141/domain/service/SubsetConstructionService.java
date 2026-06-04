package br.ufv.sin141.domain.service;

import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SubsetConstructionService {

    private static final String DEAD_STATE_ID = "{}";

    public Dfa convertNfaToDfa(Nfa nfa) {
        Set<String> dfaAlphabet = new LinkedHashSet<>(nfa.getAlphabet());
        dfaAlphabet.remove(Transition.EPSILON);

        Set<State> startClosure = epsilonClosure(nfa, Set.of(nfa.getInitialState()));
        Map<Set<State>, State> subsetToState = new LinkedHashMap<>();
        State initialDfa = toDfaState(startClosure, true);
        subsetToState.put(startClosure, initialDfa);

        Deque<Set<State>> queue = new ArrayDeque<>();
        queue.add(startClosure);

        Set<Transition> transitions = new LinkedHashSet<>();
        boolean needsDeadState = false;

        while (!queue.isEmpty()) {
            Set<State> current = queue.poll();
            State currentDfa = subsetToState.get(current);

            for (String symbol : dfaAlphabet) {
                Set<State> target = epsilonClosure(nfa, move(nfa, current, symbol));

                if (target.isEmpty()) {
                    needsDeadState = true;
                    continue;
                }

                State targetDfa = subsetToState.get(target);
                if (targetDfa == null) {
                    targetDfa = toDfaState(target, false);
                    subsetToState.put(target, targetDfa);
                    queue.add(target);
                }
                transitions.add(new Transition(currentDfa, symbol, Set.of(targetDfa)));
            }
        }

        Set<State> dfaStates = new LinkedHashSet<>(subsetToState.values());

        if (needsDeadState) {
            State dead = new State(DEAD_STATE_ID, false, false);
            dfaStates.add(dead);
            for (String symbol : dfaAlphabet) {
                transitions.add(new Transition(dead, symbol, Set.of(dead)));
            }
            for (Map.Entry<Set<State>, State> entry : subsetToState.entrySet()) {
                State origin = entry.getValue();
                for (String symbol : dfaAlphabet) {
                    Set<State> target = epsilonClosure(nfa, move(nfa, entry.getKey(), symbol));
                    if (target.isEmpty()) {
                        transitions.add(new Transition(origin, symbol, Set.of(dead)));
                    }
                }
            }
        }

        return new Dfa(dfaStates, dfaAlphabet, transitions, initialDfa);
    }

    private Set<State> epsilonClosure(Nfa nfa, Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Deque<State> stack = new ArrayDeque<>(states);
        while (!stack.isEmpty()) {
            State s = stack.pop();
            for (State next : nfa.getTransitionsFrom(s, Transition.EPSILON)) {
                if (closure.add(next)) {
                    stack.push(next);
                }
            }
        }
        return closure;
    }

    private Set<State> move(Nfa nfa, Set<State> subset, String symbol) {
        Set<State> result = new HashSet<>();
        for (State s : subset) {
            result.addAll(nfa.getTransitionsFrom(s, symbol));
        }
        return result;
    }

    private State toDfaState(Set<State> subset, boolean initial) {
        boolean accepting = false;
        Set<String> ids = new TreeSet<>();
        for (State s : subset) {
            ids.add(s.getId());
            if (s.isAccepting()) accepting = true;
        }
        return new State("{" + String.join(",", ids) + "}", initial, accepting);
    }
}
