package cardio.cardio;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.EditText;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static String data;
    private static Context context;
    private static EditTextPreference name;
    private static EditTextPreference password;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private String value;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private void createListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences preference, String key) {
                System.out
                        .print('1');
                //String value = preference.getString("name", "NULL");
                   /* if (preference.equals("name")) {
                        // update the changed gallery name to summary filed

// Set up the input
                        //final EditText input = new EditText(this);
                        //final EditTextPreference input_name = (EditTextPreference) findPreference("name");
                        //SettingsActivity set = new SettingsActivity();

                        data = name.getText().toString();
                        System.out.println("data: "+data);
                        try {
                            if(!data.equals(User.getName()))
                                new SettingsActivity.CallServer().execute("Username").get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                }
                else if (preference.equals("pwd")) {
                    // update the changed gallery name to summary filed

// Set up the input
                    //final EditText input = new EditText(this);
                    //final EditTextPreference input_name = (EditTextPreference) findPreference("name");
                    //SettingsActivity set = new SettingsActivity();

                    data = set.getName();password.getText().toString();
                    System.out.println(data);
                    try {
                        if(!data.equals(User.getPassword()))
                            new SettingsActivity.CallServer().execute("Password").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }*/
            }
        };
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(listener);
    }
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }else if (preference instanceof EditTextPreference){
                if (preference.getKey().equals("name")) {
                    // update the changed gallery name to summary filed

// Set up the input
                    //final EditText input = new EditText(this);
                    //final EditTextPreference input_name = (EditTextPreference) findPreference("name");
                    SettingsActivity set = new SettingsActivity();

                    data = /*set.getName();*/set.name.getText().toString();

                            try {
                                if(!data.equals(User.getName()))
                                {System.out.println("name: "+data);
                                    new SettingsActivity.CallServer().execute("Username").get();
                                    name.setText(data);
                                    Toast.makeText(context, "Changed to '"+data+"'",Toast.LENGTH_SHORT).show();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                    preference.setSummary(stringValue);
                }
                else if (preference.getKey().equals("pwd")) {
                    // update the changed gallery name to summary filed
                    SettingsActivity set = new SettingsActivity();
                    data = /*set.getName();*/set.password.getText().toString();
                    System.out.println(data);
                    try {
                        if(!data.equals(User.getPassword()) && !data.equals(""))
                        {
                            System.out.println("password: "+data);
                            new SettingsActivity.CallServer().execute("Password").get();
                            password.setText("");
                            Toast.makeText(set.context, "Changed to '"+data+"'",Toast.LENGTH_SHORT).show();

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    preference.setSummary(stringValue);
                }
            }
            else if (preference.getKey().equals("num1")) {
                preference.setSummary(stringValue);
            }
            else if (preference.getKey().equals("num2")) {
                preference.setSummary(stringValue);
            }
            else if (preference.getKey().equals("num3")) {
                preference.setSummary(stringValue);
            }
            else if (preference.getKey().equals("doc")) {
                preference.setSummary(stringValue);
            }
            else if (preference.getKey().equals("emergency")) {
                preference.setSummary(stringValue);
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                System.out.println("OKOKOKOKOK");
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createListener();
        setupActionBar();
        context= getApplicationContext();
        //TextView name= (TextView) findViewById(R.id.chgeName);
        //name.setText(User.getName());
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            SwitchPreference call = (SwitchPreference) findPreference("call");
            SwitchPreference sms = (SwitchPreference) findPreference("sms");
            SwitchPreference gps = (SwitchPreference) findPreference("gps");

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                call.setChecked(true);
            }
            else
            {
                call.setChecked(false);
            }

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sms.setChecked(true);
            }
            else
            {
                gps.setChecked(false);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                call.setChecked(true);
            }
            else
            {
                call.setChecked(false);
            }
            password = (EditTextPreference) findPreference("pwd");
            name = (EditTextPreference) findPreference("name");
            /*name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(
                        android.preference.Preference preference, Object newValue) {
                    System.out.print("hey");
                    data = name.getText().toString();//.input_name.getText().toString();
                    System.out.println(data);
                    try {
                        new CallServer().execute("Username").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });*/
            /*SettingsActivity set = new SettingsActivity();

            data = set.getName();//.input_name.getText().toString();
            System.out.println(data);
            try {
                new SettingsActivity.CallServer().execute("Username").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/
            name.setText(User.getName());
            password.setText("");
            bindPreferenceSummaryToValue(findPreference("name"));
            bindPreferenceSummaryToValue(findPreference("pwd"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private static class CallServer extends AsyncTask<String, Void, String> {

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

                        String on= "{\"type\":\"Modify\",\"dataset\":\""+params[0]+"\",\"value\":\""+data+"\"}";
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
            Toast.makeText(context, result,Toast.LENGTH_SHORT).show();
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
