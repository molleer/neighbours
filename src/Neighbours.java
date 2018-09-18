import static java.lang.Math.sqrt;
import static java.lang.System.exit;
import static java.lang.System.nanoTime;
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
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    private enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    private enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    private double width = 400, height = 400; // Size for window
    private final long interval = 450000000;
    private long previousTime = nanoTime();
    private final double margin = 50;
    private double dotSize;
    private Actor[][] world;

    public static void main(String[] args) {
        launch(args);
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (square)
        int nLocations = 900;

        // TODO

        // Should be last
        fixScreenSize(nLocations);
    }

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    private void updateWorld() {
        final double threshold = 0.7; // % of surrounding neighbours that are like me

        //TODO copied matrix, logic stuff
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {
                //update
            }
        }
    }


    // ------- Methods ------------------

    // TODO write the methods here, implement/test bottom up

    //Shuffles all unsatisfied actors to new positions in the matrix
    private void shuffleUnsatisfied(Actor[][] world) {

    }

    private void shuffle(Actor[][] world) {
        Random rnd;
        Actor temp;
        int rndCol, rndRow;
        if (world != null) {
            rnd = new Random();
            for (int row = 0; row < world.length; row++) {
                for (int col = 0; col < world[row].length; col++) {
                    rndRow = rnd.nextInt(world.length);
                    rndCol = rnd.nextInt(world[row].length);

                    //Swaps the position of two elements in the matrix
                    temp = world[rndRow][rndCol];
                    world[rndRow][rndCol] = world[row][col];
                    world[row][col] = temp;
                }
            }
        }
    }

    //returns a matrix that displays the current state of each actor in the parameter world
    //TODO: this method might "work", but we should use the enum for State instead
    /*
    private boolean[][] getStates(Actor[][] world, double threshold) {
        boolean[][] result = new boolean[world.length][world.length]; //assuming the matrix is symmetrical
        for (int row = 0; row < result.length; row++)
            for (int col = 0; col < result[row].length; col++)
                if (isSatisfied(getNeighbors(world, col, row), threshold, world[row][col])) {
                    result[row][col] = true;
                } else {
                    result[row][col] = false;
                }
        return result;
    }
    */

    //TODO test this method
    private State[][] getStates(Actor[][] world, double threshold) {
        boolean isSatisfied;
        State[][] result = new State[world.length][world.length]; //assuming the matrix is symmetrical
        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result[row].length; col++) {
                if (world[row][col] == Actor.NONE) {
                    result[row][col] = State.NA;
                } else {
                    isSatisfied = isSatisfied(getNeighbors(world, col, row), threshold, world[row][col]);
                    if (isSatisfied) {
                        result[row][col] = State.SATISFIED;
                    } else {
                        result[row][col] = State.UNSATISFIED;
                    }
                }
            }
        }
        return result;
    }

    //indicates whether or not a specific Actor is satisfied
    private boolean isSatisfied(Actor[] neighbors, double threshold, Actor actor) {
        int nSat, nUnsat; //number of satisfied actors / number of unsatisfied actors
        int nRelevantNeighbors;
        double ratio;
        if (actor == Actor.NONE) {
            return false;
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
            //TODO is an actor happy with no neighbors?
            nRelevantNeighbors = nSat + nUnsat;
            ratio = (double) nSat / (double) nRelevantNeighbors;
            if (nUnsat == 0 && nSat == 0 || ratio >= threshold) {
                return true;
            } else {
                return false;
            }
        }
    }

    //Returns the neighbors of the actor at the specified coordinates as an array
    private Actor[] getNeighbors(Actor[][] world, int col, int row) {
        ArrayList<Actor> neighbors = new ArrayList();
        Actor[] result;

        if (col - 1 >= 0) {
            //left
            neighbors.add(world[row][col - 1]);
            if (row + 1 < world.length) {
                //down,left
                neighbors.add(world[row + 1][col - 1]);
            }
            if (row - 1 >= 0) {
                //up, left
                neighbors.add(world[row - 1][col - 1]);
            }
        }

        if (col + 1 < world[row].length) {
            //right
            neighbors.add(world[row][col + 1]);
            if (row - 1 >= 0) {
                //up, right
                neighbors.add(world[row - 1][col + 1]);
            }
            if (row + 1 < world.length) {
                //down,right
                neighbors.add(world[row + 1][col + 1]);
            }
        }
        if (row - 1 >= 0) {
            //up
            neighbors.add(world[row - 1][col]);
        }
        if (row + 1 < world.length) {
            //down
            neighbors.add(world[row + 1][col]);
        }
        result = new Actor[neighbors.size()];
        for (int i = 0; i < neighbors.size(); i++) {
            result[i] = neighbors.get(i);
        }
        return result;
    }

    //Creates a world
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

        //TODO will need to shuffle the matrix after creation
        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result.length; col++) {
                //First add all red actors, then all blue actors and finally all "none" actors
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
        System.out.println("World size: " + worldSize);
        System.out.println("maxRed:  " + maxRed + "  | nRed:  " + nRed);
        System.out.println("maxBlue: " + maxBlue + "  | nBlue: " + nBlue);
        System.out.println("maxNone: " + maxNone + "  | nNone: " + nNone);
        System.out.println("---");

        return result;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    private void test() {
        // A small hard coded world for testing
//        Actor[][] testWorld = new Actor[][]{
//                {Actor.RED, Actor.RED, Actor.NONE},
//                {Actor.NONE, Actor.BLUE, Actor.NONE},
//                {Actor.RED, Actor.NONE, Actor.BLUE}
//        };
//        double th = 0.5;   // Simple threshold used for testing
//        int size = testWorld.length;
//        double[] dist = {0.25, 0.25, 0.50};
//
//
//        // TODO test methods
//        //Actor[][] testWorld = createWorld(dist, 900);
//        for (int row = 0; row < testWorld.length; row++) {
//            for (int col = 0; col < testWorld[row].length; col++)
//                if (testWorld[row][col] == Actor.BLUE)
//                    System.out.print("B ");
//                else if (testWorld[row][col] == Actor.RED)
//                    System.out.print("R ");
//                else
//                    System.out.print("N ");
//            System.out.println();
//        }
//        System.out.println();
//
////        boolean[][] satisfied = getStates(testWorld, th);
//        State[][] states = getStates(testWorld, th);
//
//        for (int row = 0; row < states.length; row++) {
//            for (int col = 0; col < states[row].length; col++) {
//                if (states[row][col] == State.SATISFIED) {
//                    System.out.print("S ");
//                } else if (states[row][col] == State.UNSATISFIED) {
//                    System.out.print("U ");
//                } else {
//                    System.out.println("N"); //NA
//                }
//            }
//            System.out.println();
//        }
        double th = 0.5;   // Simple threshold used for testing
        double[] dist = {0.25, 0.25, 0.50};
        Actor[][] world = createWorld(dist, 100);


        //Prints the world matrix two times, first time: raw, second time: shuffled
        for (int a = 0; a < 2; a++) {
            for (int i = 0; i < world.length; i++) {
                for (int j = 0; j < world[i].length; j++) {
                    System.out.print(world[i][j] + " ");
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

        State[][] states = getStates(world, 0.7);
        for (int row = 0; row < states.length; row++) {
            for (int col = 0; col < states[row].length; col++) {
                if (states[row][col] == State.UNSATISFIED) {
                    System.out.print("U ");
                } else if (states[row][col] == State.SATISFIED) {
                    System.out.print("S ");
                } else if (states[row][col] == State.NA) {
                    System.out.print("N ");
                }
            }
            System.out.println();
        }

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    private <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // *****   NOTHING to do below this row, it's JavaFX stuff  ******

    private void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

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
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }
}
