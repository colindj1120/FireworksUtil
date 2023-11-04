package com.hyperion.fireworks.firework_styles;

import com.hyperion.fireworks.particle.DecayFactor;
import com.hyperion.fireworks.particle.Particle;
import com.hyperion.fireworks.particle.ParticleCreator;
import com.hyperion.paintrandomizer.PaintRandomizer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Firework {
    private static final double GRAVITY = 9.81; // Acceleration due to gravity (m/s^2)
    public static final double SCALE       = 10.0; // 10 pixels per meter
    private static final int    TAIL_LENGTH = 20;

    protected              List<Particle> particles = new ArrayList<>();
    protected static final Random          RANDOM    = new Random();
    protected static final PaintRandomizer PAINT_RANDOMIZER;
    static {
        PaintRandomizer.Builder builder = new PaintRandomizer.Builder();
        builder.gradientStyle(() -> PaintRandomizer.GradientStyle.values()[RANDOM.nextInt(PaintRandomizer.GradientStyle.values().length)]);
        PAINT_RANDOMIZER = builder.build();
    }


    protected Point2D position;
    protected Paint   color = PAINT_RANDOMIZER.generateRandomVibrantPaint();
    protected double  alpha = 1.0;
    protected double fade = Math.random() * 0.1;
    protected double size = RANDOM.nextDouble(5, 15);

    protected boolean hasExploded = false;

    protected final Deque<Point2D> previousPositions = new LinkedList<>();

    protected final Canvas          canvas;
    protected final GraphicsContext  graphicsContext;

    protected double initialSpeed; // Initial speed in meters per second
    protected double launchAngle; // Launch angle in radians
    protected Point2D velocity; // Velocity in pixels per frame
    protected double fallVelocity;
    protected final double timeStep;

    public Firework(Point2D position, Canvas canvas, Supplier<Double> lauchAngleSupplier, double timeStep, Supplier<Double> minLaunchHeightSupplier) {
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.position = position;
        this.launchAngle = lauchAngleSupplier.get();
        this.timeStep = timeStep;
        initializeFirework(minLaunchHeightSupplier);
    }

    private void initializeFirework(Supplier<Double> minLaunchHeightSupplier) {
        // Define the minimum and maximum heights based on the canvas size
        double maxHeightMeters = canvas.getHeight() / (SCALE * 1.5); // Max height in meters
        double minHeightMeters = minLaunchHeightSupplier.get() ; // Minimum height in meters


        // Calculate initial speed range
        double minInitialSpeedY = Math.sqrt(2 * GRAVITY * minHeightMeters);
        double maxInitialSpeedY = Math.sqrt(2 * GRAVITY * maxHeightMeters);
        double minInitialSpeed = minInitialSpeedY / Math.sin(Math.PI / 4); // Optimal angle for max height
        double maxInitialSpeed = maxInitialSpeedY / Math.sin(Math.PI / 4); // Optimal angle for max height

        // Randomly choose an initial speed within the range
        initialSpeed = RANDOM.nextDouble(minInitialSpeed, maxInitialSpeed);

        // Convert speed and angle to velocity in pixels
        double velocityX = Math.cos(launchAngle) * initialSpeed * SCALE;
        double velocityY = -Math.sin(launchAngle) * initialSpeed * SCALE; // Y is negative because screen coordinates go down
        this.velocity = new Point2D(velocityX, velocityY);
        this.fallVelocity = RANDOM.nextDouble(0, 250);
    }

    public abstract void explode();

    public void move() {
        if (!hasExploded) {
            // Update the vertical velocity to account for gravity
            // Gravity is in m/s^2, so convert it to pixels per time step, and adjust for the time step
            // Since in screen coordinates Y increases downwards, gravity is added to make the firework fall.
            double velocityY = velocity.getY() + (GRAVITY * timeStep * SCALE);

            // Update the position with the current velocity
            // Position change is velocity times time step
            double newX = position.getX() + velocity.getX() * timeStep;
            double newY = position.getY() + velocityY * timeStep; // In screen coordinates, Y increases downwards

            // Update the velocity and position of the firework
            velocity = new Point2D(velocity.getX(), velocityY);
            position = new Point2D(newX, newY);

            // Add current position to the tail
            if (previousPositions.size() >= TAIL_LENGTH) {
                previousPositions.pollFirst();
            }
            previousPositions.offerLast(position);

            boolean isOutOfBoundsBottom = Arrays.stream(new Point2D[]{new Point2D(0, 200)})
                                                .anyMatch(point2D -> !canvas.getLayoutBounds()
                                                                            .contains(position.add(point2D)));

            boolean isOutOfBoundsTopLeftRight = Arrays.stream(new Point2D[]{new Point2D(0, -100),
                                                                            new Point2D(-100, 0),
                                                                            new Point2D(100, 0)})
                                                      .anyMatch(point2D -> !canvas.getLayoutBounds()
                                                                                  .contains(position.add(point2D)));

            // Check for explosion conditions, like reaching the peak height or exceeding bounds
            // The firework should explode when it starts falling down, which is when the vertical velocity becomes positive.
            if ((velocityY > 0 && isOutOfBoundsBottom) || velocityY > fallVelocity) {
                hasExploded = true;
                explode();
            } else if (isOutOfBoundsTopLeftRight) {
                hasExploded = true;
                explode();
            }
        } else {
            // If the firework has exploded, update the particles
            particles.removeIf(Particle::isDead);
            particles.forEach(Particle::move);
        }
    }




    public void draw() {
        if (!hasExploded) {
            Iterator<Point2D> iter     = previousPositions.descendingIterator();
            double            tempSize = size;
            while (iter.hasNext()) {
                Point2D pos = iter.next();
                graphicsContext.setGlobalAlpha(alpha);  // Set the opacity
                graphicsContext.setFill(color);
                graphicsContext.fillOval(pos.getX(), pos.getY(), size, size);
                alpha -= fade; // Decrease opacity for older positions
                size -= 0.2;  // Decrease size for older positions
            }
            // Reset the alpha for the next drawing
            alpha = 1.0;

            // Reset the size for the next drawing
            size = tempSize;
//            // Draw current position
            graphicsContext.setFill(color);
            graphicsContext.fillOval(position.getX(), position.getY(), size, size);
        } else {
            particles.forEach(particle -> particle.draw(graphicsContext));
        }
    }

    public boolean isDead() {
        return hasExploded && particles.stream()
                                       .allMatch(Particle::isDead);
    }

    protected Collection<Particle> createExplosionParticles(int numParticles, double minSpeed, double maxSpeed, double minSize, double maxSize, DecayFactor decayFactor, ParticleCreator particleCreator) {
        return IntStream.range(0, numParticles)
                        .mapToObj(i -> {
                            double  angle    = RANDOM.nextDouble() * 2 * Math.PI;
                            double  speed    = RANDOM.nextDouble(minSpeed, maxSpeed); // Speed range for explosion particles
                            double size = RANDOM.nextDouble(minSize, maxSize);
                            Point2D velocity = new Point2D(Math.cos(angle) * speed, Math.sin(angle) * speed);
                            return particleCreator.create(this.position, velocity, PAINT_RANDOMIZER.generateRandomVibrantPaint(), size, decayFactor.getDecayRate());
                        })
                        .collect(Collectors.toList());
    }
}
