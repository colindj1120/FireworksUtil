package com.hyperion.fireworks;

import com.hyperion.fireworks.firework_styles.*;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * FireworksUtil is a utility class designed to draw fireworks on a JavaFX Canvas. The class uses functional programming constructs to offer a more modular and flexible design.
 *
 * @author Colin Jokisch
 * @version 2.2
 */
@SuppressWarnings("unused")
public class FireworksUtil extends Pane {
    private final        List<Firework>  fireworks     = new ArrayList<>();
    private final        Queue<Firework> fireworkQueue = new LinkedList<>();
    private final        Random          random             = new Random();

    private final Map<Class<? extends Firework>, Double> additionalDelays = Map.of(
            DoubleExplosionFirework.class, 2.0, // extra 2 seconds for a double explosion
            LargeFirework.class, 1.5 // extra 1.5 seconds for a large firework
            // Add more if needed
    );

    private final Canvas           internalCanvas;
    private final GraphicsContext  internalGc;
    private final Timeline         creationTimeline;
    private final Timeline         updateTimeline;
    private final double secondsBetweenLaunches;
    private final double           FPS;
    private final Point2D          startPos;
    private final Supplier<Double> launchAngleSupplier;
    private final Supplier<Double> minLaunchHeightSupplier;

    /**
     * Initializes the canvas, graphics context, and other settings with a customizable batch size.
     *
     * @param builder
     *         The FireworksUtil Builder
     */
    public FireworksUtil(Builder builder) {
        internalCanvas = new Canvas(builder.width, builder.height);
        internalGc     = internalCanvas.getGraphicsContext2D();

        setupInternalCanvas(builder);

        secondsBetweenLaunches = builder.secondsBetweenLaunches;
        FPS                            = 1 / builder.frameRate;
        startPos                       = builder.startPos;
        launchAngleSupplier = builder.launchAngleSupplier;
        minLaunchHeightSupplier = builder.minLaunchHeightSupplier;

        creationTimeline = createLaunchTimeline(builder);

        updateTimeline = createDrawingTimeLine();
    }

    private Timeline createDrawingTimeLine() {
        final Timeline updateTimeline = new Timeline(new KeyFrame(Duration.seconds(FPS), e -> drawFrame()));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        return updateTimeline;
    }

    private Timeline createLaunchTimeline(Builder builder) {
        final Timeline creationTimeline = new Timeline(new KeyFrame(Duration.seconds(secondsBetweenLaunches), e -> launchFireworks(builder.maxBatchSize)));
        creationTimeline.setCycleCount(Timeline.INDEFINITE);
        return creationTimeline;
    }

    private void setupInternalCanvas(Builder builder) {
        ImageView background = getBackground(builder);

        internalCanvas.setBlendMode(BlendMode.ADD);
        internalCanvas.setEffect(new Reflection(0, 0.4, 0.15, 0));
        getChildren().addAll(background, internalCanvas);
    }

    private ImageView getBackground(Builder builder) {
        ImageView background = builder.background;

        Consumer<ImageView> setBackgroundProperties = imageView -> {
            imageView.fitHeightProperty()
                     .bind(internalCanvas.heightProperty());
            imageView.fitWidthProperty()
                     .bind(internalCanvas.widthProperty());
        };

        setBackgroundProperties.accept(background);
        return background;
    }

    private void launchFireworks(int n) {
        AtomicReference<Double> maxExtraDelay = new AtomicReference<>(0.0);
        List<Firework> batch = IntStream.range(0, n)
                                        .mapToObj(i -> Optional.ofNullable(fireworkQueue.poll()))
                                        .map(optionalFirework -> optionalFirework.orElseGet(this::createRandomFirework))
                                        .peek(firework -> {
                                            // Calculate extra delay based on firework type
                                            double extraDelay = additionalDelays.getOrDefault(firework.getClass(), 0.0);
                                            maxExtraDelay.updateAndGet(currentMax -> Math.max(currentMax, extraDelay));
                                        })
                                        .toList();

        fireworks.addAll(batch);

        // Update the launch timeline to include the extra delay
        double normalDelay = secondsBetweenLaunches;
        double newDelay = normalDelay + maxExtraDelay.get();
        creationTimeline.stop();
        creationTimeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(newDelay), e -> launchFireworks(n)));
        creationTimeline.playFromStart();

        // Schedule the timeline to reset to normal delay after this launch
        PauseTransition resetDelay = new PauseTransition(Duration.seconds(newDelay));
        resetDelay.setOnFinished(event -> resetCreationTimeline(normalDelay, n));
        resetDelay.play();
    }

    private void resetCreationTimeline(double normalDelay, int maxBatchSize) {
        double delay = Math.random() > .5 ? normalDelay : normalDelay/2;
        creationTimeline.stop();
        creationTimeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(delay), e -> launchFireworks(maxBatchSize)));
        creationTimeline.playFromStart();
    }

    private Firework createRandomFirework() {
        FireworkType     fireworkType        = FireworkType.values()[random.nextInt(FireworkType.values().length)];
//        FireworkType     fireworkType        = FireworkType.MEDIUM;
        return switch (fireworkType) {
            case STROBE -> new StrobeFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            case SPARKLE -> new SparkleFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            case SMALL -> new SmallFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            case MEDIUM -> new MediumFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            case LARGE -> new LargeFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            case DOUBLE -> new DoubleExplosionFirework(startPos, internalCanvas, launchAngleSupplier, FPS, minLaunchHeightSupplier);
            // Add more cases if you have more firework types
        };
    }

    private void drawFrame() {
        internalGc.clearRect(0, 0, internalCanvas.getWidth(), internalCanvas.getHeight());

        fireworks.forEach(firework -> {
            firework.move();
            firework.draw();
        });

        // Remove fireworks that have moved off the screen or reached their target distance
        fireworks.removeIf(Firework::isDead);
    }

    public void start() {
        creationTimeline.play();
        updateTimeline.play();
    }

    public void stop() {
        creationTimeline.stop();
        updateTimeline.stop();
    }

    public void bindCanvasSize(ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty heightProperty) {
        internalCanvas.widthProperty()
                      .bind(widthProperty);
        internalCanvas.heightProperty()
                      .bind(heightProperty);
    }

    // Builder inner class
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder {
        private int              maxBatchSize                   = 3; // default value
        private double           width                          = 800; // default width
        private double           height                         = 600; // default height
        private double           frameRate                      = 60;
        private double secondsBetweenLaunches = 3.0;
        private Supplier<Double> launchAngleSupplier     = () -> Math.toRadians(60 + (Math.random() * 60));
        private Supplier<Double> minLaunchHeightSupplier = null;
        private Point2D          startPos                = null;
        private ImageView        background                     = new ImageView(Objects.requireNonNull(getClass().getResource("/Images/City Night Skyline.jpg"))
                                                                                       .toExternalForm());

        // methods to set the builder fields
        public Builder maxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
            return this;
        }

        public Builder width(double width) {
            this.width = width;
            return this;
        }

        public Builder height(double height) {
            this.height = height;
            return this;
        }

        public Builder secondsBetweenLaunches(double secondsBetweenLaunches) {
            this.secondsBetweenLaunches = secondsBetweenLaunches;
            return this;
        }

        public Builder frameRate(double frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Builder startPos(Point2D startPos) {
            this.startPos = startPos;
            return this;
        }

        public Builder launchAngleSupplier(Supplier<Double> launchAngleSupplier) {
            this.launchAngleSupplier = launchAngleSupplier;
            return this;
        }

        public Builder minLaunchHeightSupplier(Supplier<Double> minLaunchHeightSupplier) {
            this.minLaunchHeightSupplier = minLaunchHeightSupplier;
            return this;
        }

        public Builder background(ImageView background) {
            this.background = background;
            return this;
        }

        // build method
        public FireworksUtil build() {
            startPos                = Optional.ofNullable(startPos).orElse(new Point2D(width / 2, height));
            minLaunchHeightSupplier = Optional.ofNullable(minLaunchHeightSupplier).orElse(() -> (height / (Firework.SCALE * 1.5)) / 2.0);
            return new FireworksUtil(this);
        }
    }
}
