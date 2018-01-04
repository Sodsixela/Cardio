package cardio.cardio;

import java.util.ArrayList;

/**
 * Created by Administrateur on 04-Jan-18.
 */

class DataUser {
    private static final DataUser ourInstance = new DataUser();
    private ArrayList<Double> tmp;
    private ArrayList<Double> acc;
    private ArrayList<Double> crd;

    public ArrayList<Double> getTmp() {
        return tmp;
    }

    public void setTmp(ArrayList<Double> temp) {
        this.tmp = temp;
    }

    public ArrayList<Double> getAcc() {
        return acc;
    }

    public void setAcc(ArrayList<Double> accel) {
        this.acc = accel;
    }

    public ArrayList<Double> getCrd() {
        return crd;
    }

    public void setCrd(ArrayList<Double> cardio) {
        this.crd = cardio;
    }

    static DataUser getInstance() {

        return ourInstance;
    }

    private DataUser() {
    }
}
