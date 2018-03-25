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
    private EditText name;
    private EditText password;
    private TextView info;
    private ProgressBar pb;
    private Button click;
    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        name=findViewById(R.id.editText);
        password=findViewById(R.id.editText2);
        info=findViewById(R.id.textView2);
        pb = findViewById(R.id.logWait);
        click = findViewById(R.id.button);
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
        System.out.println("name:"+prefs.getString("name",""));
        System.out.println("host:"+prefs.getString("host","0"));
        if(prefs.getString("host"," ").equals(" "))
        {
            new FindServer().execute("");
        }
        Server.getInstance().setIpAddress(prefs.getString("host",""));
        /*try {
            InputStream inputStream = context.openFileInput("host.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                if(stringBuilder.toString().isEmpty())
                {
                    new FindServer().execute("");
                }
                else
                {
                    Server.getInstance().setIpAddress(stringBuilder.toString());
                }
                inputStream.close();

            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

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
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        info.setText(getString(R.string.connection));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.SEND_SMS}, 123);
            info.setText(getString(R.string.acceptSMS));
            return;
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            info.setText(getString(R.string.acceptSMS));
            return;
        }
        //try {
        if(first)
        {
            new FindServer().execute("");
            //need authorization too
            /*AsyncTask<String, Void, String> chg = */new SettingsActivity.CallServer(name.getText().toString()).execute("Username");
            pause();
            //while (!chg.isCancelled());
            /*chg = */new SettingsActivity.CallServer(password.getText().toString()).execute("Password");
            //while (!chg.isCancelled());
            unlock();
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            Intent intent2 = new Intent(getApplicationContext(), Alert.class);
            startService(intent2);
            Toast.makeText(getApplicationContext(), "Set some emergency phone numbers in notification", Toast.LENGTH_SHORT).show();
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startActivity(intent);
        }
        else
            new CallServer().execute("");//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer

        /*}/*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/


    }
    public void pause()
    {
        pb.setVisibility(View.VISIBLE);
    }
    public void unlock()
    {
        pb.setVisibility(View.GONE);
        click.setEnabled(true);
    }
    private class CallServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine;
            publishProgress(getString(R.string.connection));

            try {
                System.out.println("ip:"+ InetAddress.getByName(""));
                // System.out.println("ip:"+InetAddress.get);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8088);  //connect to server
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                try {
                    // The capital string before each colon has a special meaning to SMTP
                    // you may want to read the SMTP specification, RFC1822/3

                    String on= "{\"type\":\"Connexion\",\"name\":\""+name.getText().toString()+"\",\"pwd\":\""+password.getText().toString()+"\"}";
                    out.println(on);
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
                    //textView.setText("finish");
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
            pb.setVisibility(View.GONE);
            if(result.contains("Error"))
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
                    Bundle saveName= new Bundle();

                    saveName.putString("Username", User.getName());
                    Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);
                    Intent intent2 = new Intent(getApplicationContext(), Alert.class);
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
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
            click.setEnabled(false);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }
    private class FindServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            // Find the server using UDP broadcast
            try {
                //Open a random port to send the package
                DatagramSocket c = new DatagramSocket();
                c.setBroadcast(true);
                c.setSoTimeout(5000);
                //byte[] sendData= "{\"type\":\"Data\"}".getBytes();
                byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
                //Try the 255.255.255.255 first
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                    c.send(sendPacket);
                    System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
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
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                            c.send(sendPacket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }
                System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
                publishProgress(getString(R.string.searchServ));
                //Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);
                //We have a response
                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    responseLine=receivePacket.getAddress().getHostAddress();
                    Server.getInstance().setIpAddress(receivePacket.getAddress().getHostAddress());
                    Context context = getApplicationContext();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
        protected void onPostExecute(String result) {
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
