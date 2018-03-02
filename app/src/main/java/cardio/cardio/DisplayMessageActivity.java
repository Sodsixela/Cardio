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

public class DisplayMessageActivity extends AppCompatActivity implements LocationListener{
    public static final String GET_GRAPHIC = "getDataType";
    private ArrayList<Float> Data;
    private static int port = 8080;
    private static String host=Server.getInstance().getIpAddress();

    protected void onCreate(Bundle savedInstanceState) {//initialise the page
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_display_message);

        User.setC(getApplicationContext());
        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Location localisation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1000, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1000, this);
*/

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);//show the message from the previous page

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        //if(!(message ==null))
          //  new CallServer().execute("");//to call the server we need a thread
    }

    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);
        port = 8080;
        Context context = getApplicationContext();
        if(Server.getInstance().getIpAddress()==null)
        {
            try {
                InputStream inputStream = context.openFileInput("host.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(receiveString);
                    }

                    Server.getInstance().setIpAddress(stringBuilder.toString());

                    inputStream.close();

                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        host=Server.getInstance().getIpAddress();
        if(!(message ==null))
            new CallServer().execute("");//to call the server we need a thread
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        port = 8080;
        host=Server.getInstance().getIpAddress();
        System.out.println("restaure");
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

    public void getParameters(View view)
    {
        Intent intent = new Intent(this, /*Parameter*/SettingsActivity.class);
        startActivity(intent);
    }
    @Override
    public void onLocationChanged(Location loc) {
        System.out.println("1");
        loc.getLatitude();
        loc.getLongitude();
        System.out.println("Latitude" +loc.getLatitude()+ " Longitude" + loc.getLongitude() +"f");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("2f");
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("3f");
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("4f");
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
                        /*ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                        out.writeObject(ok);//Send the message "Data" to the server to have back all the data*/
                        String message = in.readLine();
                        try {
                            /*JSONArray jCardio = new JSONArray(message);
                            System.out.println("Json array : "+jCardio);
                            ArrayList<Double> fill=new ArrayList<>();
                            if (jCardio != null) {
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("heartrate"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                        System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setCrd(fill);
                                fill=new ArrayList<Double>();
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("temperature"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                        System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setTmp(fill);
                                fill=new ArrayList<Double>();
                                for (int i=0;i<jCardio.length();i++){
                                    //fill.add(jCardio.getDouble(i));
                                    if(jCardio.getJSONObject(i).get("type").equals("acceleration"))
                                    {
                                        fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                        System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                                    }
                                }
                                DataUser.getInstance().setAcc(fill);
                            }*/

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
                        //ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                       /* try {

                            ///can be changed, here we limit us to 3 table of 24 number in only one for the first test of socket
                            ArrayList<Double> objects = (ArrayList<Double>)in.readObject();//where we will have these data
                            ArrayList<Double> fill=new ArrayList<Double>();//used to split the data from objects
                            for(int i=0;i<objects.size();i++)//we split objects to put the good data into the good DataUser table
                            {
                                fill.add(objects.get(i));
                                if(i==23) {
                                    DataUser.getInstance().setCrd(fill);
                                    fill=new ArrayList<Double>();
                                }
                                else  if(i==47) {
                                    DataUser.getInstance().setTmp(fill);
                                    fill=new ArrayList<Double>();
                                }
                                else  if(i==71) {
                                    DataUser.getInstance().setAcc(fill);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }*/

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
