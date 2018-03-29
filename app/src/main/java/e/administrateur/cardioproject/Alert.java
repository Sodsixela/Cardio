package e.administrateur.cardioproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Alert extends Service {
    private AsyncTask alert;
    public Alert() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate() {
        //create notification
        Intent notificationIntent = new Intent(this, Alert.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getString(R.string.notifTitle))
                        .setSmallIcon(R.mipmap.logo)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(110, notification);
        getLocation();//to have at least one location
        alert= new AlertCall().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//starting the thread
    }
    public void onDestroy()
    {
        alert.cancel(true);//we stop the thread too
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    //we already got the permission
    @SuppressLint("MissingPermission")
    private String getLocation()
    {
        String addressText="";
        Location localisation;
        LocationManager locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        ///we huse NETWORK but can work with gps

        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1000, new myListener());
        }
        localisation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(localisation!=null)//if it's null, then there is a problem or localisation isn't activated on the phone
        {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());//to get the address from location
            List<Address> address;
            try {
                address = geocoder.getFromLocation(localisation.getLatitude(), localisation.getLongitude(), 1);
                addressText = getString(R.string.place)+" "+String.format("%s, %s, %s",
                        address.get(0).getThoroughfare(),
                        address.get(0).getLocality(),
                        address.get(0).getCountryName());
                System.out.println(addressText);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*we still keep the longitutude lattitude*/
            addressText+="\n"+getString(R.string.Long)+": "+localisation.getLatitude()+"\n"+getString(R.string.Lat)+": "+localisation.getLongitude();
        }

        return addressText;
    }
    private class myListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AlertCall extends AsyncTask<Void, String, String>{
        DatagramSocket socket;


        @Override
        protected String doInBackground(Void... params) {

            Looper.prepare();
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            try {
                socket = new DatagramSocket(8000, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                socket.setSoTimeout(5000);//we set a time out to check if the thread is canceled
                byte[] recvBuf = new byte[1500];
                while (!isCancelled()) {//if service still exist
                    //Receive a packet
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        try{
                            socket.receive(packet);
                            if(packet.getData()!=null)
                            {
                                //Packet received
                                //See if the packet holds the right command (message)
                                String message = new String(packet.getData()).trim();
                                //We will send here danger message
                                if (message.equals("ALERT")) {
                                    onProgressUpdate("ALERT");

                                } else if (message.equals("EMERGENCY")) {
                                    onProgressUpdate("EMERGENCY");
                                }

                            }
                        } catch (IOException e) {

                        }

                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Looper.loop();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(String... values) {//We have catched an alert
            Context context=getBaseContext();
            String addressText="";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);//to get settings like if gps is authorized by the user
            ArrayList<String> numbers= new ArrayList<>();

            if(values[0].equals("ALERT"))
            {
                values[0]=getString(R.string.alert);
                for(int i=1;i<=3;i++)//we alert close friends number
                {
                    if(!prefs.getString("num"+i,"").equals(" "))
                        numbers.add(prefs.getString("num"+i,""));
                }
            }
            else if (values[0].equals("EMERGENCY"))//we alert emergency, doctors
            {
                values[0]=getString(R.string.emergency);
                if(!prefs.getString("doc","").equals(" "))
                    numbers.add(prefs.getString("doc","") );
                if(!prefs.getString("emergency","").equals(" "))
                    numbers.add(prefs.getString("emergency",""));
            }
            if(prefs.getBoolean("gps",false)) {//if gps authorized

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    addressText=getLocation();//we take the location  if we can of course
                }
            }
            try
            {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    for (String number : numbers) {//we send the message to each numbers
                        SmsManager.getDefault().sendTextMessage(number, null, values[0]+"\n"+User.getName()+" "+getString(R.string.danger)+" "+addressText, null, null);
                        System.out.print(number);
                    }
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }
}
