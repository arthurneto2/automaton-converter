package br.ufv.sin141.application.dto;

import java.util.Collections;
import java.util.List;

public final class SimulationTrace {

    private final boolean accepted;
    private final List<SimulationStep> steps;
    private final String finalState;
    private final String rejectionReason;

    public SimulationTrace(boolean accepted,
                           List<SimulationStep> steps,
                           String finalState,
                           String rejectionReason) {
        this.accepted = accepted;
        this.steps = Collections.unmodifiableList(List.copyOf(steps));
        this.finalState = finalState;
        this.rejectionReason = rejectionReason;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public List<SimulationStep> getSteps() {
        return steps;
    }

    public String getFinalState() {
        return finalState;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
