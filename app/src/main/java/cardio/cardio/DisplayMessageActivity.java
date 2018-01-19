package cardio.cardio;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity {
    public static final String GET_GRAPHIC = "getDataType";
    private ArrayList<Float> Data;
    private static int port = 8080;
    private static String host="192.168.43.212";
    protected void onCreate(Bundle savedInstanceState) {//initialise the page
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);//show the message from the previous page

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        if(!(message ==null))
            new CallServer().execute("");//to call the server we need a thread
    }
    public void getData(View view)//when pressing refresh, to have the lastest data into the server
    {
        new CallServer().execute("");
    }


    public void getCardio(View view)
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
                            JSONObject jsonObj = new JSONObject( message);
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
                                    System.out.println(jTemp.getDouble(i));
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
                            }
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
            TextView txt = (TextView) findViewById(R.id.textView);
            txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
