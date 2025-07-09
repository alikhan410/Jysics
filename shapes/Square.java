package shapes;

import enums.BodyType;
import enums.Constants;
import interfaces.PhysicsObject;
import interfaces.Updatable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class Square implements Updatable, PhysicsObject {
    private final Rectangle shape;
    private double velocityY = 0;
    private double velocityX = 0;
    private final double gravity = 500;
    private final double damping = 0.7;
    private final double floorY;
    private final double floorX;
    private final double mass;
    private final BodyType bodyType;
    private final double GRAVITY = Constants.GRAVITY;       // Acceleration due to gravity (px/s^2)
    private final double RESTITUTION = 0.1;   // Coefficient of restitution (0 = no bounce, 1 = perfect bounce)


    public Square(double centerX, double centerY, double height, double width, Color color, double floorX, double floorY, double mass, BodyType bodyType) {
        this.floorY = floorY;
        this.floorX = floorX;
        this.mass = mass;
        this.bodyType = bodyType;
        shape = new Rectangle(width, height);
        shape.setFill(color);
        shape.setX(centerX - width / 2);
        shape.setY(centerY - height / 2);

    }

    public Rectangle getShape() {
        return shape;
    }

    public double getWidth() {
        return shape.getWidth();
    }

    public double getHeight() {
        return shape.getHeight();
    }

    public double getX() {
        return shape.getX();
    }

    public double getY() {
        return shape.getY();
    }

    public double getCenterX() {
        return shape.getX() + shape.getWidth() / 2;
    }

    public double getCenterY() {
        return shape.getY() + shape.getHeight() / 2;
    }

    public void update(double dt) {
        if (bodyType == BodyType.Static) return;

        // Apply gravity
        velocityY += GRAVITY * dt;

        // Integrate position
        shape.setY(shape.getY() + velocityY * dt);
        shape.setX(shape.getX() + velocityX * dt);

        // Handle floor and wall collisions
        bounceY();
        bounceX();

        // Apply friction if on floor
        if (onFloor()) {
            double frictionAccel = Constants.FRICTION * GRAVITY;

            if (velocityX > 0) {
                velocityX = Math.max(0, velocityX - frictionAccel * dt);
            } else if (velocityX < 0) {
                velocityX = Math.min(0, velocityX + frictionAccel * dt);
            }

            // X-settling threshold
            if (Math.abs(velocityX) < 1e-2) {
                velocityX = 0;
            }
        }

        // Y-settling threshold to prevent endless micro-bounces
        if (Math.abs(velocityY) < 5 && onFloor()) {
            velocityY = 0;
        }
    }

    private void bounceY() {
        if (shape.getY() + shape.getHeight() >= floorY) {
            shape.setY(floorY - shape.getHeight());

            double relativeVelocity = velocityY;
            double impulse = -(1 + RESTITUTION) * relativeVelocity / (1 / mass + 0);
            velocityY += impulse / mass;
        }
    }

    private void bounceX() {
        if (shape.getX() < 0) {
            shape.setX(0);
            velocityX = -velocityX * RESTITUTION;
        }
        if (shape.getX() + shape.getWidth() > floorX) {
            shape.setX(floorX - shape.getWidth());
            velocityX = -velocityX * RESTITUTION;
        }
    }

    private boolean onFloor() {
        return shape.getY() + shape.getHeight() >= floorY - 0.5;
    }
}