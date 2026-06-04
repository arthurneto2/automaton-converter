package br.ufv.sin141.presentation.panel;

import br.ufv.sin141.application.controller.TheoryAppController;
import br.ufv.sin141.application.dto.SimulationStep;
import br.ufv.sin141.application.dto.SimulationTrace;
import br.ufv.sin141.application.exception.AlphabetViolationException;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.presentation.MainViewController;
import br.ufv.sin141.presentation.view.AutomatonView;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class SimulationController {

    private static final String STYLE_ACCEPTED =
            "-fx-background-color: #1e8e3e; -fx-text-fill: white;"
                    + " -fx-font-size: 28; -fx-font-weight: bold;"
                    + " -fx-padding: 18 0 18 0; -fx-alignment: center;"
                    + " -fx-background-radius: 6;";
    private static final String STYLE_REJECTED =
            "-fx-background-color: #c5221f; -fx-text-fill: white;"
                    + " -fx-font-size: 28; -fx-font-weight: bold;"
                    + " -fx-padding: 18 0 18 0; -fx-alignment: center;"
                    + " -fx-background-radius: 6;";
    private static final String STYLE_NEUTRAL =
            "-fx-background-color: #eeeeee; -fx-text-fill: #555555;"
                    + " -fx-font-size: 18;"
                    + " -fx-padding: 18 0 18 0; -fx-alignment: center;"
                    + " -fx-background-radius: 6;";

    private final TheoryAppController app = MainViewController.appController();

    @FXML private TextField wordField;
    @FXML private AutomatonView dfaView;
    @FXML private Label resultLabel;
    @FXML private Label reasonLabel;
    @FXML private TableView<SimulationStep> stepsTable;
    @FXML private TableColumn<SimulationStep, Number> positionColumn;
    @FXML private TableColumn<SimulationStep, String> fromColumn;
    @FXML private TableColumn<SimulationStep, String> symbolColumn;
    @FXML private TableColumn<SimulationStep, String> toColumn;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        dfaView.setApp(app);
        dfaView.setTitle("AFD ativo");
        configureStepsTable();
        resetResultLabel();
        refreshDfa();
    }

    private void configureStepsTable() {
        positionColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPosition()));
        fromColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFromState()));
        symbolColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSymbol()));
        toColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToState()));
    }

    @FXML
    public void onRefreshDfa() {
        refreshDfa();
    }

    @FXML
    public void onSimulate() {
        if (app.getCurrentAutomaton() == null) {
            showError("Nenhum autômato definido",
                    "Defina um autômato na aba \"Definição\" antes de simular.");
            return;
        }
        String word = wordField.getText() == null ? "" : wordField.getText();
        try {
            SimulationTrace trace = app.simulateWord(word);
            refreshDfa();
            stepsTable.getItems().setAll(trace.getSteps());
            applyResult(trace, word);
            statusLabel.setText("Simulação concluída em " + trace.getSteps().size() + " passos.");
        } catch (AlphabetViolationException ex) {
            showError("Símbolo fora do alfabeto", ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Erro na simulação", ex.getMessage());
        }
    }

    private void refreshDfa() {
        if (app.getCurrentAutomaton() == null) {
            dfaView.showPlaceholder("Defina um autômato na aba \"Definição\".");
            return;
        }
        Dfa dfa = app.getMinimizedDfa() != null ? app.getMinimizedDfa()
                : app.getCurrentDfa() != null ? app.getCurrentDfa()
                : app.convertCurrentToDfa();
        dfaView.render(dfa);
    }

    private void applyResult(SimulationTrace trace, String word) {
        if (trace.isAccepted()) {
            resultLabel.setText("ACEITA");
            resultLabel.setStyle(STYLE_ACCEPTED);
            reasonLabel.setText("Palavra \"" + word + "\" termina em estado aceitador "
                    + trace.getFinalState() + ".");
        } else {
            resultLabel.setText("REJEITADA");
            resultLabel.setStyle(STYLE_REJECTED);
            reasonLabel.setText(trace.getRejectionReason() != null
                    ? trace.getRejectionReason()
                    : "Palavra \"" + word + "\" não foi aceita.");
        }
    }

    private void resetResultLabel() {
        resultLabel.setText("aguardando simulação");
        resultLabel.setStyle(STYLE_NEUTRAL);
        reasonLabel.setText("");
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(header);
        alert.showAndWait();
        statusLabel.setText("Erro: " + message);
    }
}
