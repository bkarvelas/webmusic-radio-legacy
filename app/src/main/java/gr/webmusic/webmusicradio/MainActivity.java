package gr.webmusic.webmusicradio;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gr.webmusic.webmusicradio.adapter.CustomListAdapter;
import gr.webmusic.webmusicradio.app.AppController;
import gr.webmusic.webmusicradio.lib.SimpleXmlRequest;
import gr.webmusic.webmusicradio.pojo.Item;


public class MainActivity extends Toolbar {

    // json data url
//    private static final String XML_URL = "https://ajax.googleapis.com/ajax/services/feed/load?v=2.0&q=http://webmusic.gr/?feed=rss2&num=";

    // WebMusic rss url
    private static final String XML_URL = "http://webmusic.gr/?feed=rss2";
    // STREAM URL
    private final String STREAM_URL = "http://82.145.43.92:8202";

    private ProgressDialog pDialog = new ProgressDialog(this);
//    private List<WebsiteArticleList> postsList = new ArrayList<WebsiteArticleList>();
    private List<Item> postsList = new ArrayList<>();
    private ListView listView;
    private CustomListAdapter adapter;
    private MediaPlayer mediaPlayer;
    private ImageButton btnPlay;

    // DEBUG TAGs
    private final String DEBUG_TAG_GENERAL = "Debug_TAG";
    private final String DEBUG_TAG_NET = "NetworkStatus";
    private final String DEBUG_TAG_PHONE_RINGING = "PhoneState";

    private boolean isConnected, isConnectivity, isPlayBtnPressed = false, onlyWiFiCheckBox;

    private BroadcastReceiver broadcastReceiver;
    private ConnectivityManager cm;
    private PhoneStateListener phoneStateListener;
    private NetworkInfo mobile;
    private boolean mobileNetwork;
    private String reqTAG = "request";

    public MainActivity(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Debug
        Log.d(DEBUG_TAG_GENERAL, "onCreate RUN");

        // initialize mediaPlayer
        mediaPlayerInit();

        // initialize phoneStateListenerMethod
        phoneStateListenerMethod();

        // initialize wifiBroadcastReceiver to listen for phone state changes and internet connection
        // changes
        wifiBroadcastReceiver();

        // Check if WiFi and Mobile Internet are available
        checkNtwConnectivityState();

        // initialize the btnPlay and set onClickListener
        btnPlay = (ImageButton) findViewById(R.id.button_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton(v);
            }
        });

        xmlParse();
    }

    private void xmlParse() {
        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, postsList);
        listView.setAdapter(adapter);

        pDialog = new ProgressDialog(this);

        // Showing progress dialog before making http request
        pDialog.setMessage(getString(R.string.loading));
        pDialog.show();

        SimpleXmlRequest<Item> simpleXmlRequest = new SimpleXmlRequest<>(Request.Method.GET,1, XML_URL, Item.class,

                new Response.Listener<Item>() {
                    @Override
                    public void onResponse(Item response) {
                        Log.d("Ontws travame response", response.toString());
                        hidePDialog();

//                        response.getMatches();
//                        String test = response.getTitle();
//                        Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error Object
                    }
                }
        );
        AppController.getInstance().addToRequestQueue(simpleXmlRequest, reqTAG);
        Log.d(DEBUG_TAG_GENERAL, "ti ginete gamwto");
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log.d(DEBUG_TAG_GENERAL, "onResume RUN");

        // load settings
        load();

        if (isPlayBtnPressed) {
            if (onlyWiFiCheckBox && mobileNetwork) {

                // changes the play button's image to play.png
                btnPlay.setImageResource(R.drawable.play);

                // stops the music - stream and resets the state of the mediaPlayer
                mediaPlayer.stop();
                mediaPlayer.reset();

                // prints "Please check your internet connection" on device's screen
                Toast.makeText(MainActivity.this, getString(R.string.check_connection), Toast.LENGTH_LONG).show();
                // Log.d("ToastRunning", "Toast is running");

                isPlayBtnPressed = false;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG_GENERAL, "onStart RUN");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG_GENERAL, "onPause RUN");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG_GENERAL, "onStop RUN");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG_GENERAL, "onDestroy RUN");

        // Music stops playing and resets media player to be ready next time will run the app
        mediaPlayer.release();

        //unregister our receiver
        this.unregisterReceiver(this.broadcastReceiver);

        //unregister our phoneStateListener
        TelephonyManager tMngr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (tMngr != null) {
            tMngr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        hidePDialog();
    }

    public void playButton(View v) {

        // check network connectivity state
        checkNtwConnectivityState();

        if (!mediaPlayer.isPlaying()) {

            if (!isConnected || (onlyWiFiCheckBox && mobileNetwork)) {

                // prints "Please check your internet connection" on device's screen
                Toast.makeText(MainActivity.this, getString(R.string.check_connection), Toast.LENGTH_LONG).show();

                // changes the play button's image to play.png
                btnPlay.setImageResource(R.drawable.play);

                // stops the music - stream and resets the state of the mediaPlayer
                mediaPlayer.stop();
                mediaPlayer.reset();

                isPlayBtnPressed = false;
            } else {
                // prints "Loading..." on device's screen
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();

                // Plays - streams the music
                playStream();
                isPlayBtnPressed = true;

                // changes the button image to pause.png
                btnPlay.setImageResource(R.drawable.pause);
            }

        } else {
            // stops the music - stream and resets the state of the mediaPlayer
            mediaPlayer.stop();
            mediaPlayer.reset();

            // changes the button image to play.png
            btnPlay.setImageResource(R.drawable.play);

            isPlayBtnPressed = false;
        }
    }

    protected void checkNtwConnectivityState() {
        try {
            cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (mobile == null) {
                mobileNetwork = false;
            } else {
                mobileNetwork = true;
            }

            if (wifi.isAvailable()) {
                Log.d(DEBUG_TAG_NET, "WiFi = Connected");
            } else {
                Log.d(DEBUG_TAG_NET, "WiFi = Disconnected");
            }

            if (mobileNetwork) {
                Log.d(DEBUG_TAG_NET, "mobile = Connected");

            } else {
                Log.d(DEBUG_TAG_NET, "mobile = Disconnected");

            }

            Log.d(DEBUG_TAG_NET, "mobile is available: " + mobileNetwork);
            Log.d(DEBUG_TAG_NET, "Network Connected = " + isConnected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void mediaPlayerInit() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playStream() {
        try {
            // sets the streaming source and prepares the mediaPlayer to stream the music
            mediaPlayer.setDataSource(STREAM_URL);
            mediaPlayer.prepareAsync();

        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.mediaplayer_data_source_error_msg), Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.mediaplayer_data_source_error_msg), Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.mediaplayer_data_source_error_msg), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wifiBroadcastReceiver() {
        // set a broadcast receiver to check-filter when the internet connection has been lost or
        // connected
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    Log.d("NetworkCheckReceiver", "NetworkCheckReceiver invoked...");

                    // check wifi and mobile connection
                    checkNtwConnectivityState();

                    isConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                    if (!isConnectivity) {

                        if (isPlayBtnPressed) {
                            Log.d("NetworkCheckReceiver", "connected");

                            // prints "Loading..." on device's screen
                            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();

                            // Plays - streams the music
                            playStream();

                            // changes the button image to pause.png
                            btnPlay.setImageResource(R.drawable.pause);
                        }
                    } else {
                        Log.d("NetworkCheckReceiver", "disconnected");

                        // prints "Please check your internet connection" on device's screen
                        Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

                        // set play button's image to play.png
                        btnPlay.setImageResource(R.drawable.play);

                        // stops the music - stream and resets the state of the mediaPlayer
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                }

            }
        };

        // initialize BroadcastReceiver
        registerReceiver(broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private void phoneStateListenerMethod() {

        // sets a phoneStateListener to read the call states of the phone
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(DEBUG_TAG_PHONE_RINGING, "Ringing");

                        // Incoming call: Pause Streaming
                        if (mediaPlayer.isPlaying()) {

                            // stops the music - stream and resets the state of the mediaPlayer
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(DEBUG_TAG_PHONE_RINGING, "Idle");

                        //Not in call: Play Stream
                        if (isPlayBtnPressed) {
                            if (!mediaPlayer.isPlaying()) {

                                // Plays - streams the music
                                playStream();
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d(DEBUG_TAG_PHONE_RINGING, "Dialing or on hold");

                        // a call is dialing, active of on hold
                        if (mediaPlayer.isPlaying()) {

                            // stops the music - stream and resets the state of the mediaPlayer
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                        break;
                    default:
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager tMngr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (tMngr != null) {
            tMngr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void load() {
        Context mContext = getApplicationContext();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.web_music_preferences), Context.MODE_PRIVATE);
        onlyWiFiCheckBox = sharedPreferences.getBoolean("wifi_check_box", false);

        Log.d("onlyWIFI", "onlyWiFiCheckBox: " + onlyWiFiCheckBox);
    }

}





