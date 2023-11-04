package com.hyperion.fireworks.particle;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class Particle {
    protected       double  opacity   = 1.0;
    protected       double  decayRate;
    protected       Point2D position;
    protected       Point2D velocity;
    protected       Paint   color;
    protected       double  size;
    protected final double  gravity;

    public Particle(Point2D position, Point2D velocity, Paint color, double size, double decayRate) {
        this.position = position;
        this.color    = color;
        this.size     = size;
        this.velocity = velocity;
        this.gravity  = 0.06;
        this.decayRate = decayRate;
    }

    public boolean isDead() {
        return opacity <= 0.0;
    }

    public void move() {
        velocity = velocity.add(0, gravity);
        position = position.add(velocity);
        opacity -= decayRate;
    }

    public void draw(GraphicsContext gc) {
        gc.setGlobalAlpha(Math.random() * opacity);  // Use the opacity
        gc.setFill(color);
        gc.fillOval(position.getX(), position.getY(), 5, 5);
    }
}
