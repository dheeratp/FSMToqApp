package dheera.cs160.berkeley.edu.fsmtoqapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import 	java.util.TimerTask;
import android.util.FloatMath;


import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.Card;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteToqNotification;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.CardImage;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.DeckOfCardsLauncherIcon;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.util.ParcelableUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.List;

import gmail.yuyang226.flickrj.sample.android.FlickrjActivity;


public class ToqActivity extends Activity {

    private final static String PREFS_FILE= "prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";

    static DeckOfCardsManager mDeckOfCardsManager;
    static RemoteDeckOfCards mRemoteDeckOfCards;
    static RemoteResourceStore mRemoteResourceStore;

    private CardImage[] mCardImages;
    private ToqBroadcastReceiver toqReceiver;
    static int counterOfNotifications=0;

    /*Start: For Location*/
    //LocationManager locationManager;
    private String provider;
    Location location;
    private final String PROX_ALERT = "dheera.cs160.berkeley.edu.fsmtoqapp.PROXIMITY_ALERT";
    PendingIntent pIntent1 = null;
    private ProximityReceiver proxReceiver = null;
    double lat;
    double lng;
    HashMap<String,String> fsmLeaderObjects;
    int i=0;

    ListCard listCard;
    SimpleTextCard simpleTextCard;

    Button uninstallbutton;
    Button installbutton;

    /*End: For Location*/


    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    private ToqAppStateBroadcastReceiver toqAppStateReceiver;
    private ViewGroup notificationPanel;
    private ViewGroup deckOfCardsPanel;
    View installDeckOfCardsButton;
    View uninstallDeckOfCardsButton;
    private TextView statusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toq);
        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        toqReceiver = new ToqBroadcastReceiver();
        init();
        setupUI();

    }


    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(proxReceiver);
        //locationManager.removeProximityAlert(pIntent1);
    }


    public void onResume() {


        super.onResume();
       // Toast.makeText(ToqActivity.this, "Resuming", Toast.LENGTH_SHORT).show();
        //locationManager.requestLocationUpdates(provider, 400, 1, this);

    }


    /**
     * @see android.app.Activity#onStart()
     * This is called after onCreate(Bundle) or after onRestart() if the activity has been stopped
     */
    protected void onStart(){
        super.onStart();

        Log.d(Constants.TAG, "ToqApiDemo.onStart");
        // If not connected, try to connect
        if (!mDeckOfCardsManager.isConnected()){
            try{
                mDeckOfCardsManager.connect();
            }
            catch (RemoteDeckOfCardsException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toq, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {

        //START: ADDED FOR THE ERROR OF APP CRASHING - CRAZY
        // Panels
        notificationPanel= (ViewGroup)findViewById(R.id.notification_panel);
        deckOfCardsPanel= (ViewGroup)findViewById(R.id.doc_panel);
        setChildrenEnabled(deckOfCardsPanel, false);
        setChildrenEnabled(notificationPanel, false);


        //END: ADDED FOR THE ERROR OF APP CRASHING - CRAZY


        final Button btnNotification = (Button) findViewById(R.id.send_notif_button);
        btnNotification.setVisibility(View.INVISIBLE);
        uninstallbutton = (Button) findViewById(R.id.uninstall_button);
        installbutton = (Button) findViewById(R.id.install_button);

        findViewById(R.id.send_notif_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendNotification();

            }
        });

        detectLocation();


        findViewById(R.id.install_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                install();
                //detectLocation();
            }
        });

        findViewById(R.id.uninstall_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uninstall();
            }
        });

//        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addSimpleTextCard();
//            }
//        });

//        findViewById(R.id.remove_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                removeDeckOfCards();
//            }
//        });


    }
    public void detectLocation()
    {
           /*START THE PART WHERE YOU DETECT LOCATION*/


        // Acquire a reference to the system Location Manager
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //        Criteria criteria = new Criteria();
        //        provider = locationManager.getBestProvider(criteria, false);
        //
        //        location = locationManager.getLastKnownLocation(provider);

        //setLocationAlert();

        /*END THE PART WHERE YOU DETECT LOCATION*/


        /* start: Other code */

        // Use Location Manager to get location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Location updates listener
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                double startLat = location.getLatitude();
                double startLon = location.getLongitude();
                //Sproul's lat long values
                double endLat = 37.86965;
                double endLon =-122.25914;

                double distance = calc_distance_from_reference(startLat,startLon, endLat,endLon);

                if(distance<=50) {
                    //reached Sproul, send notification
                    Toast.makeText(getApplicationContext(), "You are close to Sproul Plaza!", Toast.LENGTH_SHORT).show();
                     sendNotification();
                     counterOfNotifications++;

                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

        };

        // Register the listener with the Location Manager to receive location updates
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50000, 0, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 0, locationListener);
        }

      /* end: Other code */
    }

    private double calc_distance_from_reference(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pr = (float) (180/3.14169);
        float a1 = (float)lat_a / pr;
        float a2 = (float)lng_a / pr;
        float b1 = (float)lat_b / pr;
        float b2 = (float)lng_b / pr;
        float t1 = FloatMath.cos(a1)*FloatMath.cos(a2)*FloatMath.cos(b1)*FloatMath.cos(b2);
        float t2 = FloatMath.cos(a1)*FloatMath.sin(a2)*FloatMath.cos(b1)*FloatMath.sin(b2);
        float t3 = FloatMath.sin(a1)* FloatMath.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        double ans = (double)(6366000*tt);
        //System.out.println("!!!!!!!!!!!!!!!!!!!GEO ="+(a1+a2+b1+b2+tt));
        //System.out.println("!!!!!!!!!!!!!!!!!!!Distance between("+lat_a+","+lng_a+")" +"and ("+lat_b+","+lng_b+") is "+ans );
        return ans ;
    }






    //End: LocationListener overridden methods



    private void sendNotification() {


        Random generator = new Random();
        List<String> keys      = new ArrayList<String>(fsmLeaderObjects.keySet());
        String       randomKey = keys.get( generator.nextInt(keys.size()) );
        String       value     = fsmLeaderObjects.get(randomKey);

        //Object[] values = fsmLeaderObjects.values().toArray();
        //Object randomValue = values[generator.nextInt(values.length)];
        //System.out.println("randomKey = "+randomKey+ " randomvalue = "+value);


        String[] message = new String[2];
        message[0] = randomKey;
        message[1] = value;

        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                "Free Speech Notification", message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(true);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        // Create a notification with the NotificationTextCard we made
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            //System.out.println("BEFORE FIRING OFF NOTIFICATION!! mDeckOfCardsManager="+mDeckOfCardsManager);
                mDeckOfCardsManager.sendNotification(notification);
                Toast.makeText(ToqActivity.this, "Check your Toq for next steps!!", Toast.LENGTH_SHORT).show();

        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            //System.out.println("EXCEPTION IS "+e);
            //Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Installs applet to Toq watch if app is not yet installed
     */
    private void install() {
        boolean isInstalled = true;


        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        try {
            //System.out.println("***********1*************");
            isInstalled = mDeckOfCardsManager.isInstalled();
            //System.out.println("***********1************* isInstalled= "+isInstalled);


            //START: ADDED FROM init()


            //END: ADDED FROM init()
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            //System.out.println("***********1 Exception *************"+e.getMessage());
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (!isInstalled) {
            try {
                //System.out.println("***********2************* mRemoteDeckOfCards = "+mRemoteDeckOfCards);
                mDeckOfCardsManager.installDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
                //System.out.println("***********2 After*************");

                //System.out.println("***********3 After sendNotification()*************");
            } catch (RemoteDeckOfCardsException e) {
                e.printStackTrace();
                //System.out.println("***********2 Exception*************"+e.getMessage());
                Toast.makeText(this, "Error: Cannot install application", Toast.LENGTH_SHORT).show();
            }
        } else {
            //System.out.println("***********3*************");
            Toast.makeText(this, "App is already installed!", Toast.LENGTH_SHORT).show();
        }

        try{
            //System.out.println("***********4*************");
            storeDeckOfCards();
            //System.out.println("***********4 After storeDeckOfCards()*************");
        }
        catch (Exception e){
            //System.out.println("***********5 Exeption*************"+e.getMessage());
            e.printStackTrace();
        }
        //counterOfNotifications++;
        //detectLocation();

    }

    private void uninstall() {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
            //System.out.println("1.....IN UNINSTALL mDeckOfCardsManager="+mDeckOfCardsManager);

        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (isInstalled) {
            try{
                //System.out.println("2.....IN UNINSTALL mDeckOfCardsManager="+mDeckOfCardsManager);

                mDeckOfCardsManager.uninstallDeckOfCards();
                uninstallbutton.setEnabled(false);
                installbutton.setEnabled(true);
                uninstallbutton.setBackgroundColor(Color.GRAY);
                installbutton.setBackgroundColor(Color.parseColor("#BA533A"));
               // btnNotification.performClick();
                //System.out.println("3.....IN UNINSTALL mDeckOfCardsManager="+mDeckOfCardsManager);

            }
            catch (RemoteDeckOfCardsException e){
                Toast.makeText(this, getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            }
        } else {
            //System.out.println("4.....IN UNINSTALL mDeckOfCardsManager="+mDeckOfCardsManager);

            Toast.makeText(this, getString(R.string.already_uninstalled), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeDeckOfCards() {
        ListCard listCard = mRemoteDeckOfCards.getListCard();
        if (listCard.size() == 0) {
            return;
        }

        listCard.remove(0);

        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards);
            //System.out.println("IN REMOVEDECKOFCARDS mDeckOfCardsManager="+mDeckOfCardsManager);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete Card from ListCard", Toast.LENGTH_SHORT).show();
        }

    }

    // Initialise
    private void init()
    {

        deckOfCardsManagerListener= new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener= new DeckOfCardsEventListenerImpl();
        mDeckOfCardsManager.addDeckOfCardsManagerListener(deckOfCardsManagerListener);
        mDeckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);
        // Create the state receiver
        toqAppStateReceiver= new ToqAppStateBroadcastReceiver();

        fsmLeaderObjects = new HashMap<String, String>();

        fsmLeaderObjects.put("Mario Savio", "Express your own view of free speech in a drawing");
        fsmLeaderObjects.put("Joan Baez","Draw a megaphone");
        fsmLeaderObjects.put("Art Goldberg","Draw Now");
        fsmLeaderObjects.put("Michael Rossman","Draw Free Speech");
        fsmLeaderObjects.put("Jack Weinberg","Draw FSM");
        fsmLeaderObjects.put("Jackie Goldberg","Draw SLATE");

        // START: ADDED BEFORE SUBMISSION

        DeckOfCardsLauncherIcon whiteIcon = null;
        DeckOfCardsLauncherIcon colorIcon = null;
        mRemoteResourceStore= new RemoteResourceStore();

        // Get the launcher icons
        try{
            whiteIcon= new DeckOfCardsLauncherIcon("white.launcher.icon", getBitmap("bw.png"), DeckOfCardsLauncherIcon.WHITE);
            colorIcon= new DeckOfCardsLauncherIcon("color.launcher.icon", getBitmap("color.png"), DeckOfCardsLauncherIcon.COLOR);
        }
        catch (Exception e){
            e.printStackTrace();
            //System.out.println("Can't get launcher icon");
            return;
        }
        mCardImages = new CardImage[6];
        try{
            mCardImages[3]= new CardImage("card.image.1", getBitmap("jack_weinberg_toq.png"));
            mCardImages[2]= new CardImage("card.image.2", getBitmap("joan_baez_toq.png"));
            mCardImages[4]= new CardImage("card.image.3", getBitmap("michael_rossman_toq.png"));
            mCardImages[1]= new CardImage("card.image.4", getBitmap("art_goldberg_toq.png"));
            mCardImages[5]= new CardImage("card.image.5", getBitmap("jackie_goldberg_toq.png"));
            mCardImages[0]= new CardImage("card.image.6", getBitmap("mario_savio_toq.png"));
        }
        catch (Exception e){
            e.printStackTrace();
            //System.out.println("Can't get picture icon");
            // UNCOMMENT LATER return;
        }

        try {
            if ((mRemoteDeckOfCards = getStoredDeckOfCards()) == null) {
                mRemoteDeckOfCards = createDeckOfCards();
                storeDeckOfCards();
            }
        }
        catch (Throwable th){
            th.printStackTrace();
            mRemoteDeckOfCards = null; // Reset to force recreate
        }
        // Make sure in usable state
        if (mRemoteDeckOfCards == null){
            //System.out.println("ABOUT TO CALL createDeckOfCards = "+mRemoteDeckOfCards);
            mRemoteDeckOfCards = createDeckOfCards();
        }
        //System.out.println("%%%%%%%%%%%%%%%% mRemoteResourceStore = "+mRemoteResourceStore);
        // Set the custom launcher icons, adding them to the resource store
        mRemoteDeckOfCards.setLauncherIcons(mRemoteResourceStore, new DeckOfCardsLauncherIcon[]{whiteIcon, colorIcon});


        //System.out.println("@@@@@@@@@@@@@@@@%%%%%%%%%%%%%%%% mRemoteResourceStore = "+mRemoteResourceStore);

        // Re-populate the resource store with any card images being used by any of the cards
        int i = 0;
        for (Iterator<Card> it = mRemoteDeckOfCards.getListCard().iterator(); it.hasNext(); ) {

            String cardImageId = ((SimpleTextCard) it.next()).getCardImageId();
            if ((cardImageId != null) && !mRemoteResourceStore.containsId(cardImageId)) {
                mRemoteResourceStore.addResource(mCardImages[i]);
                i++;
            }
        }

        //END: ADDED BEFORE SUBMISSION


        // Create the resource store for icons and images


        installDeckOfCardsButton = findViewById(R.id.install_button);
        uninstallDeckOfCardsButton = findViewById(R.id.uninstall_button);

        // Register toq app state receiver

        // Status
        statusTextView= (TextView)findViewById(R.id.status_text);
        //statusTextView.setText("Initialised");

        registerToqAppStateReceiver();

        // If not connected, try to connect
        if (!mDeckOfCardsManager.isConnected()){

            //setStatus(getString(R.string.status_connecting));

            Log.d(Constants.TAG, "ToqApiDemo.onStart - not connected, connecting...");

            try{
                mDeckOfCardsManager.connect();
            }
            catch (RemoteDeckOfCardsException e){
                Toast.makeText(this, getString(R.string.error_connecting_to_service), Toast.LENGTH_SHORT).show();
                Log.e(Constants.TAG, "ToqApiDemo.onStart - error connecting to Toq app service", e);
            }

        }
        else{
            Log.d(Constants.TAG, "ToqApiDemo.onStart - already connected");
            setStatus(getString(R.string.status_connected));
            refreshUI();
        }



    }

    // Register state receiver
    private void registerToqAppStateReceiver(){
        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction(Constants.BLUETOOTH_ENABLED_INTENT);
        intentFilter.addAction(Constants.BLUETOOTH_DISABLED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_PAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_UNPAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_CONNECTED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_DISCONNECTED_INTENT);
        getApplicationContext().registerReceiver(toqAppStateReceiver, intentFilter);
    }



    // Handle card events triggered by the user interacting with a card in the installed deck of cards
    private class DeckOfCardsEventListenerImpl implements DeckOfCardsEventListener {

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardOpen(java.lang.String)
         */
        public void onCardOpen(final String cardId){
            runOnUiThread(new Runnable(){
                public void run(){
                    //Toast.makeText(ToqActivity.this, getString(R.string.event_card_open) + cardId, Toast.LENGTH_SHORT).show();

                    //System.out.println("&&&&&&&&&&&&&&&&&&&&& In onCardOpen &&&&&&&&&&&&&&&&&&&&&&"+mRemoteDeckOfCards);

                    Intent intent = new Intent(getApplicationContext(), FSMToqActivity.class);

                    startActivity(intent);


                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardVisible(java.lang.String)
         */
        public void onCardVisible(final String cardId){
            runOnUiThread(new Runnable(){
                public void run(){
                    //Toast.makeText(ToqActivity.this, getString(R.string.event_card_visible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardInvisible(java.lang.String)
         */
        public void onCardInvisible(final String cardId){
            runOnUiThread(new Runnable(){
                public void run(){
                   // Toast.makeText(ToqActivity.this, getString(R.string.event_card_invisible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardClosed(java.lang.String)
         */
        public void onCardClosed(final String cardId){
            runOnUiThread(new Runnable(){
                public void run(){
                    //Toast.makeText(ToqActivity.this, getString(R.string.event_card_closed) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption){
            runOnUiThread(new Runnable(){
                public void run(){
                    //Toast.makeText(ToqActivity.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption + "]", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption, final String quickReplyOption){
            runOnUiThread(new Runnable(){
                public void run(){
                   // Toast.makeText(ToqActivity.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption + ":" + quickReplyOption +  "]", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    // Read an image from assets and return as a bitmap
    private Bitmap getBitmap(String fileName) throws Exception{

        try{
            //System.out.println("Filename ="+fileName);
            InputStream is= getAssets().open(fileName);
            //System.out.println("Input Stream ="+is);
            return BitmapFactory.decodeStream(is);
        }
        catch (Exception e){
            //System.out.println("EXCEPTION in getBitmap="+e.getMessage());
            throw new Exception("An error occurred getting the bitmap: " + fileName, e);
        }
    }

    private RemoteDeckOfCards getStoredDeckOfCards() throws Exception{

        if (!isValidDeckOfCards()){
            Log.w(Constants.TAG, "Stored deck of cards not valid for this version of the demo, recreating...");
            return null;
        }

        SharedPreferences prefs= getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        String deckOfCardsStr= prefs.getString(DECK_OF_CARDS_KEY, null);

        if (deckOfCardsStr == null){
            return null;
        }
        else{
            return ParcelableUtil.unmarshall(deckOfCardsStr, RemoteDeckOfCards.CREATOR);
        }

    }

    /**
     * Uses SharedPreferences to store the deck of cards
     * This is mainly used to
     */
    private void storeDeckOfCards() throws Exception{
        //System.out.println("In storeDeckOfCards ");
        // Retrieve and hold the contents of PREFS_FILE, or create one when you retrieve an editor (SharedPreferences.edit())
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Create new editor with preferences above
        SharedPreferences.Editor editor = prefs.edit();
        // Store an encoded string of the deck of cards with key DECK_OF_CARDS_KEY
        editor.putString(DECK_OF_CARDS_KEY, ParcelableUtil.marshall(mRemoteDeckOfCards));
        // Store the version code with key DECK_OF_CARDS_VERSION_KEY
        editor.putInt(DECK_OF_CARDS_VERSION_KEY, Constants.VERSION_CODE);
        // Commit these changes
        editor.commit();
        //System.out.println("In storeDeckOfCards After commit");
    }

    // Check if the stored deck of cards is valid for this version of the demo
    private boolean isValidDeckOfCards(){

        SharedPreferences prefs= getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Return 0 if DECK_OF_CARDS_VERSION_KEY isn't found
        int deckOfCardsVersion= prefs.getInt(DECK_OF_CARDS_VERSION_KEY, 0);

        return deckOfCardsVersion >= Constants.VERSION_CODE;
    }

    // Create some cards with example content
    private RemoteDeckOfCards createDeckOfCards(){

        listCard =  new ListCard();
        for (Map.Entry<String,String> entry : fsmLeaderObjects.entrySet()) {


            String key = entry.getKey();
            String value = entry.getValue();
            //System.out.println("IN FOR LOOP...key = "+key+" value = "+value);
            //FOR SIMPLE TEXT CARD FOR EACH PERSON

            simpleTextCard = new SimpleTextCard(Integer.toString(i));
            try {

                 mRemoteResourceStore.addResource(mCardImages[i]);

                 simpleTextCard.setCardImage(mRemoteResourceStore,mCardImages[i]);

            }
            catch (Exception e){
                e.printStackTrace();
                //System.out.println("Can't get picture icon");
                // UNCOMMENT LATER return;
            }
            simpleTextCard.setHeaderText(key);
            simpleTextCard.setTitleText(value);
            simpleTextCard.setReceivingEvents(true);
            simpleTextCard.setShowDivider(true);

            listCard.add(simpleTextCard);

            i++;
        }
        mRemoteDeckOfCards = new RemoteDeckOfCards(this, listCard);

        try {
            //System.out.println("***BEFORE UPDATEDECKOFCARDS***mDeckOfCardsManager="+mDeckOfCardsManager);
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards,mRemoteResourceStore);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
           // Toast.makeText(this, "Failed to Create SimpleTextCard", Toast.LENGTH_SHORT).show();
        }
        return mRemoteDeckOfCards;
    }

    // Toq app state receiver
    private class ToqAppStateBroadcastReceiver extends BroadcastReceiver{

        /**
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        public void onReceive(Context context, Intent intent){

            String action= intent.getAction();

            if (action == null){
                Log.w(Constants.TAG, "ToqApiDemo.ToqAppStateBroadcastReceiver.onReceive - action is null, returning");
                return;
            }

            Log.d(Constants.TAG, "ToqApiDemo.ToqAppStateBroadcastReceiver.onReceive - action: " + action);

            // If watch is now connected, refresh UI
            if (action.equals(Constants.TOQ_WATCH_CONNECTED_INTENT)){
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_connected), Toast.LENGTH_SHORT).show();
                refreshUI();
            }
            // Else if watch is now disconnected, disable UI
            else if (action.equals(Constants.TOQ_WATCH_DISCONNECTED_INTENT)){
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
                disableUI();
            }

        }

    }

    // Connected to Toq app service, so refresh the UI
    private void refreshUI(){

        try{

            // If Toq watch is connected
            if (mDeckOfCardsManager.isToqWatchConnected()){

                // If the deck of cards applet is already installed
                if (mDeckOfCardsManager.isInstalled()){
                    installbutton.setBackgroundColor(Color.GRAY);
                    installbutton.setEnabled(false);
                    uninstallbutton.setEnabled(true);
                    Log.d(Constants.TAG, "ToqApiDemo.refreshUI - already installed");
                    updateUIInstalled();
                }
                // Else not installed
                else{
                    Log.d(Constants.TAG, "ToqApiDemo.refreshUI - not installed");
                    updateUINotInstalled();
                }

            }
            // Else not connected to the Toq app
            else{
                Log.d(Constants.TAG, "ToqApiDemo.refreshUI - Toq watch is disconnected");
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
                disableUI();
            }

        }
        catch (RemoteDeckOfCardsException e){
            Toast.makeText(this, getString(R.string.error_checking_status), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.refreshUI - error checking if Toq watch is connected or deck of cards is installed", e);
        }

    }

    // Disable all UI components
    private void disableUI(){
        // Disable everything
        setChildrenEnabled(deckOfCardsPanel, false);
        setChildrenEnabled(notificationPanel, false);
    }

    // Enable/Disable a view group's children and nested children
    private void setChildrenEnabled(ViewGroup viewGroup, boolean isEnabled){

        for (int i = 0; i < viewGroup.getChildCount();  i++){

            View view= viewGroup.getChildAt(i);

            if (view instanceof ViewGroup){
                setChildrenEnabled((ViewGroup)view, isEnabled);
            }
            else{
                view.setEnabled(isEnabled);
            }

        }

    }

    // Set up UI for when deck of cards applet is already installed
    private void updateUIInstalled(){

        // Panels
       // notificationPanel= (ViewGroup)findViewById(R.id.notification_panel);
       // deckOfCardsPanel= (ViewGroup)findViewById(R.id.doc_panel);

        // Enable everything
        setChildrenEnabled(deckOfCardsPanel, true);
        setChildrenEnabled(notificationPanel, true);

        // Install disabled; update, uninstall enabled
        installDeckOfCardsButton.setEnabled(false);

        uninstallDeckOfCardsButton.setEnabled(true);


    }

    // Set up UI for when deck of cards applet is not installed
    private void updateUINotInstalled(){

        // Disable notification panel
        setChildrenEnabled(notificationPanel, false);

        // Enable deck of cards panel
        setChildrenEnabled(deckOfCardsPanel, true);

        uninstallbutton.setBackgroundColor(Color.GRAY);
        uninstallbutton.setEnabled(false);
        installbutton.setEnabled(true);

        // Install enabled; update, uninstall disabled
        installDeckOfCardsButton.setEnabled(true);

        uninstallDeckOfCardsButton.setEnabled(false);

        // Focus
        installDeckOfCardsButton.requestFocus();
        //setupUI();
    }




    // Handle service connection lifecycle and installation events
    private class DeckOfCardsManagerListenerImpl implements DeckOfCardsManagerListener{

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onConnected()
         */
        public void onConnected(){
            runOnUiThread(new Runnable(){
                public void run(){
                    //setStatus(getString(R.string.status_connected));
                    refreshUI();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onDisconnected()
         */
        public void onDisconnected(){
            runOnUiThread(new Runnable(){
                public void run(){
                    setStatus(getString(R.string.status_disconnected));
                    disableUI();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationSuccessful()
         */
        public void onInstallationSuccessful(){
            runOnUiThread(new Runnable(){
                public void run(){

                    //setStatus(getString(R.string.status_installation_successful));
                    updateUIInstalled();
                    uninstallbutton.setEnabled(true);
                    installbutton.setEnabled(false);
                    installbutton.setBackgroundColor(Color.GRAY);
                    uninstallbutton.setBackgroundColor(Color.parseColor("#BA533A"));

                    Toast.makeText(ToqActivity.this, R.string.status_installation_successful, Toast.LENGTH_SHORT).show();


                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onInstallationDenied()
         */
        public void onInstallationDenied(){
            runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(ToqActivity.this, R.string.status_installation_denied, Toast.LENGTH_SHORT).show();

                   // setStatus(getString(R.string.status_installation_denied));
                    updateUINotInstalled();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener#onUninstalled()
         */
        public void onUninstalled(){
            runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(ToqActivity.this, R.string.status_uninstalled, Toast.LENGTH_SHORT).show();

                   // setStatus(getString(R.string.status_uninstalled));
                    uninstallbutton.setEnabled(false);
                    uninstallbutton.setBackgroundColor(Color.GRAY);
                    installbutton.setEnabled(true);
                    installbutton.setBackgroundColor(Color.parseColor("#BA533A"));
                    updateUINotInstalled();
                }
            });
        }

    }


    // Set status bar message
    private void setStatus(String msg){
        statusTextView.setText(msg);
    }


}
class ProximityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent intent) {
        if(intent.getData() != null)
            Log.v("", intent.getData().toString());
        Bundle extras = intent.getExtras();
        if(extras != null) {
            Log.v("", "Message: " + extras.getString("message"));
            Log.v("", "Entering? " + extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING));
        }
    }
}
