package graphs;

/**
 * Simple class to store pair of Doubles, referred as x and y.
 */
public class Coord {
    private Double x;
    private Double y;

    public Coord(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coord{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
