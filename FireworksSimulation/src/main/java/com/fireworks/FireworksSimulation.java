package com.fireworks;

import com.hyperion.fireworks.FireworksUtil;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

/**
 * A fireworks simulation featuring accurate gravity calculations,
 * explosion effects, and more realistic rocket behavior.
 *
 * @version 3.1
 * @author Colin Jokisch
 */
public class FireworksSimulation extends Application {
    private static final int    WIDTH            = 1200;
    private static final int    HEIGHT           = 1000;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Canvas        canvas        = new Canvas(WIDTH, HEIGHT);
        FireworksUtil.Builder builder = new FireworksUtil.Builder();
        builder.width(WIDTH).height(HEIGHT).frameRate(60).startPos(new Point2D(200, 750)).launchAngleSupplier(() -> Math.toRadians(45 + (Math.random() * 60)));
        FireworksUtil fireworksUtil = builder.build();
        fireworksUtil.start();
        fireworksUtil.bindCanvasSize(canvas.widthProperty(), canvas.heightProperty());
        Scene scene = new Scene(fireworksUtil, WIDTH, HEIGHT);

        stage.setTitle("Advanced Fireworks Simulation");
        stage.setScene(scene);
        stage.show();
    }
}

