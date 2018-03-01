package cardio.cardio;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class Parameter extends AppCompatActivity {
    private String value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User.setC(getApplicationContext());
        setContentView(R.layout.activity_parameter);
        Switch onOffSwitch = (Switch)  findViewById(R.id.callSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    //ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) = PackageManager.PERMISSION_GRANTED;
                    //ActivityCompat.
                }
                else
                {

                }
            }

        });
    }

    public void changeName(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change username");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                value = input.getText().toString();
                System.out.println(value);
                try {
                    new CallServer().execute("Username").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void changePwd(View view)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change password");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                value = input.getText().toString();
                try {
                    new CallServer().execute("Password").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private class CallServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = "error";
            try {
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8080);  //connect to server


                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                if (client != null /*&& os != null && is != null*/) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3

                        String on= "{\"type\":\"Modify\",\"dataset\":\""+params[0]+"\",\"value\":\""+value+"\"}";
                        out.println(on);
                        String message = in.readLine();

                        System.out.println("message: "+ (String) message);
                        try {
                            JSONObject jsonObj = new JSONObject( message);
                            if(jsonObj.getString("Change").contains("Changed"))//if it's ok we initialise the data of the user
                            {
                                responseLine=jsonObj.getString("Change");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            responseLine="Error";
                        }


                        out.close();
                        in.close();
                        client.close();   //closing the connection
                        //textView.setText("finish");
                    } catch (UnknownHostException e) {
                        responseLine = "Trying to connect to unknown host";
                        System.err.println("Trying to connect to unknown host: " + e);
                    } catch (IOException e) {
                        responseLine = "Error, couldn't reach the server";
                        System.err.println("IOException:  " + e);

                    }
                }


            } catch (UnknownHostException e) {
                responseLine = "Trying to connect to unknown host";
                e.printStackTrace();
            } catch (IOException e) {
                responseLine = "Error, couldn't reach the server";
                e.printStackTrace();
            }
            return responseLine;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Parameter.this, result,Toast.LENGTH_SHORT).show();
            /*System.out.println("zef");
            Toast.makeText(Parameter.this, "Text", Toast.LENGTH_SHORT).show();*/
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {


        }
    }
}
