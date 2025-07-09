package PhysicsWorld;

import enums.BodyType;
import enums.Constants;
import interfaces.PhysicsObject;
import interfaces.Updatable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.w3c.dom.css.Rect;
import shapes.Ball;
import shapes.Square;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld implements Updatable {
    List<PhysicsObject> objects = new ArrayList<>();

    public PhysicsWorld() {
    }

    public Ball spawnBall(double centerX, double centerY, double radius, Color color, double floorY, double floorX, double mass, BodyType bodyType) {
        Ball ball = new Ball(centerX, centerY, radius, color, floorY, floorX, mass, bodyType);
        objects.add(ball);
        return ball;
    }

    public Square spawnSquare(double centerX, double centerY, double height, double width, Color color, double floorX, double floorY, double mass, BodyType bodyType) {
        Square square = new Square(centerX, centerY, height, width, color, floorX, floorY, mass, bodyType);
        objects.add(square);
        return square;
    }

    public void collision() {
        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                PhysicsObject a = objects.get(i);
                PhysicsObject b = objects.get(j);

                if (a instanceof Ball ballA && b instanceof Ball ballB) {
                    if (areBallsColliding(ballA, ballB)) {
                        resolveBallCollision(ballA, ballB);
                    }
                }
                if ((a instanceof Ball && b instanceof Square) || (a instanceof Square && b instanceof Ball)) {
                    // Normalize order: ball always first
                    Ball ball = a instanceof Ball ? (Ball) a : (Ball) b;
                    Square square = a instanceof Square ? (Square) a : (Square) b;

                    if (areBallAndSquareColliding(ball, square)) {
                        double rectCenterX = square.getCenterX();
                        double rectCenterY = square.getCenterY();
                        double halfWidth = square.getWidth() / 2.0;
                        double halfHeight = square.getHeight() / 2.0;

                        double ballX = ball.getCenterX();
                        double ballY = ball.getCenterY();

                        // Step 1: Closest point on rectangle to ball
                        double closestX = Math.max(rectCenterX - halfWidth, Math.min(ballX, rectCenterX + halfWidth));
                        double closestY = Math.max(rectCenterY - halfHeight, Math.min(ballY, rectCenterY + halfHeight));

                        // Step 2: Vector from rect to ball
                        double dx = ballX - closestX;
                        double dy = ballY - closestY;

                        double distance = Math.sqrt(dx * dx + dy * dy);
                        double radius = ball.getRadius();

                        if (distance == 0) {
                            dx = 0;
                            dy = -1;
                            distance = 1;
                        }

                        double overlap = radius - distance;

                        // Step 3: Collision normal (unit vector)
                        double nx = dx / distance;
                        double ny = dy / distance;

                        // Step 4: Push ball out of collision
                        ball.setCenterX(ballX + nx * overlap);
                        ball.setCenterY(ballY + ny * overlap);

                        // Step 5: Compute relative velocity along normal
                        double velAlongNormal = ball.velocityX * nx + ball.velocityY * ny;

                        // Only apply impulse if moving into the surface
                        if (velAlongNormal < 0) {
                            double mass = ball.getMass();
                            double restitution = Constants.RESTITUTION;
                            double impulse = -(1 + restitution) * velAlongNormal * mass;

                            ball.velocityX += (impulse / mass) * nx;
                            ball.velocityY += (impulse / mass) * ny;
                        }
                    }


                }
            }
        }
    }


    private boolean areBallsColliding(Ball a, Ball b) {
        double dx = a.getShape().getCenterX() - b.getShape().getCenterX();
        double dy = a.getShape().getCenterY() - b.getShape().getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (a.getShape().getRadius() + b.getShape().getRadius());
    }

    private boolean areBallAndSquareColliding(Ball circle, Square rect) {
        double circleX = circle.getCenterX();
        double circleY = circle.getCenterY();

        // Adjusted rectangle bounds assuming rectX/Y is center-based
        double rectCenterX = rect.getCenterX();
        double rectCenterY = rect.getCenterY();
        double halfWidth = rect.getWidth() / 2.0;
        double halfHeight = rect.getHeight() / 2.0;

        double rectLeft = rectCenterX - halfWidth;
        double rectRight = rectCenterX + halfWidth;
        double rectTop = rectCenterY - halfHeight;
        double rectBottom = rectCenterY + halfHeight;

        // Find the closest point on the rectangle to the circle's center
        double closestX = clamp(circleX, rectLeft, rectRight);
        double closestY = clamp(circleY, rectTop, rectBottom);

        // Calculate the horizontal and vertical distances between the circle center
        // and the closest point on the rectangle
        double dx = circleX - closestX;
        double dy = circleY - closestY;

        double distanceSquared = dx * dx + dy * dy;

        //We are doing radius*radius bc we didn't take sqrt of distance
        return distanceSquared <= circle.getRadius() * circle.getRadius();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void resolveBallCollision(Ball ballA, Ball ballB) {

        double dx = ballA.getShape().getCenterX() - ballB.getShape().getCenterX();
        double dy = ballA.getShape().getCenterY() - ballB.getShape().getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) distance = 0.001;

        double overlap = ballA.getShape().getRadius() + ballB.getShape().getRadius() - distance;
        if (overlap <= 0.01) return;

        //normal vector
        double nx = dx / distance;
        double ny = dy / distance;

        // Positional correction with small buffer to prevent re-collision
        double correction = (overlap / 2) + 0.01;
        double m1 = ballA.getMass();
        double m2 = ballB.getMass();
        double totalMass = m1 + m2;

        // Distribute positional correction based on static/dynamic status and mass ratios:
        // - If one ball is static, apply full correction to the dynamic one.
        // - If both are dynamic, distribute correction proportionally to inverse mass (more correction to lighter object).
        double correctionA = ballA.isStatic ? 0 : (ballB.isStatic ? correction : correction * (m2 / totalMass));
        double correctionB = ballB.isStatic ? 0 : (ballA.isStatic ? correction : correction * (m1 / totalMass));

        ballA.getShape().setCenterX(ballA.getShape().getCenterX() + correctionA * nx);
        ballA.getShape().setCenterY(ballA.getShape().getCenterY() + correctionA * ny);

        ballB.getShape().setCenterX(ballB.getShape().getCenterX() - correctionB * nx);
        ballB.getShape().setCenterY(ballB.getShape().getCenterY() - correctionB * ny);


        double vxRel = ballA.velocityX - ballB.velocityX;
        double vyRel = ballA.velocityY - ballB.velocityY;

        double restitution = Constants.RESTITUTION; // realistic bounce factor

        double velAlongNormal = vxRel * nx + vyRel * ny;


        if (velAlongNormal > 0) return; // balls are moving apart


        double inverseMassA = ballA.isStatic ? 0 : 1 / m1;
        double inverseMassB = ballB.isStatic ? 0 : 1 / m2;

        double impulse = -(1 + restitution) * velAlongNormal / (inverseMassA + inverseMassB);
        double impulseX = impulse * nx;
        double impulseY = impulse * ny;

        ballA.velocityX += ballA.isStatic ? 0 : impulseX * inverseMassA;
        ballA.velocityY += ballA.isStatic ? 0 : impulseY * inverseMassA;
        ballB.velocityX -= ballB.isStatic ? 0 : impulseX * inverseMassB;
        ballB.velocityY -= ballB.isStatic ? 0 : impulseY * inverseMassB;

        // --------- FRICTION IMPULSE (SLIDE) ---------
        // Tangent vector
        double tx = -ny;
        double ty = nx;

        // Relative velocity along tangent
        double velAlongTangent = vxRel * tx + vyRel * ty;

        // Friction coefficient
        double friction = Constants.FRICTION;

        // Magnitude of friction impulse
        double jt = -velAlongTangent / (inverseMassA + inverseMassB);

        // Clamp jt to Coulomb's law (<= mu * normal impulse)
        double maxFriction = friction * impulse;
        jt = Math.max(-maxFriction, Math.min(jt, maxFriction));

        // Apply friction impulse
        double frictionX = jt * tx;
        double frictionY = jt * ty;

        ballA.velocityX += ballA.isStatic ? 0 : frictionX * inverseMassA;
        ballA.velocityY += ballA.isStatic ? 0 : frictionY * inverseMassA;
        ballB.velocityX -= ballB.isStatic ? 0 : frictionX * inverseMassB;
        ballB.velocityY -= ballB.isStatic ? 0 : frictionY * inverseMassB;


        if (Math.abs(ballA.velocityY) < 0.1) ballA.velocityY = 0;
        if (Math.abs(ballA.velocityX) < 0.1) ballA.velocityX = 0;
        if (Math.abs(ballB.velocityY) < 0.1) ballB.velocityY = 0;
        if (Math.abs(ballB.velocityX) < 0.1) ballB.velocityX = 0;
    }


    @Override
    public void update(double dt) {
        for (int i = 0; i < 5; i++) {
            collision(); // repeat to resolve chains of collisions
        }
    }
}
