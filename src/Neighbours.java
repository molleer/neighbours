import java.awt.Point;
import java.util.ArrayList;
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

    private double width = 400, height = 400; // Size for window
    private final long interval = 200_000_000;
    private long previousTime = System.nanoTime();
    //    private final double margin = 50;
    private double dotSize;
    private Actor[][] world;

    public static void main(String[] args) {
        launch(args);
    }

    // This method initializes the world variable with a random distribution of Actors
    @Override
    public void init() {
        double[] dist = {0.55, 0.35, 0.10}; // %-distribution of RED, BLUE and NONE
        int nLocations = 90_000;  // Number of locations (places) in world (square)

        world = createWorld(dist, nLocations);
        shuffle(world);
        calculateDotSize(nLocations);
    }

    // This is the method called by the timer to update the world
    private void updateWorld() {
        final double threshold = 0.625;
        State[][] states = getStates(world, threshold);
        shuffleUnsatisfied(world, states); //convert all unsatisfied actors into an array
    }

    //Shuffles all unsatisfied actors to new positions in the matrix
    //TODO see if this could be improved and made more efficient
    private void shuffleUnsatisfied(Actor[][] matrix, State[][] states) {
        Random rnd;
        Actor temp;
        ArrayList<Point> openList;
        int rndIndex;
        if (matrix != null) {
            openList = new ArrayList();
            rnd = new Random();
            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    if (matrix[row][col] == Actor.NONE || states[row][col] == State.UNSATISFIED
                            || states[row][col] == State.NA) {
                        openList.add(new Point(col, row));
                    }
                }
            }
            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    if (matrix[row][col] == Actor.NONE || states[row][col] == State.UNSATISFIED
                            || states[row][col] == State.NA) {

                        rndIndex = rnd.nextInt(openList.size());

                        temp = matrix[row][col];

                        int r = (int) openList.get(rndIndex).getY();
                        int c = (int) openList.get(rndIndex).getX();
                        matrix[row][col] = matrix[r][c];

                        matrix[r][c] = temp;

                        openList.remove(rndIndex);
                        openList.add(new Point(col, row));
                    }
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
//                System.out.println("Actor value: " + world[row][col]);
//                System.out.println(Arrays.toString(getNeighbors(world, col, row)));
//                System.out.println("Threshold: " + threshold);
                states[row][col] = getState(getNeighbors(world, col, row), threshold, world[row][col]);
//                System.out.println("Result state: " + states[row][col]);
//                System.out.println("-");
            }
        }
        return states;
    }

    //TODO see if this could be compressed
    private State getState(Actor[] neighbors, double threshold, Actor actor) {
        int nSat, nUnsat, nRelevantNeighbors;
        double ratio;
        if (actor == Actor.NONE) {
            return State.NA;
        } else {
            nSat = nUnsat = 0;
            for (Actor a : neighbors) {
                if (a == Actor.NONE) {
                    continue; //NONEs don't count
                } else if (actor == Actor.BLUE && a == Actor.BLUE || actor == Actor.RED && a == Actor.RED) {
                    nSat++;
                } else {
                    nUnsat++;
                }
            }
            nRelevantNeighbors = nSat + nUnsat; //NONE neighbors are disregarded
            ratio = (double) nSat / (double) nRelevantNeighbors;
            if (nUnsat == 0 && nSat == 0 || ratio >= threshold) {
                return State.SATISFIED;
            } else {
                return State.UNSATISFIED;
            }
        }
    }

    //Returns the neighbors of the actor at the specified coordinates as an array
    private Actor[] getNeighbors(Actor[][] world, int col, int row) {
        ArrayList<Actor> neighbors = new ArrayList();
        Actor[] result;
        if (col - 1 >= 0) {
            neighbors.add(world[row][col - 1]); //left
            if (row + 1 < world.length) {
                neighbors.add(world[row + 1][col - 1]); //down,left
            }
            if (row - 1 >= 0) {
                neighbors.add(world[row - 1][col - 1]); //up, left
            }
        }
        if (col + 1 < world[row].length) {
            neighbors.add(world[row][col + 1]); //right
            if (row - 1 >= 0) {
                neighbors.add(world[row - 1][col + 1]); //up, right
            }
            if (row + 1 < world.length) {
                neighbors.add(world[row + 1][col + 1]);  //down,right
            }
        }
        if (row - 1 >= 0) {
            neighbors.add(world[row - 1][col]); //up
        }
        if (row + 1 < world.length) {
            neighbors.add(world[row + 1][col]); //down
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
//        System.out.println("World size: " + worldSize);
//        System.out.println("maxRed:  " + maxRed + "  | nRed:  " + nRed);
//        System.out.println("maxBlue: " + maxBlue + "  | nBlue: " + nBlue);
//        System.out.println("maxNone: " + maxNone + "  | nNone: " + nNone);
//        System.out.println("---");
        return result;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    private void test() {
        double threshold = 0.875;
        double[] dist = {0.49, 0.48, 0.01}; //Red, Blue & None
        Actor[][] world = createWorld(dist, 100);

        //Prints the world matrix two times, first time: raw, second time: shuffled
        for (int a = 0; a < 2; a++) {
            for (int i = 0; i < world.length; i++) {
                for (int j = 0; j < world[i].length; j++) {
//                    System.out.print(world[i][j] + " ");
                    if (world[i][j] == Actor.RED) {
                        System.out.print("R ");
                    } else if (world[i][j] == Actor.BLUE) {
                        System.out.print("B ");
                    } else {
                        System.out.print("_ ");
                    }
                }
                System.out.println();
            }
            System.out.println("---");
            shuffle(world);
        }

        //Checks whether the distribution is still correct
        int r = 0, b = 0, n = 0;
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                if (world[i][j] == Actor.RED) {
                    r++;
                } else if (world[i][j] == Actor.BLUE) {
                    b++;
                } else if (world[i][j] == Actor.NONE) {
                    n++;
                }
            }
        }
        System.out.println("(After shuffle)");
        System.out.println("Reds:  " + r);
        System.out.println("Blues: " + b);
        System.out.println("Nones: " + n);
        System.out.println("---");

        //Prints the state of all of the actors in the world
        State[][] states = getStates(world, threshold);
        for (int row = 0; row < states.length; row++) {
            for (int col = 0; col < states[row].length; col++) {
                if (states[row][col] == State.UNSATISFIED) {
                    System.out.print("U ");
                } else if (states[row][col] == State.SATISFIED) {
                    System.out.print("S ");
                } else if (states[row][col] == State.NA) {
                    System.out.print("_ ");
                }
            }
            System.out.println();
        }

        System.exit(0);
    }

//    // Helper method for testing (NOTE: reference equality)
//    private <T> int count(T[] arr, T toFind) {
//        int count = 0;
//        for (int i = 0; i < arr.length; i++) {
//            if (arr[i] == toFind) {
//                count++;
//            }
//        }
//        return count;
//    }

    private void calculateDotSize(int nLocations) {
        dotSize = (width - 2 /** margn*/) / Math.sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
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

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }

    // Render the state of the world to the screen
    private void renderWorld(GraphicsContext g, Actor[][] world) {
        double x, y;
        g.clearRect(0, 0, width, height);
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {
                x = dotSize * col /*+ margin*/;
                y = dotSize * row /*+ margin*/;
                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    continue; //don't draw NONEs
                }
                g.fillRect(x, y, dotSize, dotSize);
            }
        }
    }
}