package com.panda.video_music_player.controllers;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class MainController {
    private final int MAX_VIDEO = 10;
    @FXML
    private Label welcomeText;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox scrollVbox;
    @FXML
    private TextField searchBox;

    @FXML
    public void initialize() {
        System.out.println("Initialized");
        new Thread(()->syncVideos("Friendzone ost")).start();
    }

    @FXML
    public void clickOnSearchButton() {
        if(!searchBox.getText().isEmpty() && !searchBox.getText().isBlank()) {
            syncVideos(searchBox.getText());
        }
    }

    private void syncVideos(String searchKeyword) {
        Platform.runLater(()->{
            scrollVbox.getChildren().clear();
            List<Map<String, String>> dataList = getVideoList(searchKeyword, MAX_VIDEO);
            System.out.println(dataList.size());
            HBox rowHbox = new HBox();
            for ( int i = 0 ; i < dataList.size() ; i++ ) {
                if(i % 2 == 0) rowHbox = new HBox();
                rowHbox.setSpacing(20);
                rowHbox.setAlignment(Pos.CENTER);
                rowHbox.setFillHeight(false);
                HBox itemHBox = new HBox();
                itemHBox.setPrefWidth(600);
                itemHBox.setMinWidth(600);
                itemHBox.setPrefHeight(270);
                itemHBox.setMinHeight(270);
                itemHBox.setStyle("-fx-background-color: black");
                itemHBox.setAlignment(Pos.CENTER);
                StackPane stackPane = new StackPane();
                stackPane.setAlignment(Pos.CENTER);
                stackPane.setPrefWidth(600);
                stackPane.setPrefHeight(270);
                stackPane.getChildren().add(getImageView(dataList.get(i).get("thumbnail")));
                HBox overlay = new HBox();
                overlay.setPrefWidth(600);
                overlay.setPrefHeight(270);
                overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
                stackPane.getChildren().add(overlay);
                VBox detailWrapper = new VBox();
                detailWrapper.setPrefWidth(600);
                detailWrapper.setPrefHeight(270);
                detailWrapper.setAlignment(Pos.CENTER);
                detailWrapper.setSpacing(20);
                Label label = new Label(dataList.get(i).get("title"));
                label.setWrapText(true);
                label.setPrefWidth(500);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-text-alignment: center;");
                Button playVlcButton = new Button("Play In VLC");
                playVlcButton.setPrefWidth(150);
                playVlcButton.setPrefHeight(45);
                int currentDataListId = i;
                playVlcButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;" +
                        "-fx-background-radius: 0px;");
                playVlcButton.setOnMouseClicked(event -> {
                    playVlcPlayer(dataList.get(currentDataListId).get("url"));
                });
                detailWrapper.getChildren().addAll(label, playVlcButton);
                stackPane.getChildren().add(detailWrapper);
                itemHBox.getChildren().add(stackPane);
                rowHbox.getChildren().add(itemHBox);
                if(i % 2 == 0) scrollVbox.getChildren().add(rowHbox);
            }
        });
    }

    private ImageView getImageView(String imagePath) {
        try {
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(600);
            imageView.setFitHeight(270);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private List<Map<String, String>> getVideoList(String searchKeyword, int numberOfVideo) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        List<Map<String, String>> dataList = null;
        Request request = new Request.Builder()
                .url("https://tza-yt-parser.onrender.com/scrape/videoList?searchKeyword="+searchKeyword+"&numberOfVideo="+numberOfVideo)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                Type listType = new TypeToken<List<Map<String, String>>>(){}.getType();
                dataList = gson.fromJson(responseBody, listType);

            } else {
                System.out.println("Request failed with status code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during the request: " + e.getMessage());
        }
        return dataList;
    }

    public static void playVlcPlayer(String videoUrl) {
        new Thread(()->{
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "yt-dlp", "-f", "bestvideo+bestaudio", "-g", videoUrl
                );

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String videoStreamUrl = reader.readLine(); // First line: Video URL
                String audioStreamUrl = reader.readLine(); // Second line: Audio URL

                process.waitFor();
                reader.close();

                System.out.println("Video URL: " + videoStreamUrl);
                System.out.println("Audio URL: " + audioStreamUrl);
                // Command to open VLC with separate video and audio URLs
                ProcessBuilder vlcPB = new ProcessBuilder(
                        "vlc", videoStreamUrl, "--input-slave=" + audioStreamUrl
                );

                vlcPB.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}