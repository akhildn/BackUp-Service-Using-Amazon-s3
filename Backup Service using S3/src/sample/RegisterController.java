package sample;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by Aditya on 01-07-2015.
 */
public class RegisterController  implements Initializable{


    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    String bname;
    String accessKey,secretKey;

    @FXML
    private TextField email;
    @FXML
    private TextField fname;
    @FXML
    private TextField lname;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField cpassword;
    @FXML
    private Button Lreturn;
    @FXML
    private Label notify;
    @FXML
    private Label notifyp;
    @FXML
    private Label notifys;


    @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void jumpLS(ActionEvent event) throws IOException{
        Parent login_page_parent = FXMLLoader.load(getClass().getResource("first.fxml"));
        Scene login_page_scene=new Scene(login_page_parent);
        Stage app_stage= (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.setScene(login_page_scene);
        app_stage.show();
    }

    @FXML
    void addDetailsDB(ActionEvent event) throws IOException, ClassNotFoundException, SQLException {
        clearLables();
        if (!email.getText().isEmpty() && !fname.getText().isEmpty() && !lname.getText().isEmpty()
            &&!password.getText().isEmpty() && !cpassword.getText().isEmpty()){

            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
            String gemail = email.getText();
            String gfname = fname.getText();
            String glname = lname.getText();
            String gpassword;

            EmailValidator valid = new EmailValidator();
            boolean cvemail = valid.validate(gemail);
            String query = "SELECT * FROM project.uinfo WHERE email='" + gemail + "'";
            statement = connect.createStatement();
            resultSet = statement.executeQuery(query);
            if (cvemail) {
                if (!resultSet.isBeforeFirst()) {
                    if (password.getText().equals(cpassword.getText())) {

                        gpassword = password.getText();

                        createBucket();
                        putBucket();
                        statement.executeUpdate("INSERT INTO project.uinfo (email,fname,lname,password,bname) " +
                                "VALUES ('" + gemail + "','" + gfname + "','" + glname + "','" + gpassword + "','" + bname + "')");


                        String cquery = "SELECT * FROM project.uinfo WHERE email='" + gemail + "'";
                        statement = connect.createStatement();
                        resultSet = statement.executeQuery(cquery);
                        while (resultSet.next()) {
                            if (gemail.equals(resultSet.getString(1))) {
                                clearForm();
                                notifys.setText("registration successfull click on login");
                            }
                        }
                    } else {
                        notifyp.setText("password did not match");
                    }
                } else {
                    notify.setText("email already exists");
                }
            } else {
                notify.setText("not a valid email address");
            }
        }else {
            clearForm();
            notifys.setText("empty fields found");
        }
    }

    private void clearLables() {
        notifys.setText("");
        notify.setText("");
        notifyp.setText("");
    }

    @FXML
    void clearForm(){

            email.clear();
            fname.clear();
            lname.clear();
            password.clear();
            cpassword.clear();
            notifys.setText("");
            notify.setText("");
            notifyp.setText("");
    }

    void createBucket(){
        UUID uuid=UUID.randomUUID();
        Long id = UUID.randomUUID().getMostSignificantBits();
        bname="akhildn"+id;
        System.out.println(bname);
    }
    void putBucket(){

        AmazonS3 conn = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        conn.setEndpoint("s3.amazonaws.com");
        conn.createBucket(new CreateBucketRequest(bname));
    }

}


