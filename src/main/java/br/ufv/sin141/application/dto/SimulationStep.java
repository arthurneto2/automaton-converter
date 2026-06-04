package br.ufv.sin141.application.dto;

public final class SimulationStep {

    private final int position;
    private final String fromState;
    private final String symbol;
    private final String toState;

    public SimulationStep(int position, String fromState, String symbol, String toState) {
        this.position = position;
        this.fromState = fromState;
        this.symbol = symbol;
        this.toState = toState;
    }

    public int getPosition() {
        return position;
    }

    public String getFromState() {
        return fromState;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getToState() {
        return toState;
    }
}
