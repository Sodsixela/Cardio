package e.administrateur.cardioproject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
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
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static EditTextPreference name;
    private static EditTextPreference password;

    private void createListener() {
        /*
      A preference value change listener that updates the preference's summary
      to reflect its new value.
     */
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences preference, String key) {

            }
        };
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(listener);
    }
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof EditTextPreference){
                if (preference.getKey().equals("name")) {
                    // update the changed gallery name to summary filed

                    // Set up the input
                    data = stringValue;

                            try {
                                if(!data.equals(User.getName()) && !data.equals(""))
                                {
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
                    data =stringValue;
                    try {
                        if(!data.equals(User.getPassword()) && !data.equals(""))
                        {
                            new SettingsActivity.CallServer().execute("Password").get();
                            Toast.makeText(context, "Changed to '"+data+"'",Toast.LENGTH_SHORT).show();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    preference.setSummary(stringValue);
                    password.setText("");
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
            if(User.getName()==null)
            {
                User.setName(savedInstanceState.getString("Username"));
            }

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            password = (EditTextPreference) findPreference("pwd");
            name = (EditTextPreference) findPreference("name");

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
            EditTextPreference num1= (EditTextPreference) findPreference("num1");
            if(num1.getText().toString().equals(" "))
                num1.setText("");
            EditTextPreference num2= (EditTextPreference) findPreference("num2");
            if(num2.getText().toString().equals(" "))
                num2.setText("");
            EditTextPreference num3= (EditTextPreference) findPreference("num3");
            if(num3.getText().toString().equals(" "))
                num3.setText("");
            EditTextPreference doc= (EditTextPreference) findPreference("doc");
            if(doc.getText().toString().equals(" "))
                doc.setText("");
            EditTextPreference em= (EditTextPreference) findPreference("emergency");
            if(em.getText().toString().equals(" "))
                em.setText("");
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
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

    //USE TO SEND MODIFICATION TO THE SERVER

    public static class CallServer extends AsyncTask<String, Void, String> {
        private final String change;
        CallServer(String change,Context c)
        {
            this.change=change;
            context= c;
        }
        CallServer()
        {
            change = null;
        }
        @Override
        protected String doInBackground(String... params) {
            String responseLine = "error";
            try {
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8088);  //connect to server


                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                try {
                    // The capital string before each colon has a special meaning to SMTP
                    // you may want to read the SMTP specification, RFC1822/3
                    String on;
                    if(change==null)
                        on= "{\"type\":\"Modify\",\"dataset\":\""+params[0]+"\",\"value\":\""+data+"\"}";
                    else {
                        on = "{\"type\":\"Modify\",\"dataset\":\"" + params[0] + "\",\"value\":\"" + change + "\"}";
                        data=change;
                    }
                    out.println(on);
                    String message = in.readLine();
                    try {
                        JSONObject jsonObj = new JSONObject( message);
                        if(jsonObj.getString("Change").contains("Changed"))//if it's ok we initialise the data of the user
                        {
                            responseLine=jsonObj.getString("Change");
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor =prefs.edit();
                            if(params[0].equals("Username"))
                            {
                                editor.putString("name",data);
                                User.setName(data);
                            }
                            else
                            {
                                editor.putString("pwd",data);
                                User.setPassword(data);
                            }

                            editor.apply();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        responseLine="Error";
                    }


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
            //if(change!=null)
                //context=getApplicationContext();//if it's from login, need to change the context
            Toast.makeText(context.getApplicationContext(), result,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {


        }
    }
}
