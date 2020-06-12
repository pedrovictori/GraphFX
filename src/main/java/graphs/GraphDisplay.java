package graphs;

import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.jgrapht.alg.drawing.RandomLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toMap;

public class GraphDisplay<V, E> extends Region {
	/**
	 * The default side size for the diagram
	 */
	private static final double DEFAULT_SIZE = 500;
	private double size;
	private final Graph<V, E> graph;
	private double scale = 1; //todo implement
	Map<V, Point2D> vertices2D;
	Map<V, Shape> nodes;
	Map<V, Text> labels;
	Map<E, Path> edges;
	LayoutAlgorithm2D<V, E> algorithm;
	private BiConsumer<V, Shape> vertexUpdater;
	private BiConsumer<V, Text> labelUpdater;
	private BiConsumer<E, Path> edgeUpdater;

	public GraphDisplay(Graph<V, E> graph) {
		this.graph = graph;
	}

	public GraphDisplay<V, E> size(double size){
		this.size = size;
		return this;
	}

	public GraphDisplay<V, E> algorithm(LayoutAlgorithm2D <V, E> algorithm){
		if(vertices2D != null) throw new IllegalStateException("algorithm() needs to be called before vertices, text or edges");
		this.algorithm = algorithm;
		return this;
	}

	public GraphDisplay<V, E> vertices(Function <V, Shape> nodeSupplier){
		size = Objects.requireNonNullElse(size, DEFAULT_SIZE);
		algorithm = Objects.requireNonNullElse(algorithm, new RandomLayoutAlgorithm2D<>());
		LayoutModel2D<V> layout = new MapLayoutModel2D<>(new Box2D(size, size));
		algorithm.layout(graph, layout);
		vertices2D = layout.collect();
		nodes = graph.vertexSet().stream().collect(toMap(v -> v, v -> {
			Point2D point2D = vertices2D.get(v);
			Shape node = nodeSupplier.apply(v);
			node.setLayoutX(point2D.getX());
			node.setLayoutY(point2D.getY());
			return node;
		}));
		return this;
	}

	public GraphDisplay<V, E> labels(UnaryOperator<Point2D> placer, Function<V, Text> textMapper){
		labels = graph.vertexSet().stream().collect(toMap(v -> v, v -> {
			Point2D place = placer.apply(vertices2D.get(v));
			Text text = textMapper.apply(v);
			text.setX(place.getX());
			text.setY(place.getY());
			return text;
		}));
		return this;
	}

	public GraphDisplay<V, E> edges(boolean arrow, BiFunction<E, Path, Path> edgeFormatter) {
		edges = graph.edgeSet().stream().collect(toMap(e -> e, e -> {
					Path edge2d = new Edge2D(vertices2D.get(graph.getEdgeSource(e)), vertices2D.get(graph.getEdgeTarget(e)), arrow);
					return edgeFormatter.apply(e, edge2d);
				}));
		return this;
	}

	public GraphDisplay<V, E> edges(BiFunction<E, Path, Path> edgeFormatter) {
		return edges(graph.getType().isDirected(), edgeFormatter);
	}


	public GraphDisplay<V, E> render(){
		if(nodes != null) getChildren().addAll(nodes.values());
		if(edges != null) getChildren().addAll(edges.values());
		if(labels != null) getChildren().addAll(labels.values());
		return this;
	}

	public GraphDisplay<V, E> withVertexUpdater(BiConsumer<V, Shape> vertexUpdater){
		this.vertexUpdater = vertexUpdater;
		return this;
	}

	public GraphDisplay<V, E> withLabelUpdater(BiConsumer<V, Text> labelUpdater){
		this.labelUpdater = labelUpdater;
		return this;
	}

	public GraphDisplay<V, E> withEdgeUpdater(BiConsumer<E, Path> edgeUpdater){
		this.edgeUpdater = edgeUpdater;
		return this;
	}

	public GraphDisplay<V, E> update(){
		if(vertexUpdater != null) nodes.forEach(vertexUpdater);
		if(labelUpdater != null) labels.forEach(labelUpdater);
		if(edgeUpdater != null) edges.forEach(edgeUpdater);
		return this;
	}
}
