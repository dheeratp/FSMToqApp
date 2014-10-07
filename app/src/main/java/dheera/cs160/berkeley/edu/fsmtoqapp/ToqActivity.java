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
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.Card;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
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


public class ToqActivity extends Activity implements LocationListener {

    private final static String PREFS_FILE= "prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";

    private DeckOfCardsManager mDeckOfCardsManager;
    private RemoteDeckOfCards mRemoteDeckOfCards;
    private RemoteResourceStore mRemoteResourceStore;
    private CardImage[] mCardImages;
    private ToqBroadcastReceiver toqReceiver;

    /*Start: For Location*/
    LocationManager locationManager;
    private String provider;
    Location location;
    private final String PROX_ALERT = "dheera.cs160.berkeley.edu.fsmtoqapp.PROXIMITY_ALERT";
    PendingIntent pIntent1 = null;
    private ProximityReceiver proxReceiver = null;
    double lat;
    double lng;
    HashMap<String,String> fsmLeaderObjects;
    int i=0;


    /*End: For Location*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toq);
        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        toqReceiver = new ToqBroadcastReceiver();
        init();
        setupUI();
        detectLocation();
    }


    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(proxReceiver);
        locationManager.removeProximityAlert(pIntent1);
    }


    public void onResume() {


        super.onResume();
        Toast.makeText(ToqActivity.this, "Resuming", Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(provider, 400, 1, this);

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
       findViewById(R.id.send_notif_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // sendNotification();
            }
        });

        findViewById(R.id.install_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                install();
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

        findViewById(R.id.remove_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDeckOfCards();
            }
        });
    }
    public void detectLocation()
    {
           /*START THE PART WHERE YOU DETECT LOCATION*/


        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        location = locationManager.getLastKnownLocation(provider);

        setLocationAlert();

        /*END THE PART WHERE YOU DETECT LOCATION*/

    }
    public void setLocationAlert()
    {
        double lat = 37.58;
        double lon = -122.01;
        float radius = 50;
        String geo = "geo:"+lat+","+lon;
        Intent intent = new Intent(PROX_ALERT, Uri.parse(geo));
        intent.putExtra("message", "Union City, CA");
        pIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);


        locationManager.addProximityAlert(lat, lon, radius, 60000L, pIntent1);



        proxReceiver = new ProximityReceiver();

        IntentFilter iFilter = new IntentFilter(PROX_ALERT);
        iFilter.addDataScheme("geo");

        registerReceiver(proxReceiver, iFilter);
    }



    //Start: LocationListener overridden methods
    @Override
    public void onProviderEnabled(String s){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 100, this);
    }
    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude();
        lng = location.getLongitude();

        Toast.makeText(ToqActivity.this, "Received GPS request for "+ String.valueOf(lat) + "," + String.valueOf(lng) + " , ready to rumble!", Toast.LENGTH_SHORT).show();

        // Do clever stuff here

        sendNotification();
    }

    public void onStatusChanged(String s, int i, Bundle bundle){

    }
    public void onProviderDisabled(String s){
        locationManager.removeUpdates(this);

    }
    //End: LocationListener overridden methods



    private void sendNotification() {




        Random generator = new Random();
        List<String> keys      = new ArrayList<String>(fsmLeaderObjects.keySet());
        String       randomKey = keys.get( generator.nextInt(keys.size()) );
        String       value     = fsmLeaderObjects.get(randomKey);

        //Object[] values = fsmLeaderObjects.values().toArray();
        //Object randomValue = values[generator.nextInt(values.length)];
        System.out.println("randomKey = "+randomKey+ " randomvalue = "+value);


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
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Installs applet to Toq watch if app is not yet installed
     */
    private void install() {
        boolean isInstalled = true;

        try {
            System.out.println("***********1*************");
            isInstalled = mDeckOfCardsManager.isInstalled();
            System.out.println("***********1************* isInstalled= "+isInstalled);
            //START: ADDED FROM init()

            DeckOfCardsLauncherIcon whiteIcon = null;
            DeckOfCardsLauncherIcon colorIcon = null;

            // Get the launcher icons
            try{
                whiteIcon= new DeckOfCardsLauncherIcon("white.launcher.icon", getBitmap("bw.png"), DeckOfCardsLauncherIcon.WHITE);
                colorIcon= new DeckOfCardsLauncherIcon("color.launcher.icon", getBitmap("color.png"), DeckOfCardsLauncherIcon.COLOR);
            }
            catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't get launcher icon");
                return;
            }

            mCardImages = new CardImage[6];
            try{
                mCardImages[0]= new CardImage("card.image.1", getBitmap("jack_weinberg_toq.png"));
                mCardImages[1]= new CardImage("card.image.2", getBitmap("joan_baez_toq.png"));
                mCardImages[2]= new CardImage("card.image.3", getBitmap("michael_rossman_toq.png"));
                mCardImages[3]= new CardImage("card.image.4", getBitmap("art_goldberg_toq.png"));
                mCardImages[4]= new CardImage("card.image.5", getBitmap("jackie_goldberg_toq.png"));
                mCardImages[5]= new CardImage("card.image.6", getBitmap("mario_savio_toq.png"));
            }
            catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't get picture icon");
                // UNCOMMENT LATER return;
            }

            // Try to retrieve a stored deck of cards
            try {
                System.out.println("BEFORE mRemoteDeckOfCards = "+mRemoteDeckOfCards);

                // If there is no stored deck of cards or it is unusable, then create new and store
              /*  if ((mRemoteDeckOfCards = getStoredDeckOfCards()) == null){
                    System.out.println("@@@@@@@@@@@@@@@@@@ mRemoteDeckOfCards = "+mRemoteDeckOfCards);

                    mRemoteDeckOfCards = createDeckOfCards();
                    storeDeckOfCards();
                }*/
                System.out.println("AFTER mRemoteDeckOfCards = "+mRemoteDeckOfCards);
            }
            catch (Throwable th){
                th.printStackTrace();
                mRemoteDeckOfCards = null; // Reset to force recreate
            }

            // Make sure in usable state
            if (mRemoteDeckOfCards == null){
                 mRemoteDeckOfCards = createDeckOfCards();
            }
            System.out.println("%%%%%%%%%%%%%%%% mRemoteResourceStore = "+mRemoteResourceStore);
            // Set the custom launcher icons, adding them to the resource store
            mRemoteDeckOfCards.setLauncherIcons(mRemoteResourceStore, new DeckOfCardsLauncherIcon[]{whiteIcon, colorIcon});

            // Re-populate the resource store with any card images being used by any of the cards
            for (Iterator<Card> it= mRemoteDeckOfCards.getListCard().iterator(); it.hasNext();){

                String cardImageId= ((SimpleTextCard)it.next()).getCardImageId();

                if ((cardImageId != null) && !mRemoteResourceStore.containsId(cardImageId)){

                    if (cardImageId.equals("card.image.1")){
                        mRemoteResourceStore.addResource(mCardImages[0]);
                    }

                    else if (cardImageId.equals("card.image.2")){
                        mRemoteResourceStore.addResource(mCardImages[1]);
                    }
                    else if (cardImageId.equals("card.image.3")){
                        mRemoteResourceStore.addResource(mCardImages[2]);
                    }

                    else if (cardImageId.equals("card.image.4")){
                        mRemoteResourceStore.addResource(mCardImages[3]);
                    }

                    else if (cardImageId.equals("card.image.5")){
                        mRemoteResourceStore.addResource(mCardImages[4]);
                    }

                    else if (cardImageId.equals("card.image.6")){
                        mRemoteResourceStore.addResource(mCardImages[5]);
                    }


                }
            }

            //END: ADDED FROM init()
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            System.out.println("***********1 Exception *************"+e.getMessage());
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (!isInstalled) {
            try {
                System.out.println("***********2************* mRemoteDeckOfCards = "+mRemoteDeckOfCards);
                mDeckOfCardsManager.installDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
                System.out.println("***********2 After*************");
            } catch (RemoteDeckOfCardsException e) {
                e.printStackTrace();
                System.out.println("***********2 Exception*************"+e.getMessage());
                Toast.makeText(this, "Error: Cannot install application", Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("***********3*************");
            Toast.makeText(this, "App is already installed!", Toast.LENGTH_SHORT).show();
        }

        try{
            System.out.println("***********4*************");
            storeDeckOfCards();
            System.out.println("***********4 After storeDeckOfCards()*************");
        }
        catch (Exception e){
            System.out.println("***********5 Exeption*************"+e.getMessage());
            e.printStackTrace();
        }
    }

    private void uninstall() {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();


        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (isInstalled) {
            try{
                mDeckOfCardsManager.uninstallDeckOfCards();
            }
            catch (RemoteDeckOfCardsException e){
                Toast.makeText(this, getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            }
        } else {
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
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete Card from ListCard", Toast.LENGTH_SHORT).show();
        }

    }

    // Initialise
    private void init()
    {

        fsmLeaderObjects = new HashMap<String, String>();

        fsmLeaderObjects.put("Mario Savio", "Express your own view of free speech in a drawing");
        fsmLeaderObjects.put("Joan Baez","Draw a megaphone");
        fsmLeaderObjects.put("Art Goldberg","Draw Now");
        fsmLeaderObjects.put("Michael Rossman","Draw Free Speech");
        fsmLeaderObjects.put("Jack Weinberg","Draw FSM");
        fsmLeaderObjects.put("Jackie Goldberg","Draw SLATE");
        // Create the resource store for icons and images
        mRemoteResourceStore= new RemoteResourceStore();



    }

    // Read an image from assets and return as a bitmap
    private Bitmap getBitmap(String fileName) throws Exception{

        try{
            System.out.println("Filename ="+fileName);
            InputStream is= getAssets().open(fileName);
            System.out.println("Input Stream ="+is);
            return BitmapFactory.decodeStream(is);
        }
        catch (Exception e){
            System.out.println("EXCEPTION in getBitmap="+e.getMessage());
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
        System.out.println("In storeDeckOfCards ");
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
        System.out.println("In storeDeckOfCards After commit");
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

        //START: CODE THAT EXISTED BEFORE
       /* ListCard listCard =  new ListCard();

        SimpleTextCard simpleTextCard= new SimpleTextCard("card0");
        listCard.add(simpleTextCard);

        simpleTextCard= new SimpleTextCard("card1");
        listCard.add(simpleTextCard);

        return new RemoteDeckOfCards(this, listCard);*/
        //END: CODE THAT EXISTED BEFORE




        ListCard listCard =  new ListCard();// mRemoteDeckOfCards.getListCard();

        // Create a SimpleTextCard with 1 + the current number of SimpleTextCards
        SimpleTextCard simpleTextCard;



        for (Map.Entry<String,String> entry : fsmLeaderObjects.entrySet()) {


            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("IN FOR LOOP...key = "+key+" value = "+value);
            //FOR SIMPLE TEXT CARD FOR EACH PERSON

            simpleTextCard = new SimpleTextCard(Integer.toString(i));
            try {

                 //simpleTextCard.setCardImage(mRemoteResourceStore,mCardImages[i]);

            }
            catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't get picture icon");
                // UNCOMMENT LATER return;
            }
            simpleTextCard.setHeaderText(key);
            simpleTextCard.setTitleText(value);
            String[] messages = {"Message: " + Integer.toString(i)};
            simpleTextCard.setMessageText(messages);
            simpleTextCard.setReceivingEvents(false);
            simpleTextCard.setShowDivider(true);

            listCard.add(simpleTextCard);

            i++;
        }


        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to Create SimpleTextCard", Toast.LENGTH_SHORT).show();
        }
        return new RemoteDeckOfCards(this, listCard);
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
