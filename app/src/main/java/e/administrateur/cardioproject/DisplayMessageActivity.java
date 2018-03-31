package e.administrateur.cardioproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity{
    public static final String GET_GRAPHIC = "getDataType";
    private static int port = 8088;
    private static String host=Server.getInstance().getIpAddress();

    /*graphical elements*/
    private FrameLayout progressBarHolder;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;

    protected void onCreate(Bundle savedInstanceState) {//initialize the page
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);//show the message from the previous page

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
    }

    protected void onStart() {//initialize elements
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        /*TEST to see if you have at least a number in each alert category
        need to be in onstart() in case you change teh parameter and come back (without going in onCreate() then)
         */
        boolean test1=false,test2=false;
        for(int i=1;i<=3;i++)//we alert close friends number
        {
            if(!(prefs.getString("num"+i,"").equals(" ") || prefs.getString("num"+i,"").equals("")) ){
                test1 = true;
            }
        }
        if(!(prefs.getString("doc","").equals(" ") || prefs.getString("doc","").equals("")))
            test2=true;
        else if(!(prefs.getString("emergency","").equals(" ") || prefs.getString("emergency","").equals("")))
            test2=true;
        if(!(test1 && test2))
        {
            Toast.makeText(getApplicationContext(), getString(R.string.needNumbers),Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);//to change of activity
            startActivity(intent);
        }
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        button4=findViewById(R.id.button4);
        button5=findViewById(R.id.button5);
        button6=findViewById(R.id.button6);
        port = 8088;
        progressBarHolder = findViewById(R.id.progressBarHolder);
        Context context = getApplicationContext();
        if (Server.getInstance().getIpAddress() == null) {//if the server has been deleted in Server
            Server.getInstance().setIpAddress(prefs.getString("host",""));
        }
        host = Server.getInstance().getIpAddress();

        /*if some data have been deleted*/
        if (DataUser.getInstance().getAcc()==null || DataUser.getInstance().getTmp()==null || DataUser.getInstance().getCrd()==null  ) {
            new CallServer().execute("");//to call the server we need a thread
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        port = 8088;
        if(Server.getInstance().getIpAddress()==null)
        {
            Server.getInstance().setIpAddress(savedInstanceState.getString("host",""));
        }
        host=Server.getInstance().getIpAddress();
        if (DataUser.getInstance().getAcc()==null || DataUser.getInstance().getTmp()==null || DataUser.getInstance().getCrd()==null  ) {
            new CallServer().execute("");//to call the server we need a thread
        }
    }

    public void getData(View view)//when pressing refresh, to have the lastest data into the server
    {
        new CallServer().execute("");
    }


    public void getCardio(View view)//Show graphic about cardiac frequency
    {
        Intent intent = new Intent(this, Graphic.class);//go into the graphic page
        intent.putExtra(GET_GRAPHIC, "CARDIO");//with a a message for wich graphic we should watch
        startActivity(intent);
    }


    public void getTemp(View view)
    {
        Intent intent = new Intent(this, Graphic.class);
        intent.putExtra(GET_GRAPHIC, "TEMP");
        startActivity(intent);
    }

    public void getAccel(View view)
    {
        Intent intent = new Intent(this, Graphic.class);
        intent.putExtra(GET_GRAPHIC, "ACCEL");
        startActivity(intent);
    }

    public void getParameters(View view)//go to the parameter
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void sendALERT(View view)
    {
        new alertMessage().execute("ALERT");
    }
    public void sendEMERGENCY(View view)
    {
        new alertMessage().execute("EMERGENCY");
    }
    public void stopAlert(View view){
        stopService(new Intent(this, Alert.class));
    }

    /*send a message un UDP to recreate an alert*/
    private class alertMessage extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {


            try {
                byte[] buffer = params[0].getBytes();
                    //On initialise la connexion côté client
                    DatagramSocket client = new DatagramSocket();
                    //On crée notre datagramme
                    InetAddress adresse = InetAddress.getLocalHost();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, 8000);

                    //On lui affecte les données à envoyer
                    packet.setData(buffer);

                    //On envoie au serveur
                    client.send(packet);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /*ASYNC TASK TO GET DATA
    this here that we put data into DataUser
    Else work like callserver in login*/
    private class CallServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {//watch CallServer in Login for more information
            try {
                InetAddress address = InetAddress.getByName(host);
                Socket client = new Socket(address, port);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                try {
                    String on= "{\"type\":\"Data\"}";

                    out.println(on);
                    String message = in.readLine();
                    try {

                        //FIRST we put what we got into a json array

                        JSONArray jCardio = new JSONArray(message);
                        ArrayList<Double> fill=new ArrayList<>();

                        //THEN we will check that for each category we got it into the JSONArray

                        for (int i=0;i<jCardio.length();i++){//we select the value of heartrate
                            if(jCardio.getJSONObject(i).get("type").equals("heartrate"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setCrd(fill);//and then fill it
                        fill= new ArrayList<>();//same we the other kind of data
                        for (int i=0;i<jCardio.length();i++){
                            if(jCardio.getJSONObject(i).get("type").equals("temperature"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setTmp(fill);
                        fill=new ArrayList<>();
                        for (int i=0;i<jCardio.length();i++){
                            if(jCardio.getJSONObject(i).get("type").equals("acceleration"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setAcc(fill);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // clean up:
                    // close the output stream
                    // close the input stream
                    // close the socket
                    out.close();
                    in.close();
                    client.close();   //closing the connection
                } catch (UnknownHostException e) {
                    System.err.println("Trying to connect to unknown host: " + e);
                } catch (IOException e) {
                    System.err.println("IOException:  " + e);
                }


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            /*enable graphic elements and stop progress bar*/
            button2.setEnabled(true);
            button3.setEnabled(true);
            button4.setEnabled(true);
            button5.setEnabled(true);
            button6.setEnabled(true);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            ProgressBar pb = findViewById(R.id.logWait);
            pb.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
             /*disable graphic elements and launch progress bar*/
            button2.setEnabled(false);
            button3.setEnabled(false);
            button4.setEnabled(false);
            button5.setEnabled(false);
            button6.setEnabled(false);

            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
            ProgressBar pb = findViewById(R.id.logWait);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
