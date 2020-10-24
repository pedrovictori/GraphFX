package pedrovictori.graphfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class UsageExample extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		Graph<Character, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		//Create a graph
		graph.addVertex('A');
		graph.addVertex('B');
		graph.addVertex('C');
		graph.addVertex('D');
		graph.addVertex('E');
		graph.addVertex('1');
		graph.addVertex('2');
		graph.addVertex('3');

		graph.addEdge('A', 'B');
		graph.addEdge('A', 'C');
		graph.addEdge('A', 'E');
		graph.addEdge('A', '3');
		graph.addEdge('B', '1');
		graph.addEdge('B', 'C');
		graph.addEdge('C', 'E');
		graph.addEdge('C', 'D');
		graph.addEdge('D', 'E');
		graph.addEdge('D', '1');
		graph.addEdge('D', '2');
		graph.addEdge('E', '1');
		graph.addEdge('E', '3');
		graph.addEdge('1', 'C');
		graph.addEdge('2', 'C');

		//Build the graph display
		GraphDisplay<Character, DefaultEdge> graphDisplay = (new GraphDisplay<>(graph))
				.size(500)
				.algorithm(new FRLayoutAlgorithm2D<>())
				.vertices(character -> new Circle(20, Character.isDigit(character) ? Color.RED : Color.BLUE))
				.labels(point2D -> new Point2D(point2D.getX(), point2D.getY() - 35), character -> new Text(character.toString()))
				.edges(true, (edge, path) -> {
					path.setFill(Character.isDigit(graph.getEdgeSource(edge)) ? Color.DARKRED : Color.DARKBLUE);
					path.getStrokeDashArray().addAll(20., 8.);
					path.setStrokeWidth(2);
					return path;
				})
				.withActionOnClick(ActionOnClick.HIGHLIGHT_OUTGOING_EDGES)
				.withCustomActionOnClick((character, shape) -> System.out.println(character));
		graphDisplay.render();

		//Build JavaFX scene
		primaryStage.setTitle("Hello World!");
		StackPane root = new StackPane();
		root.setPadding(new Insets(20));
		root.getChildren().add(graphDisplay);
		primaryStage.setScene(new Scene(root, 600, 600));
		primaryStage.show();
	}
}
