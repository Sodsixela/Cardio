package cardio.cardio;

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
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

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
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    public Alert() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate() {
        Intent notificationIntent = new Intent(this, Alert.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getString(R.string.notifTitle))
                        .setSmallIcon(R.mipmap.clogo_round)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(110, notification);
        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();*/

        //test making the application alive even phone locked
        /*pm = (PowerManager) getApplicationContext().getSystemService(
                getApplicationContext().POWER_SERVICE);*/

        getLocation();//to have at least one location
        /*Intent i = new Intent(getApplicationContext(), Alert.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),3333,i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 60);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,cal.getTimeInMillis(), pi);*/
        new AlertCall().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//starting the thread
    }
    public void onDestroy()
    {
        wl.release();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    private String getLocation()
    {
        String addressText="";
        Location localisation = null;
        System.out.println("loc: "+localisation);
        LocationManager locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        ///we huse NETWORK but can work with gps

        //locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new myListener(), null);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1000, new myListener());
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1000, new myListener());
        localisation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        System.out.println("loc: "+localisation);
        if(localisation!=null)//if it's null, then there is a problem or localisation isn't activated on the phone
        {
            System.out.println(localisation.getLatitude());
            //localisation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //System.out.println("loc: "+localisation);
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());//to get the address from location
            List<Address> address = null;
            try {
                address = geocoder.getFromLocation(localisation.getLatitude(), localisation.getLongitude(), 1);
                addressText = getString(R.string.place)+" "+String.format("%s, %s, %s",
                        //address.get(0).getMaxAddressLineIndex() > 0 ? address.get(0).getAddressLine(0) : "",
                        address.get(0).getThoroughfare(),
                        address.get(0).getLocality(),
                        address.get(0).getCountryName());
                System.out.println(addressText);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addressText+="\n"+getString(R.string.Long)+": "+localisation.getLatitude()+"\n"+getString(R.string.Lat)+": "+localisation.getLongitude();
        }

        return addressText;
    }
    private class myListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("main Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
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

    private class AlertCall extends AsyncTask<Void, String, String>{
        DatagramSocket socket;


        @Override
        protected String doInBackground(Void... params) {

            String responseLine = null;
            Looper.prepare();
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            try {
                System.out.println("alert ready");
                socket = new DatagramSocket(8000, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                int i=0;
                while (true) {
                    //Receive a packet
                    byte[] recvBuf = new byte[1500];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    /*WifiManager wifi;
                    wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiManager.MulticastLock wifiLock = wifi.createMulticastLock("wifiOn");
                    wifiLock.acquire();*/
                    WifiManager wMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiManager.WifiLock wifiLock = wMgr.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
                    wifiLock.acquire();
                    pm=(PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
                    wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "alertOff");
                    wl.acquire();
                    socket.receive(packet);
                    //Packet received
                    System.out.println("Packet received, data: " + new String(packet.getData()).trim());
                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    //We will send here danger message
                    if (message.equals("ALERT")) {
                       onProgressUpdate("ALERT");

                    }
                    else if(message.equals("EMERGENCY"))
                    {
                        onProgressUpdate("EMERGENCY");
                    }
                    wl.release();
                    wifiLock.release();
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
            Context context=getBaseContext();
            String addressText="";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);//to get settings like if gps is authorized by the user
            ArrayList<String> numbers= new ArrayList<>();

            if(values[0].equals("ALERT"))
            {
                values[0]=getString(R.string.alert);
                for(int i=1;i<=3;i++)//we alert close friends number
                {
                    if(!prefs.getString("num"+i,"").equals(""))
                        numbers.add(prefs.getString("num"+i,""));
                }
            }
            else if (values[0].equals("EMERGENCY"))//we alert emergency, doctors
            {
                values[0]=getString(R.string.emergency);
                if(!prefs.getString("doc","").equals(""))
                    numbers.add(prefs.getString("doc",""));
                if(!prefs.getString("emergency","").equals(""))
                    numbers.add(prefs.getString("emergency",""));
            }
            if(prefs.getBoolean("gps",false)) {//if gps authorized

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    addressText=getLocation();
                }

                //locationManager = (LocationManager) User.getC().getSystemService(Context.LOCATION_SERVICE);
                //Location localisation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }
            System.out.println("gps "+prefs.getBoolean("gps",false));

               /* System.out.println("Latitude" +localisation.getLatitude()+ " Longitude" + localisation.getLongitude());
                Toast.makeText(context, "Latitude" +localisation.getLatitude(), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Longitude" + localisation.getLongitude(), Toast.LENGTH_SHORT).show();*/
            try
            {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    for (String number : numbers) {//we send the message to each numbers
                        SmsManager.getDefault().sendTextMessage(number, null, values[0]+"\n"+User.getName()+" "+getString(R.string.danger)+" "+addressText, null, null);

                    }
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            //FOR TEST
            //Should be deleted for the final version

            if(prefs.getBoolean("call",false)) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:0631392430"));
                //frederic 0631392430
                //maxime 0606837803
                //alexis 0651232165
                //JD 0782166140
                try {

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        context.startActivity(intent);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
