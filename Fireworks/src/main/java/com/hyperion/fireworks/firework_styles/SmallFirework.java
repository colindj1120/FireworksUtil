package com.hyperion.fireworks.firework_styles;

import com.hyperion.fireworks.particle.DecayFactor;
import com.hyperion.fireworks.particle.Particle;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

import java.util.function.Supplier;

/**
 * Represents a small-sized firework explosion.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class SmallFirework extends Firework {

    private static final int NUM_PARTICLES = 50; // Number of particles for a small explosion

    /**
     * Constructs a SmallFirework instance.
     *
     * @param position
     *         The initial position of the firework.
     * @param canvas
     *         The canvas where the firework is displayed.
     * @param lauchAngleSupplier
     *         A supplier for the launch angle.
     * @param timeStep
     *         The time step for the simulation.
     * @param minLaunchHeightSupplier
     *         The Minimum Launch Height for the firework
     */
    public SmallFirework(Point2D position, Canvas canvas, Supplier<Double> lauchAngleSupplier, double timeStep, Supplier<Double> minLaunchHeightSupplier) {
        super(position, canvas, lauchAngleSupplier, timeStep, minLaunchHeightSupplier);
    }

    /**
     * Explodes this firework, creating a number of particles with properties suitable for a small explosion.
     */
    @Override
    public void explode() {
        this.particles.addAll(super.createExplosionParticles(NUM_PARTICLES, 1, 3, 1, 3, DecayFactor.MEDIUM, Particle::new));
    }
}

