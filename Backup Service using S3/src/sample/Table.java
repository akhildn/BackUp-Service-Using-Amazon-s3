package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Aditya on 30-06-2015.
 */
public class Table {
    private final SimpleStringProperty rfname;
    private final SimpleStringProperty rfpath;
    private final SimpleStringProperty rftime;

    public Table(String sfname,String sfpath,String sftime){
        this.rfname=new SimpleStringProperty((sfname));
        this.rfpath=new SimpleStringProperty((sfpath));
        this.rftime=new SimpleStringProperty((sftime));
    }

    public String getRfname(){
        return rfname.get();
    }
    public void setRfname(String v){
        rfname.set(v);
    }

    public String getRfpath(){
        return rfpath.get();
    }
    public void setRfpath(String v){
        rfpath.set(v);
    }

    public String getRftime(){
        return rftime.get();
    }
    public void setRftime(String v){
        rftime.set(v);
    }
}
