package e.administrateur.cardioproject;

import java.util.ArrayList;

class DataUser {//we stock here all the user data from the server
    private static final DataUser ourInstance = new DataUser();
    private ArrayList<Double> tmp;
    private ArrayList<Double> acc;
    private ArrayList<Double> crd;
    private boolean run=true;

    ArrayList<Double> getTmp() {
        return tmp;
    }

    void setTmp(ArrayList<Double> temp) {
        this.tmp = temp;
    }

    ArrayList<Double> getAcc() {
        return acc;
    }

    void setAcc(ArrayList<Double> accel) {
        this.acc = accel;
    }

    ArrayList<Double> getCrd() {
        return crd;
    }

    void setCrd(ArrayList<Double> cardio) {
        this.crd = cardio;
    }

    void setRun(boolean run){this.run = run;}

    boolean getRun(){return run;}
    static DataUser getInstance() {

        return ourInstance;
    }

    private DataUser() {
    }
}
