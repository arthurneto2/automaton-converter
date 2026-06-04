package br.ufv.sin141.domain.service;

import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HopcroftMinimizationService {

    public Dfa minimizeDfa(Dfa dfa) {
        Set<State> reachable = reachableStates(dfa);

        Set<State> accepting = new LinkedHashSet<>();
        Set<State> nonAccepting = new LinkedHashSet<>();
        for (State s : reachable) {
            if (s.isAccepting()) accepting.add(s);
            else nonAccepting.add(s);
        }

        List<Set<State>> partition = new ArrayList<>();
        if (!accepting.isEmpty()) partition.add(accepting);
        if (!nonAccepting.isEmpty()) partition.add(nonAccepting);

        boolean changed = true;
        while (changed) {
            changed = false;
            List<Set<State>> next = new ArrayList<>();
            Map<State, Integer> indexOf = indexOf(partition);

            for (Set<State> group : partition) {
                Map<List<Integer>, Set<State>> bySignature = new LinkedHashMap<>();
                for (State s : group) {
                    List<Integer> signature = new ArrayList<>(dfa.getAlphabet().size());
                    for (String symbol : dfa.getAlphabet()) {
                        Set<State> targets = dfa.getTransitionsFrom(s, symbol);
                        State target = targets.isEmpty() ? null : targets.iterator().next();
                        signature.add(target == null ? -1 : indexOf.get(target));
                    }
                    bySignature.computeIfAbsent(signature, k -> new LinkedHashSet<>()).add(s);
                }
                if (bySignature.size() > 1) changed = true;
                next.addAll(bySignature.values());
            }
            partition = next;
        }

        return buildMinimizedDfa(dfa, partition);
    }

    private Set<State> reachableStates(Dfa dfa) {
        Set<State> visited = new LinkedHashSet<>();
        Deque<State> queue = new ArrayDeque<>();
        queue.add(dfa.getInitialState());
        visited.add(dfa.getInitialState());
        while (!queue.isEmpty()) {
            State s = queue.poll();
            for (String symbol : dfa.getAlphabet()) {
                for (State target : dfa.getTransitionsFrom(s, symbol)) {
                    if (visited.add(target)) queue.add(target);
                }
            }
        }
        return visited;
    }

    private Map<State, Integer> indexOf(List<Set<State>> partition) {
        Map<State, Integer> index = new HashMap<>();
        for (int i = 0; i < partition.size(); i++) {
            for (State s : partition.get(i)) index.put(s, i);
        }
        return index;
    }

    private Dfa buildMinimizedDfa(Dfa dfa, List<Set<State>> partition) {
        Map<Set<State>, State> groupToState = new LinkedHashMap<>();
        Map<State, Set<State>> stateToGroup = new HashMap<>();

        State initial = null;
        for (Set<State> group : partition) {
            boolean isInitial = group.contains(dfa.getInitialState());
            boolean isAccepting = group.iterator().next().isAccepting();
            State merged = new State(mergedId(group), isInitial, isAccepting);
            groupToState.put(group, merged);
            for (State s : group) stateToGroup.put(s, group);
            if (isInitial) initial = merged;
        }

        Set<Transition> transitions = new LinkedHashSet<>();
        for (Set<State> group : partition) {
            State origin = groupToState.get(group);
            State representative = group.iterator().next();
            for (String symbol : dfa.getAlphabet()) {
                Set<State> targets = dfa.getTransitionsFrom(representative, symbol);
                if (targets.isEmpty()) continue;
                State target = targets.iterator().next();
                Set<State> targetGroup = stateToGroup.get(target);
                if (targetGroup == null) continue;
                transitions.add(new Transition(origin, symbol, Set.of(groupToState.get(targetGroup))));
            }
        }

        return new Dfa(new LinkedHashSet<>(groupToState.values()),
                       dfa.getAlphabet(),
                       transitions,
                       initial);
    }

    private String mergedId(Set<State> group) {
        if (group.size() == 1) return group.iterator().next().getId();
        Set<String> ids = new TreeSet<>();
        for (State s : group) ids.add(s.getId());
        return "[" + String.join("|", ids) + "]";
    }
}
