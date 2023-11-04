package com.hyperion.fireworks.particle;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

/**
 * A StrobeParticle represents a single blinking particle of a strobe firework.
 * It extends the Particle class with the ability to blink at random intervals.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class StrobeParticle extends Particle {
    private static final double BLINK_RATE = 0.2; // Probability of blinking
    private boolean isVisible = true; // Initial visibility

    public StrobeParticle(Point2D position, Point2D velocity, Paint color, double size, double decayRate) {
        super(position, velocity, color, size, decayRate);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Blinking effect: Randomly toggle visibility
        if (Math.random() < BLINK_RATE) {
            isVisible = !isVisible;
        }

        // Only draw the particle if it's currently visible
        if (isVisible) {
            super.draw(gc);
        }
    }
}
