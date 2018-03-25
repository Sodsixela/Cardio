package e.administrateur.cardioproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static e.administrateur.cardioproject.Login.EXTRA_MESSAGE;

public class Graphic extends AppCompatActivity {
    public static final String GET_GRAPHIC = "getDataType";
    private static String host=Server.getInstance().getIpAddress();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if we lost the data we go back to the menu
        if(DataUser.getInstance()==null)
        {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, "@string/resfresh");
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_graphic);
        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.);
        setSupportActionBar(myToolbar);*/
        Intent intent = getIntent();
        String message = intent.getStringExtra(DisplayMessageActivity.GET_GRAPHIC);
        TextView textView = findViewById(R.id.textView3);
        textView.setText(message);

        LineGraphSeries<DataPoint> highLimit = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> lowLimit = new LineGraphSeries<>();
        ArrayList<Double> data = new ArrayList<>();//it will take the data of the choosen table
        switch (message) {
            case "CARDIO": ///depends on the chosen table
                textView.setText(R.string.button_cardio);
                highLimit.appendData(new DataPoint(0, 100), true, 2);
                highLimit.appendData(new DataPoint(30, 100), true, 2);
                lowLimit.appendData(new DataPoint(0, 70), true, 2);
                lowLimit.appendData(new DataPoint(30, 70), true, 2);
                data = DataUser.getInstance().getCrd();
                break;
            case "TEMP":
                textView.setText(R.string.button_temperature);
                highLimit.appendData(new DataPoint(0, 37.8), true, 2);
                highLimit.appendData(new DataPoint(30, 37.8), true, 2);
                lowLimit.appendData(new DataPoint(0, 36.1), true, 2);
                lowLimit.appendData(new DataPoint(30, 36.1), true, 2);
                data = DataUser.getInstance().getTmp();
                break;
            case "ACCEL":
                textView.setText(R.string.button);
                //series = (BarGraphSeries<DataPoint>) this.series;
                highLimit.appendData(new DataPoint(0, 30), true, 2);
                highLimit.appendData(new DataPoint(30, 30), true, 2);
                lowLimit.appendData(new DataPoint(0, 0), true, 2);
                lowLimit.appendData(new DataPoint(30, 0), true, 2);
                data = DataUser.getInstance().getAcc();
                break;
        }

        GraphView graph = findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(24);
        /*graph.getViewport().setScrollable(true);
        graph.getViewport().setMinX(10);*/
        graph.addSeries(highLimit);
        graph.addSeries(lowLimit);
        highLimit.setColor(Color.RED);
        lowLimit.setColor(Color.RED);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
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
        ListView infolist= findViewById(R.id.listInfo);
        String[] title;
        Drawable[] icon;
        String[] info;
        if(warning>0)
        {
            title= new String[]{getString(R.string.average), getString(R.string.warning)};
            icon= new Drawable[]{getResources().getDrawable(android.R.drawable.ic_search_category_default),getResources().getDrawable(android.R.drawable.ic_dialog_alert)};
            String warn="You go beyond the limits "+warning +" times";
            info=new String[]{String.valueOf(average),warn};
        }
        else
        {
            title= new String[]{getString(R.string.average)};
            icon= new Drawable[]{getResources().getDrawable(android.R.drawable.ic_search_category_default)};
            info=new String[]{String.valueOf(average)};
        }
        ListInfo listInfo= new ListInfo(getApplicationContext(),title,info,icon);
        infolist.setAdapter(listInfo);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {//here to save datas
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
        String message = savedInstanceState.getString("graphic");
        intent.putExtra(GET_GRAPHIC, message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //take the xml file menu and put in here
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {//into the menu, if we want to change of graphic
            case R.id.action_parameter:
                intent = new Intent(this, SettingsActivity.class);
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

    //WORK IN THE SAME WAY THAN THE ONE IN DISPLAYMESSAGEACTIVITY

    private class CallServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {//watch CallServer in Login for more information
            try {
                InetAddress address = InetAddress.getByName(host);
                int port = 8088;
                Socket client = new Socket(address, port);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                try {
                    String on= "{\"type\":\"Data\"}";
                    out.println(on);
                    /*ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.writeObject(ok);//Send the message "Data" to the server to have back all the data*/
                    String message = in.readLine();
                    try {
                        JSONArray jCardio = new JSONArray(message);
                        System.out.println("Json array : "+jCardio);
                        ArrayList<Double> fill=new ArrayList<>();
                        for (int i=0;i<jCardio.length();i++){
                            //fill.add(jCardio.getDouble(i));
                            if(jCardio.getJSONObject(i).get("type").equals("heartrate"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setCrd(fill);
                        fill=new ArrayList<>();
                        for (int i=0;i<jCardio.length();i++){
                            //fill.add(jCardio.getDouble(i));
                            if(jCardio.getJSONObject(i).get("type").equals("temperature"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setTmp(fill);
                        fill=new ArrayList<>();
                        for (int i=0;i<jCardio.length();i++){
                            //fill.add(jCardio.getDouble(i));
                            if(jCardio.getJSONObject(i).get("type").equals("acceleration"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                                System.out.println(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setAcc(fill);

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
