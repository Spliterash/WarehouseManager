package ru.spliterash.warehouse;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.view.windows.root.Root;

public class Main extends Application {
    @Getter
    private static Main main;
    @Getter
    private Stage mainStage;
    @Getter
    private Scene mainScene;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        main = this;
        mainStage = primaryStage;
        BaseConstructor.getBase();
        openApp();
    }


    /**
     * Открывает приложуху
     */
    private void openApp() {
        mainScene = new Scene(new Root());
        mainStage.setScene(mainScene);
        mainStage.show();
    }


}
