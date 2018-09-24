import static java.lang.Math.sqrt;
import static java.lang.System.exit;
import static java.lang.System.nanoTime;

import java.awt.*;
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
    private double width = 800, height = 800; // Size for window
    private final long interval = 5_000_000;
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
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.45, 0.45, 0.10};
        // Number of locations (places) in world (square)
        int nLocations = 90_000;
        world = createWorld(dist, nLocations);
        shuffle(world);

        // Should be last
        fixScreenSize(nLocations);
    }


    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    private void updateWorld() {
        Point pUnRnB[] = new Point[world.length * world[0].length];
        Point pNone[] = new Point[world.length * world[0].length];
        int nNone = 0, nUnRnB = 0;
        State[][] states = getStates(world, 0.7);
        Random rnd = new Random();
        int randomN, temp;

        for (int i = 0; i < states.length; i++)
            for (int k = 0; k < states[i].length; k++) {
                switch (states[i][k]) {
                    case SATISFIED:
                        continue;
                    case UNSATISFIED:
                        pUnRnB[nUnRnB] = new Point();
                        pUnRnB[nUnRnB].x = k;
                        pUnRnB[nUnRnB++].y = i;
                        break;
                    case NA:
                        pNone[nNone] = new Point();
                        pNone[nNone].x = k;
                        pNone[nNone++].y = i;
                        break;
                }
            }

        for(int i=0; i < nUnRnB; i++)
        {
            randomN = rnd.nextInt(nNone);

            world[pNone[randomN].y][pNone[randomN].x] = world[pUnRnB[i].y][pUnRnB[i].x];
            world[pUnRnB[i].y][pUnRnB[i].x] = Actor.NONE;

            temp = pUnRnB[i].x;
            pUnRnB[i].x = pNone[randomN].x;
            pNone[randomN].x = temp;

            temp = pUnRnB[i].y;
            pUnRnB[i].y = pNone[randomN].y;
            pNone[randomN].y = temp;
        }

    }


    // ------- Methods ------------------

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

    //TODO test this method
    private State[][] getStates(Actor[][] world, double threshold) {
        boolean isSatisfied;
        State[][] result = new State[world.length][world.length]; //assuming the matrix is symmetrical
        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result[row].length; col++) {
                if (world[row][col] == Actor.NONE) {
                    result[row][col] = State.NA; //NA is the default value for an actor of the type NONE
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
            nRelevantNeighbors = nSat + nUnsat; //NONE neighbors are disregarded
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
                    continue;
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }
}
