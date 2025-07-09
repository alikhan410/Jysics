import PhysicsWorld.PhysicsWorld;
import engineLoop.EngineLoop;
import enums.Constants;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import physicsUI.PhysicsUI;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        double sceneWidth = Constants.SCENE_WIDTH;
        double sceneHeight = Constants.SCENE_HEIGHT;
        Pane simulationPane = new Pane();

        Color subtleBlack = Color.rgb( 242, 233, 220, 1);

        PhysicsWorld physicsWorld = new PhysicsWorld();
        EngineLoop engineLoop = new EngineLoop();
        engineLoop.addUpdatable(physicsWorld);
        PhysicsUI physicsUI = new PhysicsUI( sceneHeight, sceneWidth, physicsWorld, engineLoop, simulationPane);

        BorderPane root = new BorderPane();
        root.setCenter(simulationPane);
        root.setTop(physicsUI.getUI());

        root.setBackground(
                new Background(new BackgroundFill(subtleBlack, CornerRadii.EMPTY, Insets.EMPTY))
        );



        Scene scene = new Scene(root, sceneWidth, sceneHeight+ physicsUI.getUIHeight());
        stage.setTitle("Jysics - Physics Engine written in Java");
        stage.setScene(scene);
        stage.show();

        engineLoop.start();


    }

    public static void main(String[] args) {
        launch();
    }
}