package core;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

public class CircleGraph<V, E> extends Parent {
    private Graph<V, E> graph;
    private Double radius;
    private Map<V, Coord> vPositions;

    public CircleGraph(Graph<V, E> graph, Double radius) {
        this.graph = graph;
        this.radius = radius;
        loadGraph(graph, radius);
    }

    private void loadGraph(Graph<V, E> graph, double radius) {
        int size = graph.vertexSet().size();
        double th = (2 * Math.PI) / size; //angle between nodes, in radians
        vPositions = new HashMap<>();
        Double xCenter = 0.;
        Double yCenter = 0.;
        int i = 1;
        for (V v : graph.vertexSet()) {
            double x = radius * Math.cos(th*i) + xCenter;
            double y = radius * Math.sin(th*i) + yCenter;
            Coord node = new Coord(x,y);
            vPositions.put(v, node);
            Ellipse ellipse = new Ellipse(x, y,15,10);
            ellipse.setFill(Color.LIGHTBLUE);
            getChildren().addAll(ellipse);
            i++;
        }

        for (E e : graph.edgeSet()) {
            V s = graph.getEdgeSource(e);
            V t = graph.getEdgeTarget(e);
            Arrow a = new Arrow(vPositions.get(s), vPositions.get(t));
            getChildren().add(a);
        }

        //in a separate for so text is on top of all other nodes
        for (V v : graph.vertexSet()) {
            Coord c = vPositions.get(v);
            final Text text = new Text(c.getX() + 7, c.getY() - 10, v.toString());
            text.setFill(Color.BLUE);
            getChildren().addAll(text);
        }
    }
}
