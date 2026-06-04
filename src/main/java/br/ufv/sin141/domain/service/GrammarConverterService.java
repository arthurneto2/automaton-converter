package br.ufv.sin141.domain.service;

import br.ufv.sin141.application.exception.MalformedGrammarException;
import br.ufv.sin141.domain.model.Automaton;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.ProductionRule;
import br.ufv.sin141.domain.model.RegularGrammar;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GrammarConverterService {

    private static final String FINAL_STATE_ID = "F";

    public Automaton toAutomaton(RegularGrammar grammar) {
        validate(grammar);

        Map<String, State> nonTerminalToState = new HashMap<>();
        boolean startIsAccepting = hasEpsilonProductionForStart(grammar);

        for (String nt : grammar.getNonTerminals()) {
            boolean isStart = nt.equals(grammar.getStartSymbol());
            boolean accepting = isStart && startIsAccepting;
            nonTerminalToState.put(nt, new State(nt, isStart, accepting));
        }
        State finalState = new State(FINAL_STATE_ID, false, true);

        Set<State> states = new LinkedHashSet<>(nonTerminalToState.values());
        states.add(finalState);

        Set<Transition> transitions = new LinkedHashSet<>();
        for (ProductionRule rule : grammar.getRules()) {
            if (rule.isEpsilon()) continue;

            State origin = nonTerminalToState.get(rule.getLeftHandSide());
            String terminal = rule.getRightHandSideTerminal();

            if (rule.isTerminalOnly()) {
                transitions.add(new Transition(origin, terminal, Set.of(finalState)));
            } else {
                State target = nonTerminalToState.get(rule.getRightHandSideNonTerminal());
                transitions.add(new Transition(origin, terminal, Set.of(target)));
            }
        }

        return new Nfa(states, grammar.getTerminals(), transitions, nonTerminalToState.get(grammar.getStartSymbol()));
    }

    public RegularGrammar toGrammar(Automaton automaton) {
        Set<String> nonTerminals = new LinkedHashSet<>();
        for (State s : automaton.getStates()) nonTerminals.add(s.getId());

        Set<String> terminals = new LinkedHashSet<>(automaton.getAlphabet());
        terminals.remove(Transition.EPSILON);

        Set<ProductionRule> rules = new LinkedHashSet<>();
        for (Transition transition : automaton.getTransitions()) {
            if (transition.isEpsilon()) continue;
            String lhs = transition.getOrigin().getId();
            String terminal = transition.getSymbol();
            for (State target : transition.getTargets()) {
                rules.add(new ProductionRule(lhs, terminal, target.getId()));
                if (target.isAccepting()) {
                    rules.add(new ProductionRule(lhs, terminal, null));
                }
            }
        }

        if (automaton.getInitialState().isAccepting()) {
            rules.add(new ProductionRule(automaton.getInitialState().getId(), null, null));
        }

        return new RegularGrammar(nonTerminals, terminals, rules, automaton.getInitialState().getId());
    }

    private void validate(RegularGrammar grammar) {
        for (ProductionRule rule : grammar.getRules()) {
            if (!grammar.getNonTerminals().contains(rule.getLeftHandSide())) {
                throw new MalformedGrammarException(
                        "LHS '" + rule.getLeftHandSide() + "' is not a declared non-terminal");
            }
            if (rule.isEpsilon()) {
                if (!rule.getLeftHandSide().equals(grammar.getStartSymbol())) {
                    throw new MalformedGrammarException(
                            "Epsilon production is only allowed for the start symbol");
                }
                continue;
            }
            if (rule.getRightHandSideTerminal() == null
                    || !grammar.getTerminals().contains(rule.getRightHandSideTerminal())) {
                throw new MalformedGrammarException(
                        "Production '" + rule.getLeftHandSide() + " -> ...' uses an undeclared terminal");
            }
            if (rule.getRightHandSideNonTerminal() != null
                    && !grammar.getNonTerminals().contains(rule.getRightHandSideNonTerminal())) {
                throw new MalformedGrammarException(
                        "Production '" + rule.getLeftHandSide() + " -> "
                                + rule.getRightHandSideTerminal()
                                + rule.getRightHandSideNonTerminal()
                                + "' uses an undeclared non-terminal");
            }
        }
    }

    private boolean hasEpsilonProductionForStart(RegularGrammar grammar) {
        for (ProductionRule rule : grammar.getRules()) {
            if (rule.isEpsilon() && rule.getLeftHandSide().equals(grammar.getStartSymbol())) {
                return true;
            }
        }
        return false;
    }
}
