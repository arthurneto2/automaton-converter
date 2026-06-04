package br.ufv.sin141.domain.service;

import br.ufv.sin141.application.dto.SimulationStep;
import br.ufv.sin141.application.dto.SimulationTrace;
import br.ufv.sin141.application.exception.AlphabetViolationException;
import br.ufv.sin141.application.exception.NondeterministicTransitionException;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimulationService {

    public boolean simulate(Dfa dfa, String word) {
        return simulateWithTrace(dfa, word).isAccepted();
    }

    public SimulationTrace simulateWithTrace(Dfa dfa, String word) {
        State current = dfa.getInitialState();
        List<SimulationStep> steps = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            String symbol = String.valueOf(word.charAt(i));

            if (!dfa.getAlphabet().contains(symbol)) {
                throw new AlphabetViolationException(
                        "Symbol '" + symbol + "' at position " + i + " is not in the alphabet");
            }

            Set<State> targets = dfa.getTransitionsFrom(current, symbol);

            if (targets.isEmpty()) {
                return new SimulationTrace(
                        false, steps, current.getId(),
                        "No transition from " + current.getId() + " on '" + symbol + "'");
            }
            if (targets.size() > 1) {
                throw new NondeterministicTransitionException(
                        "State " + current.getId() + " has multiple targets on '" + symbol + "'");
            }

            State next = targets.iterator().next();
            steps.add(new SimulationStep(i, current.getId(), symbol, next.getId()));
            current = next;
        }

        boolean accepted = current.isAccepting();
        String reason = accepted ? null
                : "Halted at non-accepting state " + current.getId();
        return new SimulationTrace(accepted, steps, current.getId(), reason);
    }
}
