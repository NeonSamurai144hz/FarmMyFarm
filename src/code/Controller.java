package code;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.shape.Rectangle;

public class Controller {

    @FXML
    private Rectangle myPlayer;
    private double x;
    private double y;

    public void moveUp(ActionEvent e) {
        myPlayer.setY(y -= 10);
        System.out.println("Up");
    }

    public void moveLeft(ActionEvent e) {
        myPlayer.setX(x -= 10);  // Changed to setX and -= for left movement
        System.out.println("Left");  // Fixed print message
    }

    public void moveRight(ActionEvent e) {
        myPlayer.setX(x += 10);  // Changed to setX and += for right movement
        System.out.println("Right");  // Fixed print message
    }

    public void moveDown(ActionEvent e) {
        myPlayer.setY(y += 10);
        System.out.println("DOWN");
    }


}
