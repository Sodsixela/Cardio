package cardio.cardio;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cardio.cardio.R;

import static cardio.cardio.Login.EXTRA_MESSAGE;

public class Graphic extends AppCompatActivity {
    public static final String GET_GRAPHIC = "getDataType";
    private static int port = 8080;
    private static String host=Server.getInstance().getIpAddress();
    private LineGraphSeries<DataPoint> series;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DataUser.getInstance()==null)
        {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, "@string/resfresh");
            startActivity(intent);
            return;
        }
        User.setC(getApplicationContext());
        setContentView(R.layout.activity_graphic);
        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.);
        setSupportActionBar(myToolbar);*/
        Intent intent = getIntent();
        String message = intent.getStringExtra(DisplayMessageActivity.GET_GRAPHIC);
        TextView textView = findViewById(R.id.textView3);
        textView.setText(message);

        LineGraphSeries<DataPoint> highLimit = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> lowLimit = new LineGraphSeries<DataPoint>();
        int i=0;
        ArrayList<Double> data = new ArrayList<>();//it will take the data of the choosen table
        if(message.equals("CARDIO")){///depends on the chosen table
            highLimit.appendData(new DataPoint(0,100), true, 2);
            highLimit.appendData(new DataPoint(30,100), true, 2);
            lowLimit.appendData(new DataPoint(0,70), true, 2);
            lowLimit.appendData(new DataPoint(30,70), true, 2);
            data=DataUser.getInstance().getCrd();
        }else if (message.equals("TEMP"))
        {
            highLimit.appendData(new DataPoint(0,37.8), true, 2);
            highLimit.appendData(new DataPoint(30,37.8), true, 2);
            lowLimit.appendData(new DataPoint(0,36.1), true, 2);
            lowLimit.appendData(new DataPoint(30,36.1), true, 2);
            data=DataUser.getInstance().getTmp();
        }else if (message.equals("ACCEL"))
        {
            //series = (BarGraphSeries<DataPoint>) this.series;
            highLimit.appendData(new DataPoint(0,30), true, 2);
            highLimit.appendData(new DataPoint(30,30), true, 2);
            lowLimit.appendData(new DataPoint(0,0), true, 2);
            lowLimit.appendData(new DataPoint(30,0), true, 2);
            data=DataUser.getInstance().getAcc();
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(24);
        /*graph.getViewport().setScrollable(true);
        graph.getViewport().setMinX(10);*/
        graph.addSeries(highLimit);
        graph.addSeries(lowLimit);
        highLimit.setColor(Color.RED);
        lowLimit.setColor(Color.RED);
        series = new LineGraphSeries<DataPoint>();//show graph
        for(int j=0; j<data.size();j++)
        {
            series.appendData(new DataPoint(j,data.get(j)), true, data.size());
        }
        graph.addSeries(series);
        float average=0;
        int warning=0;

        for (double value : data)//some other use of data
        {

            average += value;
            if(highLimit.getHighestValueY()<value || lowLimit.getLowestValueY()>value)
                warning++;

        }
        average = average/data.size();
        if(warning>0)
        {
            TextView warningView = findViewById(R.id.warning);
            warningView.setText("Warning, you go beyond\nthe limits "+warning +" times");
        }
        TextView textAverage = findViewById(R.id.textView5);
        textAverage.setText(" "+average+" ");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Intent intent = getIntent();
        String message = intent.getStringExtra(DisplayMessageActivity.GET_GRAPHIC);
        savedInstanceState.putString("graphic", message);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        Intent intent = getIntent();
        String message = savedInstanceState.getString("MyString");
        intent.putExtra(GET_GRAPHIC, message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ajoute les entrées de menu_test à l'ActionBar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_parameter:
                intent = new Intent(this, /*Parameter*/SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_cardio:
                intent = new Intent(this, Graphic.class);
                intent.putExtra(GET_GRAPHIC, "CARDIO");
                startActivity(intent);
                return true;
            case R.id.action_temp:
                intent = new Intent(this, Graphic.class);
                intent.putExtra(GET_GRAPHIC, "TEMP");
                startActivity(intent);
            return true;

            case R.id.action_accel:
                intent = new Intent(this, Graphic.class);
                intent.putExtra(GET_GRAPHIC, "ACCEL");
                startActivity(intent);
            return true;

            case R.id.action_refresh:
                try {
                    new CallServer().execute("").get();
                    TextView textView = findViewById(R.id.textView3);
                    String kind= textView.getText().toString();
                    intent = new Intent(this, Graphic.class);
                    intent.putExtra(GET_GRAPHIC, kind);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
