package cardio.cardio;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class Login extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "getMessage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();}
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        String success="";
        TextView textView = findViewById(R.id.textView2);
        textView.setText("Connexion...");

        try {
            new CallServer().execute("").get();//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer
            if(User.getInstance().getName()!=null)//if the login worked go in the main page
            {
                success="Hello "+User.getInstance().getName();
                Intent intent = new Intent(this, DisplayMessageActivity.class);
                intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                startActivity(intent);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
    private class CallServer extends AsyncTask<String, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;

            try {

                EditText editText = (EditText) findViewById(R.id.editText);//we take the name and password
                EditText editText2 = (EditText) findViewById(R.id.editText2);
                Socket client = new Socket("192.168.43.212", 8080);  //connect to server

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                if (client != null && out != null && in != null) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3
                        String on= "{\"type\":\"Connexion\",\"name\":\""+editText.getText().toString()+"\",\"pwd\":\""+editText2.getText().toString()+"\"}";
                        out.println(on);
                        String message = in.readLine();

                        try {
                            JSONObject jsonObj = new JSONObject( message);
                            if(jsonObj.getString("Connexion").equals("Connected"))//if it's ok we initialise the data of the user
                            {
                                User.getInstance().setName(editText.getText().toString());
                                User.getInstance().setPassword(editText2.getText().toString());
                            }
                            responseLine=jsonObj.getString("Connexion");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            responseLine="Error";
                        }

                        // clean up:
                        // close the output stream
                        // close the input stream
                        // close the socket
                        out.close();
                        in.close();
                        client.close();   //closing the connection
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
                Server.getInstance().setIpAddress("");
            }

            return responseLine;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView textView = findViewById(R.id.textView2);
            textView.setText(result);

            // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
            TextView textView = findViewById(R.id.textView2);
            textView.setText("Connexion...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
