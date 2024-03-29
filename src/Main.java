import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root;
        root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle("Upload Pic to QR");
        primaryStage.setScene(new Scene(root, 750, 360));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
