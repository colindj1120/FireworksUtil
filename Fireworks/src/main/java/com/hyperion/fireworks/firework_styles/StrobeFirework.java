package com.hyperion.fireworks.firework_styles;

import com.hyperion.fireworks.particle.DecayFactor;
import com.hyperion.fireworks.particle.StrobeParticle;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

import java.util.function.Supplier;

/**
 * A StrobeFirework represents a firework that creates a strobe effect in the sky.
 * It extends the Firework class with the ability to generate StrobeParticles.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class StrobeFirework extends Firework {
    private static final int PARTICLE_COUNT = 200; // Number of particles in the strobe

    public StrobeFirework(Point2D position, Canvas canvas, Supplier<Double> lauchAngleSupplier, double timeStep, Supplier<Double> minLaunchHeightSupplier) {
        super(position, canvas, lauchAngleSupplier, timeStep, minLaunchHeightSupplier);
    }

    @Override
    public void explode() {
        this.particles.addAll(super.createExplosionParticles(PARTICLE_COUNT, 0.1, 1.5, 2, 5, DecayFactor.SUPER_SLOW, StrobeParticle::new));
    }
}
