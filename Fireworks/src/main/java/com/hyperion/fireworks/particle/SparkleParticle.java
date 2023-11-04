package com.hyperion.fireworks.particle;

import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparkleParticle extends Particle{
    private boolean secondExplosion = false;

    public SparkleParticle(Point2D position, Point2D velocity, Paint color, double size, double decayRate) {
        super(position, velocity, color, size, decayRate);
    }

    @Override
    public boolean isDead() {
        return opacity <= 0.0 || secondExplosion;
    }

    @Override
    public void move() {
        super.move();
        // Add extra sparkle-specific behavior, such as secondary mini-explosions
        if (opacity < .5) {
            // Create secondary mini-explosions
            secondExplosion = true;
        }
    }

    public List<Particle> explode() {
        return Stream.generate(() -> {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 0.5 + Math.random() * 2;
            Point2D newVelocity = new Point2D(Math.cos(angle) * speed, -Math.sin(angle) * speed);
            return new Particle(position, newVelocity, color, size, DecayFactor.SLOW.getDecayRate());
        }).limit(10).collect(Collectors.toList());
    }

    public boolean getSecondExplosion() {
        return secondExplosion;
    }
}
