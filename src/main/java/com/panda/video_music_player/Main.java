package com.panda.video_music_player;

import com.panda.video_music_player.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        MainController.extractBestVideoAudioUrls("https://www.youtube.com/watch?v=p5Jw-T4dVss");
    }

    public static void main(String[] args) {
        launch();
    }
}