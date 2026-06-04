package br.ufv.sin141.domain.model;

import java.util.Objects;

public final class ProductionRule {

    private final String leftHandSide;
    private final String rightHandSideTerminal;
    private final String rightHandSideNonTerminal;

    public ProductionRule(String leftHandSide,
                          String rightHandSideTerminal,
                          String rightHandSideNonTerminal) {
        this.leftHandSide = Objects.requireNonNull(leftHandSide, "leftHandSide");
        this.rightHandSideTerminal = rightHandSideTerminal;
        this.rightHandSideNonTerminal = rightHandSideNonTerminal;
    }

    public String getLeftHandSide() {
        return leftHandSide;
    }

    public String getRightHandSideTerminal() {
        return rightHandSideTerminal;
    }

    public String getRightHandSideNonTerminal() {
        return rightHandSideNonTerminal;
    }

    public boolean isTerminalOnly() {
        return rightHandSideNonTerminal == null;
    }

    public boolean isEpsilon() {
        return rightHandSideTerminal == null && rightHandSideNonTerminal == null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProductionRule that)) return false;
        return leftHandSide.equals(that.leftHandSide)
                && Objects.equals(rightHandSideTerminal, that.rightHandSideTerminal)
                && Objects.equals(rightHandSideNonTerminal, that.rightHandSideNonTerminal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftHandSide, rightHandSideTerminal, rightHandSideNonTerminal);
    }
}
