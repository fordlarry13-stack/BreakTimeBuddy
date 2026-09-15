package com.breaktimebuddy;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(
                new Controller(new ConfigHandler(new FileStorage(new File("config.json"))))
                        .getView(),
                400, 300);

        primaryStage.setTitle("Break Time Buddy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
