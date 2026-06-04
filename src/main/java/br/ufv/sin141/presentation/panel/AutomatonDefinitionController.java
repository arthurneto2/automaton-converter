package br.ufv.sin141.presentation.panel;

import br.ufv.sin141.application.controller.TheoryAppController;
import br.ufv.sin141.application.dto.TransitionTableRow;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import br.ufv.sin141.presentation.MainViewController;
import br.ufv.sin141.presentation.view.AutomatonGraphView;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AutomatonDefinitionController {

    private final TheoryAppController app = MainViewController.appController();

    @FXML private TableView<StateRow> statesTable;
    @FXML private TableColumn<StateRow, String> stateIdColumn;
    @FXML private TableColumn<StateRow, Boolean> stateInitialColumn;
    @FXML private TableColumn<StateRow, Boolean> stateAcceptingColumn;

    @FXML private TextField alphabetField;

    @FXML private TableView<TransitionRow> transitionsTable;
    @FXML private TableColumn<TransitionRow, String> transitionOriginColumn;
    @FXML private TableColumn<TransitionRow, String> transitionSymbolColumn;
    @FXML private TableColumn<TransitionRow, String> transitionTargetsColumn;

    @FXML private TableView<TransitionTableRow> previewTable;
    @FXML private ScrollPane diagramScroll;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomLabel;
    @FXML private Label statusLabel;

    private final ObservableList<StateRow> stateRows = FXCollections.observableArrayList();
    private final ObservableList<TransitionRow> transitionRows = FXCollections.observableArrayList();
    private final AutomatonGraphView graphView = new AutomatonGraphView();

    @FXML
    public void initialize() {
        configureStateTable();
        configureTransitionTable();
        configurePreview();

        stateRows.add(new StateRow("q0", true, false));
        stateRows.add(new StateRow("q1", false, true));
        alphabetField.setText("a, b");
        transitionRows.add(new TransitionRow("q0", "a", "q0, q1"));
        transitionRows.add(new TransitionRow("q0", "b", "q0"));
    }

    private void configureStateTable() {
        statesTable.setEditable(true);
        statesTable.setItems(stateRows);

        stateIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        stateIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        stateIdColumn.setOnEditCommit(e -> e.getRowValue().setId(e.getNewValue()));

        stateInitialColumn.setCellValueFactory(c -> c.getValue().initialProperty());
        stateInitialColumn.setCellFactory(CheckBoxTableCell.forTableColumn(stateInitialColumn));

        stateAcceptingColumn.setCellValueFactory(c -> c.getValue().acceptingProperty());
        stateAcceptingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(stateAcceptingColumn));
    }

    private void configureTransitionTable() {
        transitionsTable.setEditable(true);
        transitionsTable.setItems(transitionRows);

        transitionOriginColumn.setCellValueFactory(new PropertyValueFactory<>("origin"));
        transitionOriginColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        transitionOriginColumn.setOnEditCommit(e -> e.getRowValue().setOrigin(e.getNewValue()));

        transitionSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        transitionSymbolColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        transitionSymbolColumn.setOnEditCommit(e -> e.getRowValue().setSymbol(e.getNewValue()));

        transitionTargetsColumn.setCellValueFactory(new PropertyValueFactory<>("targets"));
        transitionTargetsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        transitionTargetsColumn.setOnEditCommit(e -> e.getRowValue().setTargets(e.getNewValue()));
    }

    private void configurePreview() {
        diagramScroll.setContent(graphView);
        graphView.prefWidthProperty().bind(diagramScroll.widthProperty().subtract(2));
        graphView.prefHeightProperty().bind(diagramScroll.heightProperty().subtract(2));

        graphView.zoomProperty().bindBidirectional(zoomSlider.valueProperty());
        zoomSlider.valueProperty().addListener((obs, oldV, newV) ->
                zoomLabel.setText(Math.round(newV.doubleValue() * 100) + "%"));
    }

    @FXML
    public void onResetZoom() {
        graphView.resetZoom();
    }

    @FXML
    public void onAddState() {
        stateRows.add(new StateRow("q" + stateRows.size(), false, false));
    }

    @FXML
    public void onRemoveState() {
        StateRow selected = statesTable.getSelectionModel().getSelectedItem();
        if (selected != null) stateRows.remove(selected);
    }

    @FXML
    public void onAddTransition() {
        transitionRows.add(new TransitionRow("", "", ""));
    }

    @FXML
    public void onRemoveTransition() {
        TransitionRow selected = transitionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) transitionRows.remove(selected);
    }

    @FXML
    public void onSaveAndPreview() {
        try {
            Nfa nfa = buildNfa();
            app.setCurrentAutomaton(nfa);
            refreshPreview(nfa);
            statusLabel.setText("Autômato salvo. " + nfa.getStates().size() + " estados, "
                    + nfa.getTransitions().size() + " transições.");
        } catch (RuntimeException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.setHeaderText("Não foi possível montar o autômato");
            alert.showAndWait();
            statusLabel.setText("Erro: " + ex.getMessage());
        }
    }

    private void refreshPreview(Nfa nfa) {
        previewTable.getItems().setAll(app.asTableRows(nfa));
        rebuildPreviewColumns(nfa);
        graphView.render(nfa);
    }

    private void rebuildPreviewColumns(Nfa nfa) {
        previewTable.getColumns().clear();
        TableColumn<TransitionTableRow, String> idCol = new TableColumn<>("Estado");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayId()));
        idCol.setPrefWidth(110);
        previewTable.getColumns().add(idCol);

        for (String symbol : app.orderedSymbols(nfa)) {
            String header = symbol.isEmpty() ? "ε" : symbol;
            TableColumn<TransitionTableRow, String> col = new TableColumn<>(header);
            col.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getTransitionsBySymbol().getOrDefault(header, "—")));
            col.setPrefWidth(90);
            previewTable.getColumns().add(col);
        }
    }

    private Nfa buildNfa() {
        if (stateRows.isEmpty()) throw new IllegalArgumentException("Defina pelo menos um estado.");

        Set<String> alphabet = parseAlphabet(alphabetField.getText());
        if (alphabet.isEmpty()) throw new IllegalArgumentException("Defina o alfabeto (símbolos separados por vírgula).");

        Map<String, State> stateById = new LinkedHashMap<>();
        State initial = null;
        for (StateRow row : stateRows) {
            String id = row.getId().trim();
            if (id.isEmpty()) throw new IllegalArgumentException("Estado sem identificador.");
            if (stateById.containsKey(id)) throw new IllegalArgumentException("Estado duplicado: " + id);
            State s = new State(id, row.isInitial(), row.isAccepting());
            stateById.put(id, s);
            if (row.isInitial()) {
                if (initial != null) {
                    throw new IllegalArgumentException("Mais de um estado inicial marcado.");
                }
                initial = s;
            }
        }
        if (initial == null) throw new IllegalArgumentException("Marque exatamente um estado inicial.");

        Set<Transition> transitions = new LinkedHashSet<>();
        for (TransitionRow row : transitionRows) {
            String originId = row.getOrigin().trim();
            if (originId.isEmpty()) continue;
            State origin = stateById.get(originId);
            if (origin == null) throw new IllegalArgumentException("Origem desconhecida: " + originId);

            String symbol = normalizeSymbol(row.getSymbol());
            if (!symbol.equals(Transition.EPSILON) && !alphabet.contains(symbol)) {
                throw new IllegalArgumentException(
                        "Símbolo '" + symbol + "' fora do alfabeto na transição de " + originId);
            }

            Set<State> targets = new LinkedHashSet<>();
            for (String targetId : row.getTargets().split(",")) {
                String t = targetId.trim();
                if (t.isEmpty()) continue;
                State target = stateById.get(t);
                if (target == null) {
                    throw new IllegalArgumentException(
                            "Destino desconhecido '" + t + "' na transição de " + originId);
                }
                targets.add(target);
            }
            if (targets.isEmpty()) {
                throw new IllegalArgumentException(
                        "Transição de " + originId + " com símbolo '" + symbol + "' sem destinos.");
            }
            transitions.add(new Transition(origin, symbol, targets));
        }

        return new Nfa(new LinkedHashSet<>(stateById.values()), alphabet, transitions, initial);
    }

    private Set<String> parseAlphabet(String text) {
        if (text == null) return Set.of();
        Set<String> alphabet = new LinkedHashSet<>();
        for (String token : text.split(",")) {
            String t = token.trim();
            if (!t.isEmpty()) alphabet.add(t);
        }
        return alphabet;
    }

    private String normalizeSymbol(String raw) {
        if (raw == null) return Transition.EPSILON;
        String s = raw.trim();
        if (s.isEmpty() || s.equals("ε") || s.equals("e") || s.equals("&")) return Transition.EPSILON;
        return s;
    }

    public static class StateRow {
        private final SimpleStringProperty id = new SimpleStringProperty();
        private final SimpleBooleanProperty initial = new SimpleBooleanProperty();
        private final SimpleBooleanProperty accepting = new SimpleBooleanProperty();

        public StateRow(String id, boolean initial, boolean accepting) {
            this.id.set(id);
            this.initial.set(initial);
            this.accepting.set(accepting);
        }

        public String getId() { return id.get(); }
        public void setId(String v) { id.set(v); }
        public SimpleStringProperty idProperty() { return id; }

        public boolean isInitial() { return initial.get(); }
        public void setInitial(boolean v) { initial.set(v); }
        public SimpleBooleanProperty initialProperty() { return initial; }

        public boolean isAccepting() { return accepting.get(); }
        public void setAccepting(boolean v) { accepting.set(v); }
        public SimpleBooleanProperty acceptingProperty() { return accepting; }
    }

    public static class TransitionRow {
        private final SimpleStringProperty origin = new SimpleStringProperty();
        private final SimpleStringProperty symbol = new SimpleStringProperty();
        private final SimpleStringProperty targets = new SimpleStringProperty();

        public TransitionRow(String origin, String symbol, String targets) {
            this.origin.set(origin);
            this.symbol.set(symbol);
            this.targets.set(targets);
        }

        public String getOrigin() { return origin.get(); }
        public void setOrigin(String v) { origin.set(v); }
        public SimpleStringProperty originProperty() { return origin; }

        public String getSymbol() { return symbol.get(); }
        public void setSymbol(String v) { symbol.set(v); }
        public SimpleStringProperty symbolProperty() { return symbol; }

        public String getTargets() { return targets.get(); }
        public void setTargets(String v) { targets.set(v); }
        public SimpleStringProperty targetsProperty() { return targets; }
    }
}
