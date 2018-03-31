package e.administrateur.cardioproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private String kind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        kind = intent.getStringExtra(DisplayMessageActivity.GET_GRAPHIC);
        //if we lost the data we go back to the menu
        setContentView(R.layout.activity_graphic);
        progressBarHolder = findViewById(R.id.progressBarHolder);

    }
    public void onStart() {//initialize graphic elements and Data arrays
        super.onStart();

        if (DataUser.getInstance()==null || DataUser.getInstance().getAcc()==null || DataUser.getInstance().getTmp()==null || DataUser.getInstance().getCrd()==null  ) {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, "@string/resfresh");
            startActivity(intent);
            return;

        }
        TextView textView = findViewById(R.id.textView3);
        textView.setText(kind);

        LineGraphSeries<DataPoint> highLimit = new LineGraphSeries<>();//the red lines, up and down
        LineGraphSeries<DataPoint> lowLimit = new LineGraphSeries<>();
        ArrayList<Double> data = new ArrayList<>();//it will take the data of the choosen table
        switch (kind) {
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
        graph.addSeries(highLimit);
        graph.addSeries(lowLimit);
        highLimit.setColor(Color.RED);
        lowLimit.setColor(Color.RED);
        /*we put the data inside "data" into the graph*/
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

        /*To show statistics to the user in list view*/
        average = average/data.size();
        ListView infolist= findViewById(R.id.listInfo);
        String[] title;
        Drawable[] icon;
        String[] info;
        if(warning>0)//if there is a problem
        {
            title= new String[]{getString(R.string.average), getString(R.string.warning)};
            icon= new Drawable[]{getResources().getDrawable(android.R.drawable.ic_search_category_default),getResources().getDrawable(android.R.drawable.ic_dialog_alert)};
            String warn=getString(R.string.limit)+warning +" "+getString(R.string.x);
            info=new String[]{String.valueOf(average),warn};
        }
        else//if he is healthy
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
        savedInstanceState.putString("graphic", kind);
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
        switch(kind)//to put the image of data type to the top
        {
            case "CARDIO":
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.mipmap.cardiofreq));
                break;
            case "TEMP":
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.mipmap.thermometer));
                break;
            case "ACCEL":
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.mipmap.accelerometer));
                break;

        }

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
                new CallServer().execute("");
                intent = new Intent(this, Graphic.class);
                intent.putExtra(GET_GRAPHIC, kind);
                startActivity(intent);
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
                    String message = in.readLine();
                    try {

                        //FIRST we put what we got into a json array

                        JSONArray jCardio = new JSONArray(message);
                        ArrayList<Double> fill=new ArrayList<>();

                        //THEN we will check that for each category we got it into the JSONArray

                        for (int i=0;i<jCardio.length();i++){
                            //fill.add(jCardio.getDouble(i));
                            if(jCardio.getJSONObject(i).get("type").equals("heartrate"))
                            {
                                fill.add(jCardio.getJSONObject(i).getDouble("value"));
                            }
                        }
                        DataUser.getInstance().setCrd(fill);
                        fill= new ArrayList<>();
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

            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            ProgressBar pb = findViewById(R.id.logWait);
            pb.setVisibility(View.GONE);
            Intent intent = getIntent();
            String message = intent.getStringExtra(DisplayMessageActivity.GET_GRAPHIC);
            intent = new Intent(getBaseContext(), Graphic.class);
            intent.putExtra(GET_GRAPHIC,message);
            startActivity(intent);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {

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
