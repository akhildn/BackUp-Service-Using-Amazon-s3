package sample;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller extends TimerTask implements Initializable {

    String DefaultFolder=new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
    File file,wfile;
    private static final String TIME12HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    Timer time = new Timer();
    int timerflag=0;
    int pbl;
    int flag=1;
    String filePath="";
    String filename="";
    String lastmodified="";
    public String bname;

    private PassingV gv;
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    public String email;


    @FXML private TableView<Table> table;
    @FXML private TableColumn<Table, String> fname;
    @FXML private TableColumn<Table, String> fpath;
    @FXML private TableColumn<Table, String> ftime;

    final ObservableList<Table> data = FXCollections.observableArrayList();

    @FXML
    private Button browse;
    @FXML
    private Button add;
    @FXML
    private Button cancel;
    @FXML
    private TextField spath;
    @FXML
    private Label setlable;
    @FXML
    public Label setUL;
    @FXML
    private Button delete;
    @FXML
    private TextField gfpath;
    @FXML
    private TextField tsync;
    String newVal;
    @FXML
    private Button setAS;

    AmazonS3 conn;

    @FXML
    public void initialize(URL location,ResourceBundle resources) {

        assert browse != null : "fx:id=\"browse\" was not injected: check your FXML file 'GUI.fxml'.";
        assert spath != null : "fx:id=\"spath\" was not injected: check your FXML file 'GUI.fxml'.";
        assert add != null : "fx:id=\"add\" was not injected: check your FXML file 'GUI.fxml'.";
        assert cancel != null : "fx:id=\"cancel\" was not injected: check your FXML file 'GUI.fxml'.";
        configureButtons();

        fname.setCellValueFactory(new PropertyValueFactory<Table, String>("rfname"));
        fpath.setCellValueFactory(new PropertyValueFactory<Table, String>("rfpath"));
        ftime.setCellValueFactory(new PropertyValueFactory<Table, String>("rftime"));
        table.setItems(data);


    }



    void configureVar() throws SQLException, ClassNotFoundException {
        Connection();
    }



    @FXML
    void Browse(ActionEvent event){
        if(browse!=null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*"),
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                    new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"));

            Stage s=new Stage();
            file = fileChooser.showOpenDialog(s);
            filePath = file.getAbsolutePath();
            filename =file.getName();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            lastmodified=sdf.format(file.lastModified());
            putText();
            }
    }

    void Connection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
        String query = "SELECT * FROM project.fileinfo WHERE email='"+email+"'";
        statement = connect.createStatement();
        resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            filename=resultSet.getString(2);
            filePath=resultSet.getString(3);
            lastmodified=resultSet.getString(4);
            Table entry = new Table(filename, filePath, lastmodified);
            data.add(entry);
            clearVariables();
        }
    }


    @FXML
    void addDetails(ActionEvent event) throws IOException, ClassNotFoundException, SQLException {
        dLable.setText("");
        setlable.setText("");
        Class.forName("com.mysql.jdbc.Driver");
        connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
        statement= connect.createStatement();
        if (filename != "" && filePath != "" && lastmodified != "") {

            String query="SELECT * from project.fileinfo WHERE email='"+email+"'";
            resultSet=statement.executeQuery(query);
            while(resultSet.next()){
                if(filePath.equals(resultSet.getString(3))){
                    flag=0;
                }
            }
            if (flag==1){
                setlable.setText(" ");
                String iquery="INSERT INTO project.fileinfo (email,filename,filepath,lastmodified) " +
                        "VALUES (?,?,?,?)";
                preparedStatement = connect.prepareStatement(iquery);
                preparedStatement.setString(1,email);
                preparedStatement.setString(2,filename);
                preparedStatement.setString(3,filePath);
                preparedStatement.setString(4,"not uploaded");
                preparedStatement.executeUpdate();
            Table entry = new Table(filename, filePath, lastmodified);
            data.add(entry);
            clearForm();
            clearVariables();
        }else{
            setlable.setText("Filepath already exits");
            flag=1;
            }
        }
    }

    @FXML
    void Upload() throws ClassNotFoundException, SQLException, InterruptedException {

        conn = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        conn.setEndpoint("s3.amazonaws.com");
        String fname;
        String fpath;
        String lmodified;
        Class.forName("com.mysql.jdbc.Driver");
        connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
        statement = connect.createStatement();
        String bquery = "SELECT bname FROM project.uinfo WHERE email='" + email + "'";
        resultSet = statement.executeQuery(bquery);
        while (resultSet.next()){
            bname=resultSet.getString(1);
        }

        Statement cs=connect.createStatement();
        ResultSet crs=cs.executeQuery("SELECT COUNT(*) AS total from project.fileinfo WHERE email='" + email + "'");
        while (crs.next()) {
            pbl = crs.getInt("total");
        }
        int i=1;
        String query = "SELECT * from project.fileinfo WHERE email='" + email + "'";
        resultSet = statement.executeQuery(query);
        if (resultSet.isBeforeFirst()) {
            while (resultSet.next()) {
                fname = resultSet.getString(2);
                fpath = resultSet.getString(3);
                lmodified = resultSet.getString(4);
                File gfile = new File(fpath);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lastmodified = sdf.format(gfile.lastModified());
                if (lmodified.equals(lastmodified)) {
                    setUL.setText(fname + " : is up to date");
                } else {

                    conn.putObject(new PutObjectRequest(bname, fname, gfile).withCannedAcl(CannedAccessControlList.PublicRead));

                    String iquery="update project.fileinfo set lastmodified = ? where email = ? AND filepath = ?";

                    preparedStatement = connect.prepareStatement(iquery);
                    preparedStatement.setString(1, lastmodified);
                    preparedStatement.setString(2, email);
                    preparedStatement.setString(3, fpath);
                    preparedStatement.executeUpdate();
                    i++;
                }
            }
            if( i > 1) {
                setUL.setText("upload Complete");
            }else{
                setUL.setText("all file are up to date");
            }
        }else {
            setUL.setText("No file to upload please check manage file paths");
        }
    }

    void clearForm(){
        spath.clear();
    }

    void  clearVariables(){
        filename="";
        filePath="";
        lastmodified="";
    }

    @FXML
    void CancelS(){
        spath.clear();
        filename="";
        filePath="";
        lastmodified="";
    }

    @FXML
    void putText(){
        spath.setText(filePath);
    }

    private void configureButtons() {
        if (browse != null) {
            browse.setDisable(false);
        }
        if (add != null) {
            add.setDisable(false);
        }


    }

    public void setGv(PassingV gv) throws SQLException, ClassNotFoundException {
        email =gv.getEmaill();
        this.gv = gv;
        configureVar();
    }

    @FXML
    void Logout(){
        System.exit(0);
    }

    @FXML
    private Label dLable;

    @FXML
    void DeleteP(ActionEvent event) throws ClassNotFoundException, SQLException {
       ObservableList<Table> sitem,allitems;
        allitems=table.getItems();
        sitem = table.getSelectionModel().getSelectedItems();
        Table si=table.getSelectionModel().getSelectedItem();
        String path=si.getRfpath();
        gfpath.setText(path);
        System.out.println(path);
        Class.forName("com.mysql.jdbc.Driver");
        connect = DriverManager.getConnection("jdbc:mysql://localhost/project?" + "user=root&password=akhildn");
        preparedStatement = connect.prepareStatement("DELETE FROM project.fileinfo WHERE email=? AND filepath=? ");
        preparedStatement.setString(1,email);
        preparedStatement.setString(2,path);
        preparedStatement.executeUpdate();
        sitem.forEach(allitems::remove);
        dLable.setText(path+" : is deleted");
    }

    @FXML
    void setSelected(){
        Table si=table.getSelectionModel().getSelectedItem();
        String path=si.getRfpath();
        gfpath.setText(path);
    }

    @FXML
    private Label asError;

    @FXML
    void autoSync() {
        System.out.println("function called");
        newVal = tsync.getText();
        if (isValidJavaIdentifier(newVal)) {
            asError.setText("");
            setAS.setDisable(false);
        }else {
            asError.setText("Time Format Invalid (valid format:   [0-1][0-9]:[0-5][0-9]  ");
        }
    }
    public static boolean isValidJavaIdentifier(String s) {
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(TIME12HOURS_PATTERN);
        matcher = pattern.matcher(s);
        System.out.println(matcher.matches());
        return matcher.matches();

    }

    @FXML
    void callAS(){
        if(timerflag==0){
            timerflag=1;
            setAS.setDisable(true);
            String[] parts = newVal.split(":");
            String hrs = parts[0];
            String mins = parts[1];
            int hr = Integer.parseInt(hrs);
            int mm = Integer.parseInt(mins);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hr);
            calendar.set(Calendar.MINUTE, mm);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date date = calendar.getTime();
            System.out.println(date);
            time.schedule(new TimerUpdate(email), date, TimeUnit.DAYS.toMillis(1));
    }else {
            System.out.println("cancelling timer");
            time.cancel();
            time.purge();
            time=new Timer();
            setAS.setDisable(true);
            String[] parts = newVal.split(":");
            String hrs = parts[0];
            String mins = parts[1];
            int hr = Integer.parseInt(hrs);
            int mm = Integer.parseInt(mins);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hr);
            calendar.set(Calendar.MINUTE, mm);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date date = calendar.getTime();
            System.out.println(date);
            time.schedule(new TimerUpdate(email), date, TimeUnit.DAYS.toMillis(1));
        }
    }

    public void run(){}

}



