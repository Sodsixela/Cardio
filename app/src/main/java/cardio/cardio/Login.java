package cardio.cardio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
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
        Context context = getApplicationContext();
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
                System.out.println("read:"+stringBuilder.toString());
                if(stringBuilder.toString().isEmpty())
                {
                    new FindServer().execute("").get();
                    System.out.println("done");
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        String success="";
        TextView textView = findViewById(R.id.textView2);
        textView.setText("Connection...");

        try {
            new CallServer().execute("").get();//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer
            if(Server.getInstance().getIpAddress().equals(""))
            {
                textView.setText("Searching the server");
                new FindServer().execute("").get();
            }
            if(User.getInstance().getName()!=null)//if the login worked go in the main page
            {
                success="Hello "+User.getInstance().getName();
                try
                {
                    if(Build.VERSION.SDK_INT > 15)
                    {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            // request permission (see result in onRequestPermissionsResult() method)
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 123);
                            textView.setText("Sign in again after allowing sms");
                            return;
                        }
                        User.setC(getApplicationContext());
                        new AlertCall((LocationManager) getSystemService(Context.LOCATION_SERVICE)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Intent intent = new Intent(this, DisplayMessageActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                        startActivity(intent);
                    } else
                    {
                        User.setC(getApplicationContext());
                        new AlertCall((LocationManager) getSystemService(Context.LOCATION_SERVICE)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Intent intent = new Intent(this, DisplayMessageActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                        startActivity(intent);
                    }
                    /*if(Build.VERSION.SDK_INT > 22)
                    {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling

                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                            textView.setText("Sign in again after allowing phone call");

                            return;
                                //textView.setText("You need to allow phone call to start the application");
                        }
                        new AlertCall(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Intent intent = new Intent(this, DisplayMessageActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                        startActivity(intent);
                    }
                    else
                    {
                        new AlertCall(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Intent intent = new Intent(this, DisplayMessageActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                        startActivity(intent);
                    }*/
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
    private class CallServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            publishProgress("Connection...");
            try {

                System.out.println("1");
                EditText editText = (EditText) findViewById(R.id.editText);//we take the name and password
                EditText editText2 = (EditText) findViewById(R.id.editText2);
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8080);  //connect to server


                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                if (client != null /*&& os != null && is != null*/) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3

                        String on= "{\"type\":\"Connexion\",\"name\":\""+editText.getText().toString()+"\",\"pwd\":\""+editText2.getText().toString()+"\"}";
                        out.println(on);
                        String message = in.readLine();

                        System.out.println("message: "+ (String) message);
                        try {
                            JSONObject jsonObj = new JSONObject( message);
                            if(jsonObj.getString("Connexion").equals("Connected"))//if it's ok we initialise the data of the user
                            {
                                User.getInstance().setName(editText.getText().toString());
                                User.getInstance().setPassword(editText2.getText().toString());
                                System.out.println(User.getInstance().getName());
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
            if (!result.equals("Connected")){//if it is not good, we show it to the user
                TextView textView = findViewById(R.id.textView2);
                textView.setText(result);
            }


        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... values) {
            TextView textView = findViewById(R.id.textView2);
            //textView.setText(values[0]);
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
                        }
                        System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }
                System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
                publishProgress("Searching server");
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
                    System.out.println("HEY"+message);
                    responseLine=receivePacket.getAddress().getHostAddress();
                    Server.getInstance().setIpAddress(receivePacket.getAddress().getHostAddress());
                    Context context = getApplicationContext();
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
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... values) {
            TextView textView = findViewById(R.id.textView2);
            textView.setText(values[0]);}

    }
}
