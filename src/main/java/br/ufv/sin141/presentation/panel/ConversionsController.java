package br.ufv.sin141.presentation.panel;

import br.ufv.sin141.application.controller.TheoryAppController;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.presentation.MainViewController;
import br.ufv.sin141.presentation.view.AutomatonView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class ConversionsController {

    private final TheoryAppController app = MainViewController.appController();

    @FXML private AutomatonView originalView;
    @FXML private AutomatonView dfaView;
    @FXML private AutomatonView minimizedView;
    @FXML private Label statusLabel;

    private static final String NO_AUTOMATON =
            "Defina um autômato na aba \"Definição\" e clique em \"Salvar e Atualizar Preview\".";

    @FXML
    public void initialize() {
        originalView.setApp(app);
        originalView.setTitle("Autômato Original");
        dfaView.setApp(app);
        dfaView.setTitle("AFD (Subset Construction)");
        minimizedView.setApp(app);
        minimizedView.setTitle("AFD Mínimo (Moore)");

        refreshOriginal();
        dfaView.showPlaceholder("Clique em \"Converter AFN → AFD\".");
        minimizedView.showPlaceholder("Clique em \"Minimizar AFD\".");
    }

    @FXML
    public void onRefreshOriginal() {
        refreshOriginal();
    }

    @FXML
    public void onConvertToDfa() {
        if (!ensureAutomatonReady()) return;
        try {
            Dfa dfa = app.convertCurrentToDfa();
            dfaView.render(dfa);
            minimizedView.showPlaceholder("Clique em \"Minimizar AFD\".");
            statusLabel.setText("Conversão concluída: " + dfa.getStates().size()
                    + " estados, " + dfa.getTransitions().size() + " transições.");
        } catch (RuntimeException ex) {
            showError("Erro ao converter para AFD", ex.getMessage());
        }
    }

    @FXML
    public void onMinimizeDfa() {
        if (!ensureAutomatonReady()) return;
        try {
            Dfa minimized = app.minimizeCurrentDfa();
            if (app.getCurrentDfa() != null) dfaView.render(app.getCurrentDfa());
            minimizedView.render(minimized);
            statusLabel.setText("Minimização concluída: " + minimized.getStates().size()
                    + " estados, " + minimized.getTransitions().size() + " transições.");
        } catch (RuntimeException ex) {
            showError("Erro ao minimizar AFD", ex.getMessage());
        }
    }

    private void refreshOriginal() {
        Nfa current = app.getCurrentAutomaton();
        if (current == null) {
            originalView.showPlaceholder(NO_AUTOMATON);
            dfaView.showPlaceholder(NO_AUTOMATON);
            minimizedView.showPlaceholder(NO_AUTOMATON);
            statusLabel.setText("");
        } else {
            originalView.render(current);
            statusLabel.setText("Autômato carregado: " + current.getStates().size()
                    + " estados, " + current.getTransitions().size() + " transições.");
        }
    }

    private boolean ensureAutomatonReady() {
        if (app.getCurrentAutomaton() == null) {
            showError("Nenhum autômato definido", NO_AUTOMATON);
            return false;
        }
        return true;
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(header);
        alert.showAndWait();
        statusLabel.setText("Erro: " + message);
    }
}
