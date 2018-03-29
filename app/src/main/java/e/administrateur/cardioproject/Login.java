package e.administrateur.cardioproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class Login extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "getMessage";
    private boolean first=false;

    //components of the layout
    private EditText name;
    private EditText password;
    private TextView info;
    private ProgressBar pb;
    private Button click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        //we initiate each variable
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        name=findViewById(R.id.editText);
        password=findViewById(R.id.editText2);
        info=findViewById(R.id.textView2);
        pb = findViewById(R.id.logWait);
        click = findViewById(R.id.button);

        /*We check if the name is already in the persistence
        if fo then it is a first connection
         */
        if(prefs.getString("name","").equals(""))
        {
            first=true;
            AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
            alertDialog.setTitle(getString(R.string.first));
            alertDialog.setMessage(getString(R.string.setnp));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            info.setText(getString(R.string.setnp));
        }

        /*if we don't have the network*/
        if(prefs.getString("host"," ").equals(" "))
        {
            new FindServer().execute("");
        }

        Server.getInstance().setIpAddress(prefs.getString("host",""));
    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {//here to save datas
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("loginName", name.getText().toString());
        savedInstanceState.putString("loginPwd",password.getText().toString());
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        name.setText(savedInstanceState.getString("loginName"));
        password.setText(savedInstanceState.getString("loginPwd"));
    }
    /* Called when the user taps the Send button */
    public void sendMessage(View view) {
        info.setText(getString(R.string.connection));

        /*We check all the permissions*/
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            info.setText(getString(R.string.acceptSMS));
            return;
        }

        if(first)
        {
            new FindServer().execute("");//we search the network
            //need authorization too
            /*For the first connection we take the username and password written and send it to the server*/
            new SettingsActivity.CallServer(name.getText().toString(),getApplicationContext()).execute("Username");
            pause();

            new SettingsActivity.CallServer(password.getText().toString(),getApplicationContext()).execute("Password");

            unlock();
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);//to change of activity
            Intent intent2 = new Intent(getApplicationContext(), Alert.class);//to start the alert listener
            startService(intent2);
            Toast.makeText(getApplicationContext(), "Set some emergency phone numbers in notification", Toast.LENGTH_SHORT).show();
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startActivity(intent);
        }
        else
            new CallServer().execute("");//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer
    }
    public void pause()
    {
        pb.setVisibility(View.VISIBLE);
        click.setEnabled(false);//to show the progressBar
    }
    public void unlock()
    {
        pb.setVisibility(View.GONE);
        click.setEnabled(true);//to hide the progress bar when it is finished
    }
    private class CallServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine;//we stock the answer here

            try {
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8088);  //connect to server
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                try {
                    // The capital string before each colon has a special meaning to SMTP
                    // you may want to read the SMTP specification, RFC1822/3

                    /*the json that we send*/
                    String on= "{\"type\":\"Connexion\",\"name\":\""+name.getText().toString()+"\",\"pwd\":\""+password.getText().toString()+"\"}";
                    out.println(on);

                    /*then we wait an answer*/
                    String message = in.readLine();
                    try {
                        JSONObject jsonObj = new JSONObject( message);
                        if(jsonObj.getString("Connexion").equals("Connected"))//if it's ok we initialise the data of the user
                        {
                            User.setName(name.getText().toString());
                            User.setPassword(password.getText().toString());
                        }
                        responseLine=jsonObj.getString("Connexion");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        responseLine="Error";
                    }

                    out.close();
                    in.close();
                    client.close();   //closing the connection
                } catch (UnknownHostException e) {
                    responseLine = "Error, trying to connect to unknown host";
                    System.err.println("Error, trying to connect to unknown host: " + e);
                } catch (IOException e) {
                    responseLine = "Error, couldn't reach the server";
                    System.err.println("IOException:  " + e);

                }



            } catch (UnknownHostException e) {
                responseLine = "Error, trying to connect to unknown host";
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
            //we enable the button
            pb.setVisibility(View.GONE);
            if(result.contains("Error"))//if there is an error maybe it is because the network changed, we check it
            {
                info.setText(getString(R.string.searchServ));
                new FindServer().execute("");
            }
            else if (!result.equals("Connected")){//if it is not good, we show it to the user
                click.setEnabled(true);
                info.setText(result);
            }

           else//if the login worked go in the main page
            {
                click.setEnabled(true);
                info.setText("");
                String success=getString(R.string.hello)+", "+User.getName();
                try
                {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor =prefs.edit();
                    editor.putString("Username", User.getName());
                    Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);//the menu activity
                    Intent intent2 = new Intent(getApplicationContext(), Alert.class);//the alert listener
                    startService(intent2);
                    Toast.makeText(getApplicationContext(), success, Toast.LENGTH_SHORT).show();
                    //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                    startActivity(intent);


                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        }

        @Override
        protected void onPreExecute() {//we disable the button and show the progress bar
            pb.setVisibility(View.VISIBLE);
            click.setEnabled(false);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    /*We try to find the server on the same network by sending udp broadcast and waiting an anwser*/
    private class FindServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            // Find the server using UDP broadcast
            try {
                //Open a random port to send the package
                DatagramSocket c = new DatagramSocket();
                c.setBroadcast(true);
                c.setSoTimeout(5000);//we stop if it's too long

                byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
                //Try the 255.255.255.255 first
                //we want the mask
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                    c.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Broadcast the message over all the network interfaces
                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }
                        // Send the broadcast package!
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);//we send a broacast message on the local network
                            c.send(sendPacket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                }
                publishProgress(getString(R.string.searchServ));
                //Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);//we catch the answer which should be the ip address of the server
                //We have a response
                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    responseLine=receivePacket.getAddress().getHostAddress();
                    Server.getInstance().setIpAddress(receivePacket.getAddress().getHostAddress());
                    Context context = getApplicationContext();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);//we stock in the persistance
                    SharedPreferences.Editor editor =prefs.edit();
                    editor.putString("host",receivePacket.getAddress().getHostAddress());
                    editor.apply();

                    Bundle bundle= new Bundle();
                    bundle.putString("host", receivePacket.getAddress().getHostAddress());
                    System.out.println("host:"+bundle.getString("host"));
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("host.txt", Context.MODE_PRIVATE));
                        outputStreamWriter.write(receivePacket.getAddress().getHostAddress());
                        outputStreamWriter.close();
                    }
                    catch (IOException e) {
                        System.out.println("Exception" +  "File write failed: " + e.toString());
                    }
                }
                //Close the port!
                c.close();
            } catch (IOException ex) {
                Logger.getLogger(String.valueOf(ex));
            }

            return responseLine;
        }

        @Override
        protected void onPostExecute(String result) {//disable each element and show the progress bar
            pb.setVisibility(View.GONE);
            click.setEnabled(true);
            if(result!=null) {
                info.setText(getString(R.string.signAgain));
                if(!first)
                    new CallServer().execute("");
            }
            else
            {
                info.setText(getString(R.string.ErrServ));
            }


        }

        @Override
        protected void onPreExecute() {
           pause();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            info.setText(values[0]);}

    }
}
