package pedrovictori;

import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.jgrapht.alg.drawing.RandomLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.RescaleLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

import static java.util.stream.Collectors.toMap;

public class GraphDisplay<V, E> extends Region {
	/**
	 * The default side size for the diagram
	 */
	private static final double DEFAULT_SIZE = 500;
	private double size;
	private final Graph<V, E> graph;
	Map<V, Point2D> vertices2D;
	Map<V, Shape> nodes;
	Map<V, Text> labels;
	Map<E, Path> edges;
	LayoutAlgorithm2D<V, E> algorithm;
	private BiConsumer<V, Shape> vertexUpdater;
	private BiConsumer<V, Text> labelUpdater;
	private BiConsumer<E, Path> edgeUpdater;
	private Function<V, Shape> nodeSupplier;
	private UnaryOperator<Point2D> labelPlacer;
	private Function<V, Text> textMapper;
	private boolean arrow;
	private BiFunction<E, Path, Path> edgeFormatter;
	private LayoutModel2D<V> layout;
	private ActionOnClick actionOnClick;
	private Shape lastShapeClicked;
	private V lastVertexClicked;
	private BiConsumer<V, Shape> customActionOnClick;
	private BiConsumer<V, Shape> customActionOnClickReset;

	public GraphDisplay(Graph<V, E> graph) {
		this.graph = graph;
	}

	public GraphDisplay<V, E> size(double size){
		this.size = size;
		return this;
	}

	public GraphDisplay<V, E> algorithm(LayoutAlgorithm2D <V, E> algorithm){
		if(vertices2D != null) throw new IllegalStateException("algorithm(...) needs to be called before vertices, text or edges");
		this.algorithm = algorithm;
		return this;
	}

	public GraphDisplay<V, E> vertices(Function <V, Shape> nodeSupplier){
		this.nodeSupplier = nodeSupplier;
		return this;
	}

	private void layoutGraph() {
		size = Objects.requireNonNullElse(size, DEFAULT_SIZE);
		algorithm = Objects.requireNonNullElse(algorithm, new RandomLayoutAlgorithm2D<>());
		layout = new MapLayoutModel2D<>(new Box2D(size, size));
		algorithm.layout(graph, layout);
	}

	private Map<V, Shape> produceVertices(){
		Objects.requireNonNull(layout, "Need to call layoutGraph before calling this for the first time");
		vertices2D = layout.collect();
		return nodes = graph.vertexSet().stream().collect(toMap(v -> v, v -> {
			Point2D point2D = vertices2D.get(v);
			Shape node = nodeSupplier.apply(v);
			node.setLayoutX(point2D.getX());
			node.setLayoutY(point2D.getY());
			return node;
		}));
	}

	public GraphDisplay<V, E> labels(UnaryOperator<Point2D> labelPlacer, Function<V, Text> textMapper){
		this.labelPlacer = labelPlacer;
		this.textMapper = textMapper;
		return this;
	}

	private Map<V, Text> produceLabels(){
		return labels = graph.vertexSet().stream().collect(toMap(v -> v, v -> {
			Point2D place = labelPlacer.apply(vertices2D.get(v));
			Text text = textMapper.apply(v);
			text.setX(place.getX());
			text.setY(place.getY());
			return text;
		}));
	}

	public GraphDisplay<V, E> edges(boolean arrow, BiFunction<E, Path, Path> edgeFormatter) {
		this.arrow = arrow;
		this.edgeFormatter = edgeFormatter;
		return this;
	}

	public GraphDisplay<V, E> edges(BiFunction<E, Path, Path> edgeFormatter) {
		return edges(graph.getType().isDirected(), edgeFormatter);
	}

	public Map<E, Path> produceEdges(){
		return edges = graph.edgeSet().stream().collect(toMap(e -> e, e -> {
			Path edge2d = new Edge2D(vertices2D.get(graph.getEdgeSource(e)), vertices2D.get(graph.getEdgeTarget(e)), arrow);
			return edgeFormatter.apply(e, edge2d);
		}));
	}

	public void render(){ //todo this should be the step that returns the finished object, refactor
		layoutGraph();
		setElements();
	}

	private void setElements(){
		getChildren().clear();
		if(nodeSupplier != null) {
			getChildren().addAll(produceVertices().values());

			//click events
			if(actionOnClick != null || customActionOnClick != null) {
				for (Shape shape : nodes.values()) {
					shape.setOnMouseClicked(event -> {
						V clicked = nodes.entrySet().stream().filter(entry -> entry.getValue().equals(shape)).findAny().orElseThrow().getKey(); //find the vertex represented by this shape. The nodes map contains just one shape for every vertex.
						if(actionOnClick != null) {
							if (shape.equals(lastShapeClicked)) {
								actionOnClick.reset(this);
							} else {
								actionOnClick.execute(this, clicked);
							}
						}
						if(customActionOnClick != null) {
							if (shape.equals(lastShapeClicked)) {
								customActionOnClickReset.accept(clicked, shape);
							} else {
								if(lastShapeClicked != null) customActionOnClickReset.accept(lastVertexClicked, lastShapeClicked);
								customActionOnClick.accept(clicked, shape);
							}
						}

						lastShapeClicked = shape.equals(lastShapeClicked) ? null : shape; //if a reset happened in this click, also set lastShapeClicked to null
						lastVertexClicked = clicked;
					});
				}
			}
		}
		if(edgeFormatter != null) getChildren().addAll(produceEdges().values());
		if(textMapper != null) getChildren().addAll(produceLabels().values());

	}

	public void rescale(double scale){
		(new RescaleLayoutAlgorithm2D<V, E>(scale)).layout(graph, layout);
		setElements();

	}

	public void rescale(double scale, Consumer<Shape> vertexRescaler){
		rescale(scale);
		nodes.values().forEach(vertexRescaler);
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

	public void update(){
		if(vertexUpdater != null) nodes.forEach(vertexUpdater);
		if(labelUpdater != null) labels.forEach(labelUpdater);
		if(edgeUpdater != null) edges.forEach(edgeUpdater);
	}

	public GraphDisplay<V, E> withActionOnClick(ActionOnClick action){
		actionOnClick = action;
		return this;
	}

	public GraphDisplay<V, E> withCustomActionOnClick(BiConsumer<V, Shape> action){
		customActionOnClick = action;
		return this;
	}

	/**
	 * Define a function to be executed before every custom action on click.
	 * @param reset the function to be executed before every custom action on click. This consumer function takes the last vertex to be clicked, and its Shape
	 * @return this GraphDisplay
	 */
	public GraphDisplay<V, E> withCustomActionOnClickReset(BiConsumer<V, Shape> reset){
		customActionOnClickReset = reset;
		return this;
	}

	protected Graph<V, E> getGraph(){
		return graph;
	}

	protected Map<V, Shape> getNodes() {
		return Objects.requireNonNull(nodes, "GraphDisplay hasn't been rendered yet");
	}

	protected Map<V, Text> getLabels() {
		return Objects.requireNonNull(labels, "GraphDisplay hasn't been rendered yet");
	}

	protected Map<E, Path> getEdges() {
		return Objects.requireNonNull(edges, "GraphDisplay hasn't been rendered yet");
	}
}
