package com.hyperion.fireworks.firework_styles;

import com.hyperion.fireworks.particle.DecayFactor;
import com.hyperion.fireworks.particle.Particle;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Represents a firework that explodes twice.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class DoubleExplosionFirework extends Firework {
    private static final int FIRST_EXPLOSION_PARTICLES = 300; // Number of particles for the first explosion
    private static final int SECOND_EXPLOSION_PARTICLES = 150; // Number of particles for the second explosion
    private static final int SECOND_EXPLOSION_DELAY_FRAMES = 15; // Number of frames to wait before the second explosion

    private       boolean         secondaryExplosionOccurred = false;
    private final PauseTransition pauseForSecondExplosion;

    /**
     * Constructs a DoubleExplosionFirework instance.
     *
     * @param position
     *         The initial position of the firework.
     * @param canvas
     *         The canvas where the firework is displayed.
     * @param launchAngleSupplier
     *         A supplier for the launch angle.
     * @param timeStep
     *         The time step for the simulation.
     * @param minLaunchHeightSupplier
     *         The Minimum Launch Height for the firework
     */
    public DoubleExplosionFirework(Point2D position, Canvas canvas, Supplier<Double> launchAngleSupplier, double timeStep, Supplier<Double> minLaunchHeightSupplier) {
        super(position, canvas, launchAngleSupplier, timeStep, minLaunchHeightSupplier);
        // Calculate the delay for the second explosion in terms of the timeStep
        double delayInSeconds = timeStep * SECOND_EXPLOSION_DELAY_FRAMES;
        pauseForSecondExplosion = new PauseTransition(Duration.seconds(delayInSeconds));
        pauseForSecondExplosion.setOnFinished(event -> triggerSecondExplosion());
    }

    /**
     * Explodes this firework, creating a number of particles with properties suitable for the first explosion.
     * Initiates the PauseTransition for the second explosion.
     */
    @Override
    public void explode() {
        if (!secondaryExplosionOccurred) {
            this.particles.addAll(createExplosionParticles(FIRST_EXPLOSION_PARTICLES));
            pauseForSecondExplosion.play();
            secondaryExplosionOccurred = true;
        }
    }

    /**
     * Triggers the second explosion of this firework.
     */
    private void triggerSecondExplosion() {
        this.particles.addAll(createExplosionParticles(SECOND_EXPLOSION_PARTICLES));
        hasExploded = true; // Mark as fully exploded after the second explosion
    }

    /**
     * Creates a collection of particles to simulate an explosion.
     *
     * @param numParticles The number of particles to create.
     * @return A collection of particles.
     */
    protected Collection<Particle> createExplosionParticles(int numParticles) {
        return super.createExplosionParticles(numParticles, 5, 7, 10, 15, DecayFactor.SLOW, Particle::new);
    }


}

