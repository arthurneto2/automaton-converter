package br.ufv.sin141.presentation.view;

import br.ufv.sin141.domain.model.Automaton;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomatonGraphView extends Pane {

    private static final double NODE_RADIUS = 22;
    private static final double ARROW_HEAD = 9;
    private static final double SELF_LOOP_RADIUS = 18;
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.1;

    private Automaton automaton;
    private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    public AutomatonGraphView() {
        setMinSize(320, 240);
        setPrefSize(420, 320);
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");
        widthProperty().addListener((obs, oldV, newV) -> redraw());
        heightProperty().addListener((obs, oldV, newV) -> redraw());

        zoom.addListener((obs, oldV, newV) -> {
            setScaleX(newV.doubleValue());
            setScaleY(newV.doubleValue());
        });

        addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown()) return;
            double factor = event.getDeltaY() > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
            setZoom(getZoom() * factor);
            event.consume();
        });
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public double getZoom() {
        return zoom.get();
    }

    public void setZoom(double value) {
        zoom.set(Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value)));
    }

    public void resetZoom() {
        setZoom(1.0);
    }

    public static double getMinZoom() {
        return MIN_ZOOM;
    }

    public static double getMaxZoom() {
        return MAX_ZOOM;
    }

    public void render(Automaton automaton) {
        this.automaton = automaton;
        redraw();
    }

    public void clear() {
        this.automaton = null;
        getChildren().clear();
    }

    private void redraw() {
        getChildren().clear();
        if (automaton == null || automaton.getStates().isEmpty()) return;

        double w = Math.max(getWidth(), 320);
        double h = Math.max(getHeight(), 240);
        double cx = w / 2;
        double cy = h / 2;
        double layoutRadius = Math.min(w, h) / 2 - NODE_RADIUS * 2;
        if (layoutRadius < 60) layoutRadius = 60;

        List<State> states = new ArrayList<>(automaton.getStates());
        states.sort((a, b) -> a.getId().compareTo(b.getId()));

        Map<State, Point2D> positions = new HashMap<>();
        int n = states.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = cx + layoutRadius * Math.cos(angle);
            double y = cy + layoutRadius * Math.sin(angle);
            positions.put(states.get(i), new Point2D(x, y));
        }

        for (Transition transition : automaton.getTransitions()) {
            Point2D origin = positions.get(transition.getOrigin());
            if (origin == null) continue;
            for (State target : transition.getTargets()) {
                Point2D dest = positions.get(target);
                if (dest == null) continue;
                String label = transition.isEpsilon() ? "ε" : transition.getSymbol();
                if (transition.getOrigin().equals(target)) {
                    drawSelfLoop(origin, label);
                } else {
                    drawArrow(origin, dest, label);
                }
            }
        }

        for (State state : states) {
            Point2D p = positions.get(state);
            drawState(state, p, automaton.getInitialState().equals(state));
        }
    }

    private void drawState(State state, Point2D p, boolean isInitial) {
        Circle outer = new Circle(p.getX(), p.getY(), NODE_RADIUS);
        outer.setFill(Color.web("#f3f7ff"));
        outer.setStroke(Color.web("#1f3a93"));
        outer.setStrokeWidth(1.8);
        getChildren().add(outer);

        if (state.isAccepting()) {
            Circle inner = new Circle(p.getX(), p.getY(), NODE_RADIUS - 4);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(Color.web("#1f3a93"));
            inner.setStrokeWidth(1.4);
            getChildren().add(inner);
        }

        Text label = new Text(state.getId());
        label.setFont(Font.font(12));
        double textW = label.getLayoutBounds().getWidth();
        double textH = label.getLayoutBounds().getHeight();
        label.setX(p.getX() - textW / 2);
        label.setY(p.getY() + textH / 4);
        getChildren().add(label);

        if (isInitial) {
            double arrowEndX = p.getX() - NODE_RADIUS;
            double arrowStartX = p.getX() - NODE_RADIUS - 18;
            Line line = new Line(arrowStartX, p.getY(), arrowEndX, p.getY());
            line.setStroke(Color.web("#1f3a93"));
            line.setStrokeWidth(1.8);
            getChildren().add(line);
            Polygon head = arrowHead(new Point2D(arrowStartX, p.getY()),
                                     new Point2D(arrowEndX, p.getY()));
            head.setFill(Color.web("#1f3a93"));
            getChildren().add(head);
        }
    }

    private void drawArrow(Point2D from, Point2D to, String label) {
        Point2D direction = to.subtract(from).normalize();
        Point2D perp = new Point2D(-direction.getY(), direction.getX());

        Point2D start = from.add(direction.multiply(NODE_RADIUS));
        Point2D end = to.subtract(direction.multiply(NODE_RADIUS));
        Point2D mid = start.midpoint(end).add(perp.multiply(18));

        QuadCurve curve = new QuadCurve(
                start.getX(), start.getY(),
                mid.getX(), mid.getY(),
                end.getX(), end.getY());
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(Color.web("#444444"));
        curve.setStrokeWidth(1.2);
        getChildren().add(curve);

        Polygon head = arrowHead(mid, end);
        head.setFill(Color.web("#444444"));
        getChildren().add(head);

        Text text = new Text(label);
        text.setFont(Font.font(11));
        double tw = text.getLayoutBounds().getWidth();
        text.setX(mid.getX() - tw / 2);
        text.setY(mid.getY() - 4);
        text.setFill(Color.web("#222222"));
        getChildren().add(text);
    }

    private void drawSelfLoop(Point2D at, String label) {
        double cx = at.getX();
        double cy = at.getY() - NODE_RADIUS - SELF_LOOP_RADIUS + 4;
        Circle loop = new Circle(cx, cy, SELF_LOOP_RADIUS);
        loop.setFill(Color.TRANSPARENT);
        loop.setStroke(Color.web("#444444"));
        loop.setStrokeWidth(1.2);
        getChildren().add(loop);

        Point2D arrowFrom = new Point2D(cx + SELF_LOOP_RADIUS - 4, cy + SELF_LOOP_RADIUS / 2);
        Point2D arrowTo = new Point2D(at.getX() + NODE_RADIUS * 0.6, at.getY() - NODE_RADIUS * 0.6);
        Polygon head = arrowHead(arrowFrom, arrowTo);
        head.setFill(Color.web("#444444"));
        getChildren().add(head);

        Text text = new Text(label);
        text.setFont(Font.font(11));
        double tw = text.getLayoutBounds().getWidth();
        text.setX(cx - tw / 2);
        text.setY(cy - SELF_LOOP_RADIUS - 2);
        getChildren().add(text);
    }

    private Polygon arrowHead(Point2D from, Point2D to) {
        Point2D direction = to.subtract(from).normalize();
        Point2D perp = new Point2D(-direction.getY(), direction.getX());
        Point2D tail = to.subtract(direction.multiply(ARROW_HEAD));
        Point2D left = tail.add(perp.multiply(ARROW_HEAD / 2));
        Point2D right = tail.subtract(perp.multiply(ARROW_HEAD / 2));
        Polygon poly = new Polygon();
        poly.getPoints().addAll(
                to.getX(), to.getY(),
                left.getX(), left.getY(),
                right.getX(), right.getY());
        return poly;
    }
}
