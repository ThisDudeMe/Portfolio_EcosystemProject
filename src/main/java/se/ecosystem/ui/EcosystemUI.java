package se.ecosystem.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se.ecosystem.model.Animal;
import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.simulation.SimulationHandler;

import java.util.Map;

public class EcosystemUI extends Application {

    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    private static final int CELL_SIZE = 30;

    private SimulationHandler simulationHandler;

    @Override
    public void start(Stage stage) {
        simulationHandler = new SimulationHandler(GRID_WIDTH, GRID_HEIGHT);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label header = new Label("Ecosystem Simulation");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        root.setTop(header);

        // Create Canvas
        Canvas canvas = new Canvas(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        VBox canvasContainer = new VBox(canvas);
        canvasContainer.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        root.setCenter(canvasContainer);

        Scene scene = new Scene(root);
        stage.setTitle("Ecosystem Simulation");
        stage.setScene(scene);
        stage.show();

        // Start Animation Loop
        startSimulationLoop(canvas.getGraphicsContext2D());
    }

    private void startSimulationLoop(GraphicsContext gc) {
        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Throttle updates to ~5 frames per second (200ms)
                if (now - lastUpdate >= 200_000_000) {
                    simulationHandler.updateSimulation();
                    draw(gc);
                    lastUpdate = now;
                }
            }
        }.start();
    }

    private void draw(GraphicsContext gc) {
        // 1. Clear background
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // 2. Draw Grid Lines
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }

        // 3. Draw Animals
        Map<Animal, SimulationHandler.Point> positions = simulationHandler.getAnimalPositions();
        for (Map.Entry<Animal, SimulationHandler.Point> entry : positions.entrySet()) {
            drawAnimal(gc, entry.getKey(), entry.getValue());
        }
    }

    private void drawAnimal(GraphicsContext gc, Animal animal, SimulationHandler.Point pos) {
        Color color = switch (animal.getType()) {
            case HERBIVORE -> Color.FORESTGREEN;
            case CARNIVORE -> Color.CRIMSON;
            case OMNIVORE -> Color.CORNFLOWERBLUE;
        };

        // Visual feedback for states
        if (animal.getState() == AnimalState.RESTING) {
            color = color.desaturate(); // Fade color when resting
        }

        gc.setFill(color);

        double dotSize = CELL_SIZE * 0.7;
        double x = pos.x() * CELL_SIZE + (CELL_SIZE - dotSize) / 2;
        double y = pos.y() * CELL_SIZE + (CELL_SIZE - dotSize) / 2;

        gc.fillOval(x, y, dotSize, dotSize);

        // Gold border if eating, black otherwise
        if (animal.getState() == AnimalState.EATING) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
        }
        gc.strokeOval(x, y, dotSize, dotSize);
    }
}