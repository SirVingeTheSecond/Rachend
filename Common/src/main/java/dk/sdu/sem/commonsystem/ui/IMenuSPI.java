package dk.sdu.sem.commonsystem.ui;

import javafx.stage.Stage;

public interface IMenuSPI {
	void showMainMenu(Stage stage);
	void showPauseMenu(Stage stage);
	void showGameOverMenu(Stage stage);
	void hidePauseMenu(Stage stage);
	void hideGameOverMenu(Stage stage);
}
