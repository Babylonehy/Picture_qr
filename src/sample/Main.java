package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;




public class Main extends Application {
    Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception{

        root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle("Upload Pic to QR");

        primaryStage.setScene(new Scene(root, 540, 360));
        primaryStage.show();
    }

    public static void  setImg(ImageView view){


    }

    public static void main(String[] args) {
        launch(args);
    }
}
