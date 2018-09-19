import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

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

    // Enumeration type for the Actors
    private enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
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
    private Actor[][] world;  // The world is a square matrix of Actors
    // world[row][col]

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
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;
        // TODO
    }


    // ------- Methods ------------------

    // TODO write the methods here, implement/test bottom up

    //Shuffles all unsatisfied actors to new positions in the matrix
    private void shuffleUnsatisfied(Actor[][] world) {

    }

    //returns a matrix that displays the current state of each actor in the parameter world
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

    //indicates whether or not a specific Actor is satisfied
    private boolean isSatisfied(Actor[] neighbors, double threshold, Actor actor) {
        int nSat, nUnsat; //number of satisfied actors / number of unsatisfied actors
        int nRelevantNeighbors;
        double ratio;
        if (actor == Actor.NONE) {
            return false;
        } else {
            nSat = nUnsat = 0;
            for (Actor a : neighbors)
                if (a == Actor.NONE) {
                    continue;
                } else if (actor == Actor.BLUE && a == Actor.BLUE || actor == Actor.RED && a == Actor.RED) {
                    nSat++;
                } else {
                    nUnsat++;
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
        ArrayList<Actor> neiBr = new ArrayList();
        if (col - 1 >= 0) {
            //left

            neiBr.add(world[row][col - 1]);
            if (row + 1 < world.length)
                //down,left
                neiBr.add(world[row + 1][col - 1]);
            if (row - 1 >= 0)
                //up, left
                neiBr.add(world[row - 1][col - 1]);
        }

        if (col + 1 < world[row].length) {
            //right
            neiBr.add(world[row][col + 1]);
            if (row - 1 >= 0)
                //up, right
                neiBr.add(world[row - 1][col + 1]);
            if (row + 1 < world.length)
                //down,right
                neiBr.add(world[row + 1][col + 1]);
        }
        if (row - 1 >= 0)
            //up
            neiBr.add(world[row - 1][col]);
        if (row + 1 < world.length)
            //down
            neiBr.add(world[row + 1][col]);

        Actor[] ans = new Actor[neiBr.size()];
        for (int i = 0; i < neiBr.size(); i++)
            ans[i] = neiBr.get(i);

        return ans;
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
        System.out.println("maxRed: " + maxRed + "   | nRed: " + nRed);
        System.out.println("maxBlue: " + maxBlue + "  | nBlue: " + nBlue);
        System.out.println("maxNone: " + maxNone + " | nNone: " + nNone);
        return result;
    }

    private void shuffle(Actor[][] world) {
        
    }

    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    private void test() {
        // A small hard coded world for testing
        /*Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };*/

        double th = 0.5;   // Simple threshold used for testing

        //int size = testWorld.length;
        double[] dist = {0.25, 0.25, 0.50};
        Actor[][] testWorld=createWorld(dist,100);

        // TODO test methods
        //Actor[][] testWorld = createWorld(dist, 900);
        for(int row=0; row<testWorld.length; row++) {
            for (int col = 0; col < testWorld[row].length; col++)
                if(testWorld[row][col]==Actor.BLUE)
                    System.out.print("B ");
                else if(testWorld[row][col]==Actor.RED)
                    System.out.print("R ");
                else
                    System.out.print("N ");
            System.out.println();
        }

        System.out.println('\n');
        boolean[][] satisfied = getStates(testWorld,th);

        for(int row=0; row<satisfied.length; row++) {
            for (int col = 0; col < satisfied[row].length; col++)
                if(satisfied[row][col])
                    System.out.print("X ");
                else
                    System.out.print("O ");

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
