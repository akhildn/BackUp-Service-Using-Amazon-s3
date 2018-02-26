package sample;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TimerTask;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Created by Aditya on 06-07-2015.
 */
public class TimerUpdate extends TimerTask {

    String email;

    TimerUpdate(String x){
        email=x;

    }

    String bname;
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    AmazonS3 conn;
    int pbl;


    public void run() {
        try {
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
                    String lastmodified = sdf.format(gfile.lastModified());
                    if (lmodified.equals(lastmodified)) {

                    } else {

                        conn.putObject(new PutObjectRequest(bname, fname, gfile).withCannedAcl(CannedAccessControlList.PublicRead));

                        String iquery="update project.fileinfo set lastmodified = ? where email = ? AND filepath = ?";

                        preparedStatement = connect.prepareStatement(iquery);
                        preparedStatement.setString(1, lastmodified);
                        preparedStatement.setString(2, email);
                        preparedStatement.setString(3, fpath);
                        preparedStatement.executeUpdate();
                    }
                }
                showMessageDialog(null, "auto update performed");
                System.out.println("done");
            }

        } catch (Exception ex) {

            System.out.println("error running thread " + ex.getMessage());
        }

    }
}
