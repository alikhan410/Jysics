package shapes;

import enums.BodyType;
import enums.Constants;
import interfaces.PhysicsObject;
import interfaces.Updatable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball implements Updatable, PhysicsObject {
    private final Circle shape;
    public double velocityY = 0;
    public double velocityX = 0;

    private final double GRAVITY = Constants.GRAVITY;       // Acceleration due to gravity (px/s^2)
    private final double RESTITUTION = Constants.RESTITUTION;   // Coefficient of restitution (0 = no bounce, 1 = perfect bounce)

    private final double floorY;              // Y position of the floor
    private final double floorX;              // X boundary (right wall)
    private final double mass;                // Mass of the ball
    private final double radius;
    private final BodyType bodyType;
    public final boolean isStatic;

    public Ball(double centerX, double centerY, double radius, Color color, double floorY, double floorX, double mass, BodyType bodyType) {
        this.floorY = floorY;
        this.floorX = floorX;
        this.mass = mass;
        this.radius = radius;
        this.bodyType = bodyType;
        this.isStatic = (bodyType == BodyType.Static);
        shape = new Circle(radius, color);
        shape.setCenterX(centerX);
        shape.setCenterY(centerY);
    }

    public Circle getShape() {
        return shape;
    }

    public void update(double dt) {
        switch (bodyType) {
            case BodyType.Static:
                return;
            case BodyType.Dynamic: {
                // Settling velocity threshold when ball is on the floor and velocity is low
                if (onFloor() && Math.abs(velocityY) < 5) {
                    velocityY = 0;
                } else {
                    velocityY += GRAVITY * dt;
                }
                shape.setCenterY(shape.getCenterY() + velocityY * dt);
                shape.setCenterX(shape.getCenterX() + velocityX * dt);
                bounceY();
                bounceX();

                if (onFloor() && Math.abs(velocityY) < 5) {
                    // Ball basically resting vertically on floor

                    // Apply friction to slow down horizontal sliding velocity
                    double frictionAccel = Constants.FRICTION * GRAVITY; // e.g. friction coefficient times gravity
                    if (velocityX > 0) {
                        velocityX = Math.max(0, velocityX - frictionAccel * dt);
                    } else if (velocityX < 0) {
                        velocityX = Math.min(0, velocityX + frictionAccel * dt);
                    }
                }
                // Clamp velocities close to zero to exactly zero
                if (Math.abs(velocityY) < 0.01) velocityY = 0;
                if (Math.abs(velocityX) < 0.01) velocityX = 0;
            }
        }

    }


    public void bounceY() {

        if (onFloor() && Math.abs(velocityY) < 5) {
            // Ball is basically resting — no bounce needed.
            velocityY = 0;
            return;
        }
        double bottom = shape.getCenterY() + shape.getRadius();


        if (bottom > floorY && velocityY > 0) {  // only bounce if moving down into floor
            // Moving ball to just above the floor
            shape.setCenterY(floorY - shape.getRadius());

            double relativeVelocity = velocityY;
            double floorMass = 1e10;
            double impulse = -(1 + RESTITUTION) * relativeVelocity / (1 / mass + 1 / floorMass);

            velocityY += impulse / mass;

            // Simulating friction by reducing X slightly
            velocityX *= 0.95;
        }

    }

    public void bounceX() {
        double right = shape.getCenterX() + shape.getRadius();
        double left = shape.getCenterX() - shape.getRadius();

        // Right wall
        if (right > floorX) {
            shape.setCenterX(floorX - shape.getRadius());

            double relativeVelocity = velocityX;
            double impulse = -(1 + RESTITUTION) * relativeVelocity * mass;
            velocityX += impulse / mass;
        }

        // Left wall
        if (left < 0) {
            shape.setCenterX(shape.getRadius());

            double relativeVelocity = velocityX;
            double impulse = -(1 + RESTITUTION) * relativeVelocity * mass;
            velocityX += impulse / mass;
        }
    }

    public double getMass() {
        return mass;
    }

    public double getCenterX() {
        return shape.getCenterX();
    }

    public double getCenterY() {
        return shape.getCenterY();
    }

    public void setCenterX(double X) {
        shape.setCenterX(X);
    }

    public void setCenterY(double Y) {
        shape.setCenterY(Y);
    }

    public double getRadius() {
        return radius;
    }

    private boolean onFloor() {
        double bottom = shape.getCenterY() + shape.getRadius();
        return bottom >= floorY - 0.5; // 0.5 is a small threshold to tolerate minor overshoot
    }

}
