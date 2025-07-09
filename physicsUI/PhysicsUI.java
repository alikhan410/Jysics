package physicsUI;

import PhysicsWorld.PhysicsWorld;
import engineLoop.EngineLoop;
import enums.BodyType;
import enums.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import shapes.Ball;
import shapes.Square;

import java.util.*;


public class PhysicsUI {
// =================== UI ELEMENTS ===================

    HBox uiBox = new HBox(5); // UI container for buttons and inputs
    Map<String, Button> buttons = new HashMap<>(); // Button map for toggling objects
    private final int UI_BOX_HEIGHT = Constants.UI_BOX_HEIGHT; // Height reserved for the UI bar

// =================== SCENE DIMENSIONS ===================

    final double sceneWidth;
    final double sceneHeight;

// =================== SIMULATION ELEMENTS ===================

    Pane simulationPane; // Main simulation area
    PhysicsWorld physicsWorld; // Physics world managing objects
    EngineLoop engineLoop; // Update loop for simulation

// =================== BALL AND RECTANGLE TOGGLE ===================

    boolean isRectangle = false; // Whether rectangle mode is active
    boolean isBall = true;       // Whether ball mode is active

// =================== MOUSE INTERACTION ===================

    double clickX, clickY; // Last clicked position (used for placing objects)
    double dragStartX, dragStartY; // Starting position of a drag
    boolean isDragging = false; // Whether the user is currently dragging
    double dragThreshold = Constants.DRAG_THRESHOLD; // Minimum distance to consider an action as a drag

// =================== DRAG PREVIEW ELEMENTS ===================

    private final Line dragLine = new Line(); // Line showing launch direction for balls
    private final Rectangle dragPreviewRect = new Rectangle(); // Preview box for rectangles

// =================== PHYSICS PARAMETERS ===================

    private static final double DEFAULT_VELOCITY_SCALE = 4.0;// Multiplier for launch velocity based on drag length (0.05–0.5 recommended)

    // =================== MISC VISUALS ===================
    private final TextField radiusField = new TextField();
    private final TextField massField = new TextField();
    private ComboBox<BodyType> bodyTypeSelector = new ComboBox<>();;

    public PhysicsUI(double sceneHeight, double sceneWidth, PhysicsWorld physicsWorld, EngineLoop engineLoop, Pane pane) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.physicsWorld = physicsWorld;
        this.engineLoop = engineLoop;
        this.simulationPane = pane;


        setUpCursorAndLaunch();
        setUpUIBox();
        setUpButtons();
        setUpInputs();
        setUpActions();

    }

    void setUpUIBox() {
        uiBox.setPrefHeight(UI_BOX_HEIGHT);
        uiBox.setMinHeight(UI_BOX_HEIGHT);
        uiBox.setMaxHeight(UI_BOX_HEIGHT);
        uiBox.setPadding(new Insets(15));
        uiBox.setAlignment(Pos.CENTER_LEFT);

    }

    void setUpCursorAndLaunch() {

        simulationPane.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
            isDragging = false;

            if (isBall) {

                // Set up visual drag line
                dragLine.setStartX(dragStartX);
                dragLine.setStartY(dragStartY);
                dragLine.setEndX(dragStartX);
                dragLine.setEndY(dragStartY);
                dragLine.setStroke(Color.RED);

                dragLine.setStrokeWidth(2);
                if (!simulationPane.getChildren().contains(dragLine))
                    simulationPane.getChildren().add(dragLine);
            }

            if (isRectangle) {
                dragPreviewRect.setStroke(Color.RED);
                dragPreviewRect.setStrokeWidth(2);
                dragPreviewRect.setFill(Color.TRANSPARENT);
                dragPreviewRect.setX(dragStartX);
                dragPreviewRect.setY(dragStartY);
                dragPreviewRect.setWidth(0);
                dragPreviewRect.setHeight(0);

                if (!simulationPane.getChildren().contains(dragPreviewRect)) {
                    simulationPane.getChildren().add(dragPreviewRect);
                }
            }

        });

        simulationPane.setOnMouseDragged(event -> {
            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;

            if (Math.hypot(dx, dy) > dragThreshold) {
                isDragging = true;
            }

            if (isBall) {
                dragLine.setEndX(event.getX());
                dragLine.setEndY(event.getY());
            }

            if (isRectangle) {
                dragPreviewRect.setX(Math.min(dragStartX, event.getX()));
                dragPreviewRect.setY(Math.min(dragStartY, event.getY()));
                dragPreviewRect.setWidth(Math.abs(dx));
                dragPreviewRect.setHeight(Math.abs(dy));
            }
        });

        simulationPane.setOnMouseReleased(event -> {
            simulationPane.getChildren().removeAll(dragLine, dragPreviewRect);

            double radius = 0, mass = 0;
            try {
                mass = Double.parseDouble(massField.getText());
                if (!isRectangle) {
                    radius = Double.parseDouble(radiusField.getText());
                }
            } catch (NumberFormatException e) {
                showAlert("Mass/Radius must be a number.");
                return;
            }

            if (isDragging) {
                if (isBall) {
                    double vx = (dragStartX - event.getX()) * DEFAULT_VELOCITY_SCALE;
                    double vy = (dragStartY - event.getY()) * DEFAULT_VELOCITY_SCALE;

                    spawnBall(dragStartX, dragStartY, radius, mass, vx, vy, true);
                }
                if (isRectangle) {
                    double dragEndX = event.getX();
                    double dragEndY = event.getY();

                    spawnRectangle(mass,dragEndX, dragEndY);
                }
            } else {
                if (isBall) { //Spawns a ball on cursor
                    clickX = event.getX();
                    clickY = event.getY();

                    // Make sure the ball doesn't spawn partially outside the window
                    double spawnX = Math.max(radius, Math.min(clickX, sceneWidth - radius));
                    double spawnY = Math.max(radius, Math.min(clickY, sceneHeight - radius));

                    spawnBall(spawnX, spawnY, radius, mass, 0, 0, false);
                }

            }
        });
    }

    void setUpButtons() {
        Button ballBtn = new Button("Ball: ON"); // assuming it starts enabled
        ballBtn.setStyle(
                "-fx-background-color: #2a2f35;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;" +
                        "-fx-translate-y: 1;"
        );
        buttons.put("ball-btn", ballBtn);

        Button rectBtn = new Button("Rect: OFF");
        rectBtn.setStyle(
                "-fx-background-color: #5285F4;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;"
        );
        buttons.put("rect-btn", rectBtn);

        uiBox.getChildren().addAll(ballBtn, rectBtn);
    }

    void setUpInputs() {
        Label radiusLabel = new Label("Radius:");
        radiusField.setDisable(false);
        radiusField.setText("10");
        radiusField.setPrefWidth(60);

        Label massLabel = new Label("Mass:");
        massField.setText("100");
        massField.setPrefWidth(60);

        bodyTypeSelector.getItems().addAll(BodyType.Static, BodyType.Dynamic);
        bodyTypeSelector.setValue(BodyType.Dynamic);

        uiBox.getChildren().addAll(radiusLabel, radiusField, massLabel, massField, bodyTypeSelector);
    }

    void setUpActions() {
        Button ballBtn = buttons.get("ball-btn");
        Button rectBtn = buttons.get("rect-btn");

        ballBtn.setOnAction(e -> {
            isBall = !isBall;
            isRectangle = !isRectangle;
            if (isBall) {
                activateBallButton();
                deactivateRectButton();
            } else {
                activateRectButton();
                deactivateBallButton();
            }
            toggleRadiusField();

        });

        rectBtn.setOnAction(e -> {
            isRectangle = !isRectangle;
            isBall = !isBall;
            if (isRectangle) {
                activateRectButton();
                deactivateBallButton();
            } else {
                activateBallButton();
                deactivateRectButton();
            }
            toggleRadiusField();
        });

        bodyTypeSelector.setOnAction(e -> {
            boolean isStatic = bodyTypeSelector.getValue().equals(BodyType.Static);
            if (isStatic) {
                massField.setText("0");
                massField.setDisable(true);
            } else {
                massField.setText("100");
                massField.setDisable(false);
            }
        });

    }

    public HBox getUI() {
        return uiBox;
    }

    public double getUIHeight() {
        return UI_BOX_HEIGHT;
    }

    Color generateMuteColor() {
        double hue = Math.random() * 360;
        double saturation = 0.3 + Math.random() * 0.2; // subtle
        double brightness = 0.6 + Math.random() * 0.3;
        return Color.hsb(hue, saturation, brightness);
    }

    void activateBallButton() {
        Button ball = buttons.get("ball-btn");
        ball.setText("Ball: ON");
        ball.setStyle(
                "-fx-background-color: #2a2f35;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;" +
                        "-fx-translate-y: 1;"
        );
    }

    void deactivateBallButton() {
        Button ball = buttons.get("ball-btn");
        ball.setText("Ball: OFF");
        ball.setStyle(
                "-fx-background-color: #4285F4;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;" +
                        "-fx-translate-y: 0;"
        );
    }

    void activateRectButton() {
        Button rect = buttons.get("rect-btn");
        rect.setText("Rect: ON");
        rect.setStyle(
                "-fx-background-color: #2a2f35;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;" +
                        "-fx-translate-y: 1;"
        );
    }

    void deactivateRectButton() {
        Button rect = buttons.get("rect-btn");
        rect.setText("Rect: OFF");
        rect.setStyle(
                "-fx-background-color: #4285F4;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #1f1f1f;" +
                        "-fx-border-width: 1;" +
                        "-fx-translate-y: 0;"
        );
    }

    void toggleRadiusField() {
        radiusField.setDisable(!isBall);
    }

    private void spawnBall(double x, double y, double radius, double mass, double vx, double vy, boolean applyVelocity) {
        Color color = generateMuteColor();

        // Clamp spawn position so ball doesn't go off-screen
        double spawnX = Math.max(radius, Math.min(x, sceneWidth - radius));
        double spawnY = Math.max(radius, Math.min(y, sceneHeight - radius));

        Ball ball = physicsWorld.spawnBall(spawnX, spawnY, radius, color, sceneHeight, sceneWidth, mass, bodyTypeSelector.getValue());

        if (applyVelocity) {
            ball.velocityX = vx;
            ball.velocityY = vy;
        }

        engineLoop.addUpdatable(ball);
        simulationPane.getChildren().add(ball.getShape());
    }

    private void spawnRectangle(double mass, double dragEndX, double dragEndY) {

        double width = Math.max(Math.abs(dragEndX - dragStartX), 10);
        double height = Math.max(Math.abs(dragEndY - dragStartY), 10);

        double centerX = Math.min(dragStartX, dragEndX) + width / 2;
        double centerY = Math.min(dragStartY, dragEndY) + height / 2;

        Color color = generateMuteColor();

        Square rect = physicsWorld.spawnSquare(centerX, centerY, height, width, color,sceneWidth, sceneHeight, mass, bodyTypeSelector.getValue());
        engineLoop.addUpdatable(rect);
        simulationPane.getChildren().add(rect.getShape());
    }

    void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Input Error");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../style.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.showAndWait();
    }

}
