package cardio.cardio;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "getMessage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            try {
                EditText editText = (EditText) findViewById(R.id.editText);//we take the name and password
                EditText editText2 = (EditText) findViewById(R.id.editText2);
                Socket client = new Socket("192.168.0.2", 8080);  //connect to server
                DataOutputStream os = null;//input output for simple type
                DataInputStream is = null;
                os = new DataOutputStream(client.getOutputStream());//initialise
                is = new DataInputStream(client.getInputStream());
                if (client != null && os != null && is != null) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3
                        /*os.writeBytes("Connexion");
                        System.out.println("ok");
                        os.writeBytes(editText.getText().toString());
                        System.out.println("ok");
                        os.writeBytes(editText2.getText().toString());
                        System.out.println("ok");*/

                        String[] data = {"Connexion", editText.getText().toString(),editText2.getText().toString()};
                        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());//output of object
                        out.writeObject(data);//send 3 String: Connexion to tell what to do to the server and name password
                        responseLine= is.readLine();//catch the answer
                        System.out.println(responseLine);
                        if(responseLine.equals("Connected"))//if it's ok we initialise the data of the user
                        {
                            User.getInstance().setName(editText.getText().toString());
                            User.getInstance().setPassword(editText2.getText().toString());
                            System.out.println(User.getInstance().getName());
                        }
                        //String[] ok= {"OK"};
                        //out.writeObject(ok);
                        /*os.writeBytes("ok");

                        ObjectInputStream objectInput = new ObjectInputStream(client.getInputStream()); //Error Line!


                        System.out.println("ok");
                        ArrayList<ArrayList<Double>> objects = (ArrayList<ArrayList<Double>>)objectInput.readObject();
                        for(ArrayList<Double> object : objects)
                        {
                            for(Double datas : object)
                            {
                                System.out.println(datas);
                            }
                            System.out.println(" ");
                        }*/
                        // clean up:
                        // close the output stream
                        // close the input stream
                        // close the socket
                        out.close();
                        os.close();
                        is.close();
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
            if (!result.equals("Connected")){//if it is not good, we show it to the user
                TextView textView = findViewById(R.id.textView2);
                textView.setText(result);
            }

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
