package cardio.cardio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Administrateur on 24-Jan-18.
 */

public class AlertCall extends AsyncTask<Context, String, String> implements LocationListener{
    DatagramSocket socket;

    final AlertCall thisActivity = this;
    LocationManager locationManager;

    public AlertCall(LocationManager lm){
        locationManager=lm;
    }

    @Override
    protected String doInBackground(Context... params) {
        String responseLine = null;
        Looper.prepare();
        //Keep a socket open to listen to all the UDP trafic that is destined for this port
        try {
            System.out.println("alert ready");
            socket = new DatagramSocket(8000, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            while (true) {
                //Receive a packet
                byte[] recvBuf = new byte[1500];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                //Packet received
                System.out.println("Packet received; data: " + new String(packet.getData()).trim());
                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                if (message.equals("ALERT")) {
                    onProgressUpdate("ALERT");

                }
                else if(message.equals("EMERGENCY"))
                {
                    onProgressUpdate("EMERGENCY");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("end ");
        Looper.loop();
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
        Context context= User.getC();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> numbers= new ArrayList<>();

        if(values[0].equals("ALERT"))
        {
            for(int i=1;i<=3;i++)
            {
                if(!prefs.getString("num"+i,"").equals(""))
                    numbers.add(prefs.getString("num"+i,""));
            }
        }
        else if (values[0].equals("EMERGENCY"))
        {
            if(!prefs.getString("doc","").equals(""))
                numbers.add(prefs.getString("doc",""));
            if(!prefs.getString("emergency","").equals(""))
                numbers.add(prefs.getString("emergency",""));
        }
        locationManager = (LocationManager)User.getC().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                //Location localisation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1000, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1000, this);

                System.out.println("o,");

               /* System.out.println("Latitude" +localisation.getLatitude()+ " Longitude" + localisation.getLongitude());
                Toast.makeText(context, "Latitude" +localisation.getLatitude(), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Longitude" + localisation.getLongitude(), Toast.LENGTH_SHORT).show();*/



        try
        {
            if(Build.VERSION.SDK_INT > 15)
            {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling

                    //ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }
                for (String number : numbers) {
                    SmsManager.getDefault().sendTextMessage(number, null, values[0]+" "+User.getName()+" is in danger ", null, null);

                }

            }
            else {
                for (String number : numbers) {
                    SmsManager.getDefault().sendTextMessage(number, null, values[0]+" "+User.getName()+" is in danger ", null, null);

                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:0631392430"));
        //frederic 0631392430
        //maxime 0606837803
        //alexis 0651232165
        //JD 0782166140
        try
        {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent);
            }
            /*if(Build.VERSION.SDK_INT > 22)
            {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling

                    //ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                context.startActivity(intent);

            }
            else {
                context.startActivity(intent);
            }*/
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }



        /*if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,new String[]{Manifest.permission.CALL_PHONE},10);

            // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //You already have permission
            try {
                context.startActivity(intent);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            System.out.println("pouf");
            context.startActivity(intent);
        }*/


    }

    @Override
    public void onLocationChanged(Location loc) {
        System.out.println("1");
        loc.getLatitude();
        loc.getLongitude();
        System.out.println("Latitude" +loc.getLatitude()+ " Longitude" + loc.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("2");
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("3");
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("4");
    }
}

