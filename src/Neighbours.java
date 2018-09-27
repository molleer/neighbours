import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 */
public class Neighbours extends Application {

    private enum Actor {
        BLUE, RED, NONE // NONE used for empty locations
    }

    private enum State {
        UNSATISFIED, SATISFIED, NA // Not applicable (NA), used for NONEs
    }

    private final long interval = 32_000_000;
    private long previousTime = System.nanoTime();
    private final double width = 400, height = 400;
    private double dotSize;
    private Actor[][] world;

    public static void main(String[] args) {
        launch(args);
    }

    // This method initializes the world variable with a random distribution of Actors
    @Override
    public void init() {
        double[] dist = {0.5, 0.3, 0.2}; // %-distribution of RED, BLUE and NONE
        int nLocations = 90_000;  // Number of positions in world
        world = createWorld(dist, nLocations);
        shuffle(world);
        calculateDotSize(nLocations);
    }

    // This is the method called by the timer to update the world
    private void updateWorld() {
        final double threshold = 0.750;
        State[][] states = getStates(world, threshold);
        shuffleUnsatisfied(world, states); //convert all unsatisfied actors into an array
    }

    //Make this return a new matrix
    private void shuffleUnsatisfied(Actor[][] matrix, State[][] states) {
        Random rnd;
        Actor temp;
        List<Point> nonePositions, unsatisfiedPositions;
        int rndIndex, rndRow, rndCol;

        if (matrix != null && states != null) {
            nonePositions = new ArrayList<>();
            unsatisfiedPositions = new ArrayList<>();
            rnd = new Random();

            //Retrieves the positions of all NONE and UNSATISFIED actors
            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    if (matrix[row][col] == Actor.NONE) {
                        nonePositions.add(new Point(col, row));
                    }
                    if (states[row][col] == State.UNSATISFIED) {
                        unsatisfiedPositions.add(new Point(col, row));
                    }
                }
            }

            for (int i = 0; i < unsatisfiedPositions.size(); i++) {
                if (nonePositions.size() > 0) {
                    rndIndex = rnd.nextInt(nonePositions.size());
                    rndRow = (int) nonePositions.get(rndIndex).getY();
                    rndCol = (int) nonePositions.get(rndIndex).getX();

                    int row = (int) unsatisfiedPositions.get(i).getY();
                    int col = (int) unsatisfiedPositions.get(i).getX();

                    temp = matrix[row][col];
                    matrix[row][col] = matrix[rndRow][rndCol];
                    matrix[rndRow][rndCol] = temp;

                    nonePositions.remove(rndIndex);
                    nonePositions.add(new Point(col, row));
                }
            }
        }
    }

    private void shuffle(Actor[][] matrix) {
        Random rnd;
        Actor temp;
        int rndCol, rndRow;
        if (matrix != null) {
            rnd = new Random();
            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    rndRow = rnd.nextInt(matrix.length);
                    rndCol = rnd.nextInt(matrix[row].length);

                    //Swaps the position of two elements in the matrix
                    temp = matrix[rndRow][rndCol];
                    matrix[rndRow][rndCol] = matrix[row][col];
                    matrix[row][col] = temp;
                }
            }
        }
    }

    private State[][] getStates(Actor[][] world, double threshold) {
        State[][] states = new State[world.length][world.length]; //assuming the matrix is symmetrical
        for (int row = 0; row < states.length; row++) {
            for (int col = 0; col < states[row].length; col++) {
                states[row][col] = getState(getNeighbors(world, col, row), threshold, world[row][col]);
            }
        }
        return states;
    }

    private State getState(Actor[] neighbors, double threshold, Actor actor) {
        int nSameNeighbors, nDiffNeighbors, nRelevantNeighbors;
        double ratio;
        if (actor == Actor.NONE) {
            return State.NA;
        } else {
            nSameNeighbors = nDiffNeighbors = 0;
            for (Actor a : neighbors) {
                if (a == Actor.NONE) {
                    continue; //NONEs don't count
                } else if (actor == a) {
                    nSameNeighbors++; //this is the amount of neighbors of the same "type" (Red or Blue)
                } else {
                    nDiffNeighbors++;
                }
            }
            nRelevantNeighbors = nSameNeighbors + nDiffNeighbors; //NONE neighbors are disregarded
            ratio = (double) nSameNeighbors / (double) nRelevantNeighbors;
            if (nDiffNeighbors == 0 && nSameNeighbors == 0 || ratio >= threshold) {
                return State.SATISFIED;
            } else {
                return State.UNSATISFIED;
            }
        }
    }

    //Returns the neighbors of the actor at the specified coordinates as an array
    private Actor[] getNeighbors(Actor[][] world, int col, int row) {
        List<Actor> neighbors = new ArrayList();
        Actor[] result;
        int r, c;
        for (int i = -1; i < 2; i++) {
            r = row + i;
            for (int j = -1; j < 2; j++) {
                c = col + j;
                if (i == 0 && j == 0) {
                    continue; //r and c would be equal to row and col (same position as we're getting the neighbors for)
                }
                if (r >= 0 && c >= 0 && r < world.length && c < world[r].length) {
                    neighbors.add(world[r][c]);
                }
            }
        }
        result = new Actor[neighbors.size()];
        for (int i = 0; i < neighbors.size(); i++) {
            result[i] = neighbors.get(i);
        }
        return result;
    }

    //Creates a matrix containing Actors according to the specified distribution and amount
    private Actor[][] createWorld(double[] distribution, int nLocations) {
        Actor[][] result;
        int worldSize, maxRed, maxBlue, maxNone;
        int nRed = 0, nBlue = 0, nNone = 0;

        //distribution: 0 = RED, 1 = BLUE, 2 = NONE
        maxRed = (int) Math.round(distribution[0] * nLocations);
        maxBlue = (int) Math.round(distribution[1] * nLocations);
        maxNone = (int) Math.round(distribution[2] * nLocations);

        worldSize = (int) Math.round(Math.sqrt(nLocations));
        result = new Actor[worldSize][worldSize];

        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result.length; col++) {
                //First add all red actors, then all blue actors and then finally all "none" actors
                if (nRed < maxRed) {
                    result[row][col] = Actor.RED;
                    nRed++;
                } else if (nBlue < maxBlue) {
                    result[row][col] = Actor.BLUE;
                    nBlue++;
                } else if (nNone < maxNone) {
                    result[row][col] = Actor.NONE;
                    nNone++;
                }
            }
        }
        return result;
    }

    private void calculateDotSize(int nLocations) {
        dotSize = (width - 2) / Math.sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Group root;
        Canvas canvas;
        GraphicsContext gc;
        AnimationTimer timer;
        Scene scene;

        root = new Group();
        canvas = new Canvas(width - (dotSize / 2), height - (dotSize / 2)); //minor offset to fix window size mismatch
        root.getChildren().addAll(canvas);
        gc = canvas.getGraphicsContext2D();

        timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Segregation");
        primaryStage.show();

        timer.start();  // Start simulation
    }

    // Render the state of the world to the screen
    private void renderWorld(GraphicsContext g, Actor[][] matrix) {
        double x, y;
        g.clearRect(0, 0, width, height);
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                x = dotSize * col;
                y = dotSize * row;
                if (matrix[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (matrix[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else if (matrix[row][col] == Actor.NONE) {
                    continue; //don't draw NONEs (as they have the same color as the background)
                }
                g.fillRect(x, y, dotSize, dotSize);
            }
        }
    }
}