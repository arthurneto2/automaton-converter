package br.ufv.sin141.presentation.panel;

import br.ufv.sin141.application.controller.TheoryAppController;
import br.ufv.sin141.application.exception.MalformedGrammarException;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.RegularGrammar;
import br.ufv.sin141.presentation.MainViewController;
import br.ufv.sin141.presentation.view.AutomatonView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class GrammarController {

    private static final String EXAMPLE =
            "S -> aS | bA\nA -> a | ε";

    private final TheoryAppController app = MainViewController.appController();

    @FXML private TextArea inputArea;
    @FXML private TextArea canonicalArea;
    @FXML private AutomatonView automatonView;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        inputArea.setText(EXAMPLE);
        canonicalArea.setText("");
        automatonView.setApp(app);
        automatonView.setTitle("Autômato derivado");
        automatonView.showPlaceholder(
                "Clique em \"Gramática → Autômato\" ou \"Autômato → Gramática\".");
    }

    @FXML
    public void onGrammarToAutomaton() {
        try {
            RegularGrammar grammar = app.parseGrammar(inputArea.getText());
            canonicalArea.setText(app.formatGrammar(grammar));
            Nfa nfa = app.grammarToAutomaton(grammar);
            automatonView.render(nfa);
            statusLabel.setText("Gramática convertida: " + nfa.getStates().size()
                    + " estados, " + nfa.getTransitions().size() + " transições.");
        } catch (MalformedGrammarException ex) {
            showError("Gramática inválida", ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Erro ao converter gramática", ex.getMessage());
        }
    }

    @FXML
    public void onAutomatonToGrammar() {
        Nfa current = app.getCurrentAutomaton();
        if (current == null) {
            showError("Nenhum autômato definido",
                    "Defina um autômato na aba \"Definição\" antes de extrair a gramática.");
            return;
        }
        try {
            RegularGrammar grammar = app.automatonToGrammar(current);
            String formatted = app.formatGrammar(grammar);
            inputArea.setText(formatted);
            canonicalArea.setText(formatted);
            automatonView.render(current);
            statusLabel.setText("Gramática extraída do autômato atual: "
                    + grammar.getRules().size() + " produções.");
        } catch (RuntimeException ex) {
            showError("Erro ao extrair gramática", ex.getMessage());
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(header);
        alert.showAndWait();
        statusLabel.setText("Erro: " + message);
    }
}
