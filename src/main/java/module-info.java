module com.panda.video_music_player {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.google.gson;


    opens com.panda.video_music_player to javafx.fxml;
    exports com.panda.video_music_player;
    exports com.panda.video_music_player.controllers;
    opens com.panda.video_music_player.controllers to javafx.fxml;
}