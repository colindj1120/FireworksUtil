package com.hyperion.fireworks.firework_styles;

import com.hyperion.fireworks.particle.DecayFactor;
import com.hyperion.fireworks.particle.Particle;
import com.hyperion.fireworks.particle.SparkleParticle;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SparkleFirework extends Firework {
    private static final int NUM_PARTICLES = 75;

    public SparkleFirework(Point2D position, Canvas canvas, Supplier<Double> lauchAngleSupplier, double timeStep, Supplier<Double> minLaunchHeightSupplier) {
        super(position, canvas, lauchAngleSupplier, timeStep, minLaunchHeightSupplier);
    }

    @Override
    public void move() {
        super.move();
        if (hasExploded) {
            List<Particle> newParticles = new ArrayList<>();
            particles.forEach(particle -> {
                if (particle instanceof SparkleParticle sparkleParticle) {
                    if (sparkleParticle.getSecondExplosion()) {
                        newParticles.addAll(sparkleParticle.explode());
                    }
                }
            });
            particles.addAll(newParticles);
        }

    }

    @Override
    public void explode() {
        this.particles.addAll(super.createExplosionParticles(NUM_PARTICLES, .5, 1, 7, 14, DecayFactor.MEDIUM, SparkleParticle::new));
    }
}
