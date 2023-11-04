package com.hyperion.fireworks.particle;

import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;

@FunctionalInterface
public interface ParticleCreator {
    Particle create(Point2D startPos, Point2D velocity, Paint color, double size, double decayFactor);
}
