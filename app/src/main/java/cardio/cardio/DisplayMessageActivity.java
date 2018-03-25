package cardio.cardio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DisplayMessageActivity extends AppCompatActivity{
    public static final String GET_GRAPHIC = "getDataType";
    private ArrayList<Float> Data;
    private static int port = 8088;
    private static String host=Server.getInstance().getIpAddress();
    private FrameLayout progressBarHolder;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;

    protected void onCreate(Bundle savedInstanceState) {//initialise the page
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);//show the message from the previous page

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
    }

    protected void onStart() {
        super.onStart();
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        button4=findViewById(R.id.button4);
        button5=findViewById(R.id.button5);
        button6=findViewById(R.id.button6);

        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);
        port = 8088;
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        Context context = getApplicationContext();
        if (Server.getInstance().getIpAddress() == null) {//SHOULD REPLACED BY PERSISTENCE
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            System.out.println("host:"+prefs.getString("host","0"));
            try {
                InputStream inputStream = context.openFileInput("host.txt");

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    Server.getInstance().setIpAddress(stringBuilder.toString());

                    inputStream.close();

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        host = Server.getInstance().getIpAddress();
        if (DataUser.getInstance().getAcc()==null || DataUser.getInstance().getTmp()==null || DataUser.getInstance().getCrd()==null  ) {
            new CallServer().execute("");//to call the server we need a thread
        }
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        port = 8088;
        host=Server.getInstance().getIpAddress();
        System.out.println("restaure");
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


    ///ASYNC TASK TO GET DATA
    //this here that we put data into DataUser


    private class CallServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {//watch CallServer in Login for more information
            try {
                InetAddress address = InetAddress.getByName(host);
                Socket client = new Socket(address, port);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                if (client != null ) {

                    try {
                        String[] ok= {"Data"};
                        String on= "{\"type\":\"Data\"}";

                        out.println(on);
                        String message = in.readLine();
                        try {

                            //FIRST we put what we got into a json array

                            JSONArray jCardio = new JSONArray(message);
                            System.out.println("Json array : "+jCardio);
                            ArrayList<Double> fill=new ArrayList<>();

                            //THEN we will check that for each category we got it into the JSONArray

                            if (jCardio != null) {
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("heartrate"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                     //   System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setCrd(fill);
                                fill=new ArrayList<Double>();
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("temperature"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                        //System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setTmp(fill);
                                fill=new ArrayList<Double>();
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("acceleration"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                     //   System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setAcc(fill);
                            }

                            //OLD WAY TO GET IT
                            /*JSONObject jsonObj = new JSONObject( message);
                            JSONArray jCardio = jsonObj.getJSONArray("Cardio");
                            ArrayList<Double> fill=new ArrayList<>();
                            if (jCardio != null) {
                                for (int i=0;i<jCardio.length();i++){
                                    fill.add(jCardio.getDouble(i));
                                }
                                DataUser.getInstance().setCrd(fill);
                                fill=new ArrayList<Double>();
                            }
                            JSONArray jTemp = jsonObj.getJSONArray("Temp");
                            if (jTemp != null) {
                                for (int i=0;i<jTemp.length();i++){
                                    fill.add(jTemp.getDouble(i));
                                }
                                DataUser.getInstance().setTmp(fill);
                                fill=new ArrayList<Double>();
                            }
                            JSONArray jAccel = jsonObj.getJSONArray("Accel");
                            if (jAccel != null) {
                                for (int i=0;i<jAccel.length();i++){
                                    fill.add(jAccel.getDouble(i));
                                }
                                DataUser.getInstance().setAcc(fill);
                                fill=new ArrayList<Double>();
                            }*/
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
                        //textView.setText("finish");
                    } catch (UnknownHostException e) {
                        System.err.println("Trying to connect to unknown host: " + e);
                    } catch (IOException e) {
                        System.err.println("IOException:  " + e);
                    }
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
            button2.setEnabled(true);
            button3.setEnabled(true);
            button4.setEnabled(true);
            button5.setEnabled(true);
            button6.setEnabled(true);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            TextView txt = (TextView) findViewById(R.id.textView);
            txt.setText("Executed"); // txt.setText(result);
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.GONE);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {

            button2.setEnabled(false);
            button3.setEnabled(false);
            button4.setEnabled(false);
            button5.setEnabled(false);
            button6.setEnabled(false);

            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
