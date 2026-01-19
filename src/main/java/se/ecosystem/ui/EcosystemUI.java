package se.ecosystem.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.ecosystem.model.Animal;
import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;
import se.ecosystem.simulation.SimulationHandler;

import java.util.HashMap;
import java.util.Map;

public class EcosystemUI extends Application {

    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    private static final int CELL_SIZE = 30;


    private static final long TICK_DURATION_NS = 200_000_000;

    private SimulationHandler simulationHandler;


    private final Map<Animal, SimulationHandler.Point> previousPositions = new HashMap<>();

    // Stats Labels
    private Label totalLabel;
    private Label herbivoreLabel;
    private Label carnivoreLabel;
    private Label omnivoreLabel;
    private Label avgAgeLabel;

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

        // Create Side Panel (Legend + Stats)
        VBox sidePanel = createSidePanel();
        root.setRight(sidePanel);

        Scene scene = new Scene(root);
        stage.setTitle("Ecosystem Simulation");
        stage.setScene(scene);
        stage.show();

        // Start Animation Loop
        startSimulationLoop(canvas.getGraphicsContext2D());
    }

    private VBox createSidePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 0, 0, 20)); // Left padding to separate from canvas
        panel.setPrefWidth(200);

        // Legend Section
        Label legendTitle = new Label("Legend");
        legendTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        panel.getChildren().addAll(
                legendTitle,
                createLegendItem(Color.FORESTGREEN, "Herbivore"),
                createLegendItem(Color.CRIMSON, "Carnivore"),
                createLegendItem(Color.CORNFLOWERBLUE, "Omnivore"),
                createLegendItem(Color.GOLD, "Eating (Border)")
        );

        // Stats Section
        Label statsTitle = new Label("Population Stats");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 20 0 5 0;");

        totalLabel = new Label("Total: 0");
        herbivoreLabel = new Label("Herbivores: 0");
        carnivoreLabel = new Label("Carnivores: 0");
        omnivoreLabel = new Label("Omnivores: 0");
        avgAgeLabel = new Label("Avg Age: 0 ticks");

        panel.getChildren().addAll(statsTitle, totalLabel, herbivoreLabel, carnivoreLabel, omnivoreLabel, avgAgeLabel);

        return panel;
    }

    private HBox createLegendItem(Color color, String text) {
        Rectangle rect = new Rectangle(20, 20, color);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(1);

        if (color == Color.GOLD) {
            // Special case for "Eating": make it a hollow ring like in the simulation
            rect.setFill(Color.TRANSPARENT);
            rect.setStrokeWidth(3);
        }

        Label label = new Label(text);
        HBox box = new HBox(10, rect, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void startSimulationLoop(GraphicsContext gc) {
        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                try {
                    // Initialize lastUpdate on first frame
                    if (lastUpdate == 0) {
                        lastUpdate = now;
                        // Snapshot initial positions
                        snapshotPositions();
                        return;
                    }

                    long elapsed = now - lastUpdate;

                    // 1. Simulation Update Logic
                    if (elapsed >= TICK_DURATION_NS) {
                        // Snapshot current positions BEFORE they change -> these become "previous"
                        snapshotPositions();

                        // Update simulation (moves animals to NEW positions)
                        simulationHandler.updateSimulation();

                        lastUpdate = now;
                        elapsed = 0;
                    }

                    // 2. Rendering Logic (Interpolation)
                    // Calculate progress (0.0 to 1.0) between ticks
                    double progress = (double) elapsed / TICK_DURATION_NS;

                    // Clamp progress to max 1.0 to prevent overshooting if frame is late
                    if (progress > 1.0) progress = 1.0;

                    draw(gc, progress);
                    updateStats();

                } catch (Exception e) {
                    e.printStackTrace();
                    this.stop();
                }
            }
        }.start();
    }

    private void snapshotPositions() {
        previousPositions.clear();
        // Deep copy the current map positions
        previousPositions.putAll(simulationHandler.getAnimalPositions());
    }

    private void updateStats() {
        var animals = simulationHandler.getAnimalPositions().keySet();

        long herbCount = animals.stream().filter(a -> a.getType() == AnimalType.HERBIVORE).count();
        long carnCount = animals.stream().filter(a -> a.getType() == AnimalType.CARNIVORE).count();
        long omniCount = animals.stream().filter(a -> a.getType() == AnimalType.OMNIVORE).count();

        double averageAge = animals.stream()
                .mapToInt(Animal::getAge)
                .average()
                .orElse(0.0);

        totalLabel.setText("Total: " + animals.size());
        herbivoreLabel.setText("Herbivores: " + herbCount);
        carnivoreLabel.setText("Carnivores: " + carnCount);
        omnivoreLabel.setText("Omnivores: " + omniCount);
        avgAgeLabel.setText(String.format("Avg Age: %.1f ticks", averageAge));
    }

    private void draw(GraphicsContext gc, double progress) {
        // 1. Clear background
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // 1.5 Draw Grass
        boolean[][] grassGrid = simulationHandler.getGrass();
        gc.setFill(Color.LIGHTGREEN);
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (grassGrid[x][y]) {
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // 2. Draw Grid Lines
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }

        // 3. Draw Animals using Linear Interpolation
        var currentPositions = simulationHandler.getAnimalPositions();

        for (Map.Entry<Animal, SimulationHandler.Point> entry : currentPositions.entrySet()) {
            Animal animal = entry.getKey();
            SimulationHandler.Point target = entry.getValue(); // Where we are going (new sim state)
            SimulationHandler.Point start = previousPositions.get(animal); // Where we were (old sim state)

            double drawX, drawY;

            if (start == null) {
                // Newly spawned animal, just draw at target
                drawX = target.x();
                drawY = target.y();
            } else {
                // Linear Interpolation: start + (target - start) * progress
                drawX = start.x() + (target.x() - start.x()) * progress;
                drawY = start.y() + (target.y() - start.y()) * progress;
            }

            drawAnimal(gc, animal, drawX, drawY);
        }
    }

    private void drawAnimal(GraphicsContext gc, Animal animal, double gridX, double gridY) {
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
        // Convert grid coordinates (0.0 - 20.0) to pixel coordinates
        double pixelX = gridX * CELL_SIZE + (CELL_SIZE - dotSize) / 2;
        double pixelY = gridY * CELL_SIZE + (CELL_SIZE - dotSize) / 2;

        gc.fillOval(pixelX, pixelY, dotSize, dotSize);

        // Gold border if eating, black otherwise
        if (animal.getState() == AnimalState.EATING) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
        }
        gc.strokeOval(pixelX, pixelY, dotSize, dotSize);

        // --- Draw Stats ---
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        // Construct stats string: "Age | HP | En | Hu"
        String stats = String.format("A:%d HP:%.0f E:%.0f H:%.0f",
                animal.getAge(),
                animal.getHealth(),
                animal.getEnergy(),
                animal.getHunger());

        // Draw text above the animal (centered)
        double textX = pixelX - 20;
        double textY = pixelY - 5;

        gc.fillText(stats, textX, textY);
    }
}
