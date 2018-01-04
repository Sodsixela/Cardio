package cardio.cardio;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import cardio.cardio.R;

public class Graphic extends AppCompatActivity {
    public static final String GET_GRAPHIC = "getDataType";
    LineGraphSeries<DataPoint> series;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            highLimit.appendData(new DataPoint(0,30), true, 2);
            highLimit.appendData(new DataPoint(30,30), true, 2);
            lowLimit.appendData(new DataPoint(0,0), true, 2);
            lowLimit.appendData(new DataPoint(30,0), true, 2);
            data=DataUser.getInstance().getAcc();
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
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
        for (double datas : data)//some other use of data
        {
            average += datas;
        }
        average = average/data.size();

        TextView textAverage = findViewById(R.id.textView5);
        textAverage.setText(" "+average+" ");
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
                // User chose the "Settings" item, show the app settings UI...
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

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
