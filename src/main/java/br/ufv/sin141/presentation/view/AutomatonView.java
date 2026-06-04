package br.ufv.sin141.presentation.view;

import br.ufv.sin141.application.controller.TheoryAppController;
import br.ufv.sin141.application.dto.TransitionTableRow;
import br.ufv.sin141.domain.model.Automaton;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AutomatonView extends VBox {

    private TheoryAppController app;
    private final Label titleLabel = new Label();
    private final Label placeholder = new Label();
    private final TableView<TransitionTableRow> table = new TableView<>();
    private final AutomatonGraphView graphView = new AutomatonGraphView();
    private final Slider zoomSlider = new Slider(
            AutomatonGraphView.getMinZoom(), AutomatonGraphView.getMaxZoom(), 1.0);
    private final Label zoomLabel = new Label("100%");
    private final ScrollPane scroll = new ScrollPane();
    private final BorderPane diagramPane = new BorderPane();
    private final SplitPane content = new SplitPane();

    public AutomatonView() {
        getStyleClass().add("automaton-view");
        setSpacing(6);
        setPadding(new Insets(8));

        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        placeholder.setStyle("-fx-text-fill: #888888;");
        placeholder.setVisible(false);
        placeholder.setManaged(false);

        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.setBlockIncrement(0.1);
        HBox.setHgrow(zoomSlider, Priority.ALWAYS);
        zoomLabel.setMinWidth(50);

        HBox zoomBar = new HBox(8, new Label("Zoom"), zoomSlider, zoomLabel);
        zoomBar.setAlignment(Pos.CENTER_LEFT);
        zoomBar.setPadding(new Insets(0, 0, 4, 0));

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setContent(graphView);
        graphView.prefWidthProperty().bind(scroll.widthProperty().subtract(2));
        graphView.prefHeightProperty().bind(scroll.heightProperty().subtract(2));

        graphView.zoomProperty().bindBidirectional(zoomSlider.valueProperty());
        zoomSlider.valueProperty().addListener((obs, oldV, newV) ->
                zoomLabel.setText(Math.round(newV.doubleValue() * 100) + "%"));

        diagramPane.setTop(zoomBar);
        diagramPane.setCenter(scroll);

        content.setOrientation(javafx.geometry.Orientation.VERTICAL);
        content.getItems().addAll(table, diagramPane);
        content.setDividerPositions(0.45);
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleLabel, content, placeholder);
    }

    public void setApp(TheoryAppController app) {
        this.app = app;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void render(Automaton automaton) {
        if (app == null) {
            throw new IllegalStateException("TheoryAppController not injected via setApp(...)");
        }
        showContent(true);
        rebuildColumns(automaton);
        table.getItems().setAll(app.asTableRows(automaton));
        graphView.render(automaton);
    }

    public void showPlaceholder(String message) {
        placeholder.setText(message);
        showContent(false);
        table.getItems().clear();
        graphView.clear();
    }

    public void clear() {
        showContent(false);
        placeholder.setText("");
        table.getItems().clear();
        graphView.clear();
    }

    private void showContent(boolean show) {
        content.setVisible(show);
        content.setManaged(show);
        placeholder.setVisible(!show);
        placeholder.setManaged(!show);
    }

    private void rebuildColumns(Automaton automaton) {
        table.getColumns().clear();
        TableColumn<TransitionTableRow, String> idCol = new TableColumn<>("Estado");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayId()));
        idCol.setPrefWidth(110);
        table.getColumns().add(idCol);
        for (String symbol : app.orderedSymbols(automaton)) {
            String header = symbol.isEmpty() ? "ε" : symbol;
            TableColumn<TransitionTableRow, String> col = new TableColumn<>(header);
            col.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getTransitionsBySymbol().getOrDefault(header, "—")));
            col.setPrefWidth(80);
            table.getColumns().add(col);
        }
    }
}
