package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Created by Aditya on 01-07-2015.
 */
public class LoginController implements Initializable {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private Button sumbit;
    @FXML
    private Button hregister;
    @FXML
    private AnchorPane content;
    @FXML
    private Label invalid;


    @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void jumpRS(ActionEvent event) throws IOException{
        Parent register_page_parent =FXMLLoader.load(getClass().getResource("register.fxml"));
        Scene register_page_scene=new Scene(register_page_parent);
        Stage app_stage= (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.setScene(register_page_scene);
        app_stage.show();
    }

    @FXML
    void authorize(ActionEvent event) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
        String cemail = email.getText();
        String query = "SELECT * FROM project.uinfo WHERE email='" + cemail + "'";
        statement = connect.createStatement();
        resultSet = statement.executeQuery(query);

        if (resultSet.isBeforeFirst()) {
            while (resultSet.next()) {
                if (email.getText().equalsIgnoreCase(resultSet.getString(1))) {
                    if (password.getText().equals(resultSet.getString(4))) {
                        String ce=email.getText();
                        PassingV sv=new PassingV(ce);
                        ((Node)event.getSource()).getScene().getWindow().hide();
                        FXMLLoader loader=new FXMLLoader();
                        loader.setLocation(getClass().getResource("GUI.fxml"));
                        loader.load();
                        Parent main_page_parent = loader.getRoot();
                        Scene main_page_scene = new Scene(main_page_parent);
                        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        app_stage.setScene(main_page_scene);
                        Controller controller= loader.getController();
                        controller.setGv(sv);
                        app_stage.show();

                    } else {
                        invalid.setText("password not correct");
                    }
                }
                }
            }else {
            invalid.setText("email not valid");
        }
    }
}
