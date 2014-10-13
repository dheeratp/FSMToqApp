package dheera.cs160.berkeley.edu.fsmtoqapp;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.view.MenuItem;
import android.widget.TextView;
import android.graphics.Paint;
import android.graphics.Path;
import android.widget.SeekBar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.os.Environment;
import java.io.File;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.view.Display;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Random;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.util.Config;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.location.Criteria;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.os.IBinder;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.Settings;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.CardImage;

import gmail.yuyang226.flickrj.sample.android.FlickrHelper;
import org.json.JSONException;

import javax.xml.parsers.ParserConfigurationException;

import gmail.yuyang226.flickrj.sample.android.FlickrjActivity;


public class FSMToqActivity extends Activity implements OnItemSelectedListener {

    // stores the spinner icons

    // stores the spinner item names
    private String[] spinnerNameArray = { "Black", "Red", "Blue", "Green" };
    private String[] spinnerShapeNameArray = { "None","Circle", "Square"};

    ArrayList<HashMap> spinnerList = new ArrayList<HashMap>();
    ArrayList<HashMap> spinnerShapeList = new ArrayList<HashMap>();

    HashMap map;
    Spinner mySpinner;
    Spinner myShapeSpinner;
    static DrawingView myViewInstance;
    ImageButton eraserButton;
    ImageButton clearButton;
    Button saveButton;
    private static Bitmap baseBitmap;
    public static final int BUFFER_SIZE = 1024 * 8;



    /* FOR NEW CODE FOR DETECTING LOCATION */
    // flag for GPS status
    boolean isGPSEnabled = false;
    private Context mContext;
    // flag for network status
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private SeekBar brushSize = null;
    int progressChanged = 10;
    private TextView progressbarText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsmtoq);
        //setContentView(new DrawingView(this, null));
        myViewInstance = (DrawingView)findViewById(R.id.drawing); //new DrawingView(this);
        myViewInstance.setDrawingCacheEnabled(true);
        myViewInstance.buildDrawingCache();

        //Start: for brush size
        brushSize = (SeekBar) findViewById(R.id.seek1);
        progressbarText = (TextView) findViewById(R.id.textView1);

        progressbarText.setText(brushSize.getProgress() + "");



        brushSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progressChanged = progress;

                myViewInstance.setCustomStroke(progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                progressbarText.setText(progressChanged + "");

                //Toast.makeText(FSMToqActivity.this,"seek bar progress:"+progressChanged,Toast.LENGTH_SHORT).show();
            }

        });
        //End: for brush size

        mySpinner = (Spinner)findViewById(R.id.colors_spinner);
        mySpinner.setAdapter(new ColorsAdapter(FSMToqActivity.this, R.layout.colors_spinner, spinnerNameArray));
        mySpinner.setOnItemSelectedListener(this);

        //start:shape initialization
        myShapeSpinner = (Spinner)findViewById(R.id.shape_spinner);
        myShapeSpinner.setAdapter(new ShapesAdapter(FSMToqActivity.this, R.layout.shapes_spinner, spinnerShapeNameArray));
        myShapeSpinner.setOnItemSelectedListener(this);
        //end:shape initialization



        initializeImageList();



        addListenerOnEraserButton();
        addListenerOnSaveButton();
        addListenerOnClearButton();



    }
    public void addListenerOnClearButton()
    {
        clearButton = (ImageButton) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder newDialog = new AlertDialog.Builder(FSMToqActivity.this);
                newDialog.setTitle("Clear drawing");
                newDialog.setMessage("Are you sure?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        //System.out.println("You clicked ok to clear!");
                        myViewInstance.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        //System.out.println("You clicked cancel!");

                        dialog.cancel();
                    }
                });
                newDialog.show();
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fsmtoq_my, menu);
        return true;
    }
    @Override

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        //Toast.makeText(parent.getContext(), "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString().toLowerCase(), Toast.LENGTH_SHORT).show();
        Spinner spinner = (Spinner) parent;;
        String newColorStr;
        String newShapeStr;


        if(spinner.getId()== R.id.colors_spinner) {
            myViewInstance.setErase(false);
            newColorStr = parent.getItemAtPosition(pos).toString().toLowerCase();
            //Toast.makeText(FSMToqActivity.this, "Color changed"+newColorStr, Toast.LENGTH_SHORT).show();

            myViewInstance.setColor(newColorStr,progressChanged);

        }else if(spinner.getId()== R.id.shape_spinner)
        {
            myViewInstance.setErase(false);
             newShapeStr = parent.getItemAtPosition(pos).toString().toLowerCase();
            //Toast.makeText(FSMToqActivity.this, "shape changed"+newShapeStr, Toast.LENGTH_SHORT).show();

            myViewInstance.setShape(newShapeStr);

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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

    private void initializeImageList() {

        // TODO Auto-generated method stub
        for (int i = 0; i < spinnerNameArray.length; i++) {
            map = new HashMap<String, Object>();

            map.put("Name", spinnerNameArray[i]);

            //myImageView.setImageResource(id);

            //int imageResource = getResources().getIdentifier(uri, null, getPackageName());

            ImageView image = (ImageView) findViewById(getResources().getIdentifier(spinnerNameArray[i].toLowerCase(), "id", getPackageName()));

            map.put("Icon", image);
            spinnerList.add(map);
        }
        // TODO Auto-generated method stub
        for (int i = 0; i < spinnerShapeNameArray.length; i++) {
            map = new HashMap<String, Object>();

            map.put("Name", spinnerShapeNameArray[i]);

            //myImageView.setImageResource(id);

            //int imageResource = getResources().getIdentifier(uri, null, getPackageName());

            ImageView image = (ImageView) findViewById(getResources().getIdentifier(spinnerShapeNameArray[i].toLowerCase(), "id", getPackageName()));

            map.put("Icon", image);
            spinnerShapeList.add(map);
        }
//        ImageView imageView = new ImageView(this);
//        imageView.setBackgroundResource((spinnerList.get(0).get("Icon")));
//        spinnerList.get(0).get("Name");

        //color = (new MyOnItemSelectedListener()).selectedColor;

    }
    public void addListenerOnEraserButton()
    {
        eraserButton = (ImageButton) findViewById(R.id.eraserbutton);
        eraserButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // Toast.makeText(FSMToqActivity.this  "Eraser has been clicked!", Toast.LENGTH_SHORT).show();

                myViewInstance.setErase(true);

            }

        });

    }
    public void addListenerOnSaveButton()
    {
        saveButton = (Button) findViewById(R.id.saveimage);

        saveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //baseBitmap = Bitmap.createBitmap(arg0.getWidth(), arg0.getHeight(), Bitmap.Config.ARGB_8888);
                //baseBitmap = myViewInstance.getDrawingCache();
                saveBitmap();
            }

        });

    }
    /**
     114      * Save the image to the SD card.
     115      */
    protected void saveBitmap() {
        try {
            // Save the image to the SD card.


            Bitmap well = myViewInstance.getBitmap();
            Bitmap save = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            Canvas now = new Canvas(save);
            now.drawRect(new Rect(0,0,320,480), paint);
            now.drawBitmap(well, new Rect(0,0,well.getWidth(),well.getHeight()), new Rect(0,0,320,480), null);


            File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");
            file.createNewFile();
            OutputStream stream = new FileOutputStream(file);

            save.compress(Bitmap.CompressFormat.PNG, 100, stream);


            Toast.makeText(FSMToqActivity.this, "Saved to phone!", Toast.LENGTH_SHORT).show();

            //MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            /* START: FOR UPLOADING TO FLICKR */
            showImage();
            Intent intent = new Intent(getApplicationContext(), FlickrjActivity.class);
            intent.putExtra("flickImagePath", file.getAbsolutePath());

            startActivity(intent);


                /* END : FOR UPLOADING TO FLICKR */



            // Android equipment Gallery application will only at boot time scanning system folder
            // The simulation of a media loading broadcast, for the preservation of images can be viewed in Gallery

            //intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
            //intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            // sendBroadcast(intent);

        } catch (Exception e) {
            //Toast.makeText(FSMToqActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showImage() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String svr="www.flickr.com";

                    REST rest=new REST();
                    rest.setHost(svr);

                    //initialize Flickr object with key and rest
                    Flickr flickr=new Flickr(FlickrHelper.API_KEY,rest);

                    //initialize SearchParameter object, this object stores the search keyword
                    SearchParameters searchParams=new SearchParameters();
                    searchParams.setSort(SearchParameters.INTERESTINGNESS_DESC);

                    //Create tag keyword array
                    String[] tags=new String[]{"cs160fsm"};
                    searchParams.setTags(tags);

                    //Initialize PhotosInterface object
                    PhotosInterface photosInterface=flickr.getPhotosInterface();
                    //Execute search with entered tags
                    PhotoList photoList=photosInterface.search(searchParams,20,1);

                    //get search result and fetch the photo object and get small square imag's url
                    if(photoList!=null){
                        //Get search result and check the size of photo result
                        Random random = new Random();
                        int seed = random.nextInt(photoList.size());
                        //get photo object
                        Photo photo=(Photo)photoList.get(seed);

                        //Get small square url photo
                        InputStream is = photo.getMediumAsStream();
                        final Bitmap bm = BitmapFactory.decodeStream(is);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //COMMENTED BY DHEERA ImageView imageView = (ImageView) findViewById(R.id.imview);
                                //COMMENTED BY DHEERA imageView.setImageBitmap(bm);

                                try{
                                    Bitmap save = Bitmap.createBitmap(250, 288, Bitmap.Config.ARGB_8888);
                                    Paint paint = new Paint();
                                    paint.setColor(Color.WHITE);
                                    Canvas now = new Canvas(save);
                                    now.drawRect(new Rect(0, 0, 250, 288), paint);
                                    now.drawBitmap(bm, new Rect(0, 0, bm.getWidth(), bm.getHeight()), new Rect(0, 0, 250, 288), null);


                                    File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");
                                    file.createNewFile();
                                    OutputStream stream = new FileOutputStream(file);

                                    save.compress(Bitmap.CompressFormat.PNG, 100, stream);

                                    //START: Write code for updating cards with a new image
                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, 250, 288, false);

                                    //System.out.println("!!!!!!!!!"+ ToqActivity.mRemoteDeckOfCards);
                                    ListCard listCard = ToqActivity.mRemoteDeckOfCards.getListCard();
                                    int currSize = listCard.size();

                                    //System.out.println("############## current size of listcard ############# = "+ currSize);
                                    // Create a SimpleTextCard with 1 + the current number of SimpleTextCards
                                    SimpleTextCard simpleTextCard = new SimpleTextCard(Integer.toString(currSize+1));

                                    simpleTextCard.setHeaderText("Here's a Flickr image just for you.. ");
                                    simpleTextCard.setTitleText("#cs160fsm");
                                    // String[] messages = {"Select this card to view it"};
                                    // simpleTextCard.setMessageText(messages);
                                    // simpleTextCard.setReceivingEvents(true);
                                    simpleTextCard.setShowDivider(true);



                                    CardImage mCardImage= new CardImage("card.image.7", resizedBitmap);
                                    ToqActivity.mRemoteResourceStore.addResource(mCardImage);

                                    simpleTextCard.setCardImage(ToqActivity.mRemoteResourceStore,mCardImage);


                                    listCard.add(simpleTextCard);

                                    try {
                                        ToqActivity.mDeckOfCardsManager.updateDeckOfCards(ToqActivity.mRemoteDeckOfCards,ToqActivity.mRemoteResourceStore);
                                    } catch (RemoteDeckOfCardsException e) {
                                        e.printStackTrace();
                                        //Toast.makeText(FSMToqActivity.this, "Failed to Create SimpleTextCard", Toast.LENGTH_SHORT).show();
                                    }

                                    //END: Write code for updating cards with a new image
                                }
                                catch (Exception e) {
                                    //Toast.makeText(FSMToqActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (FlickrException e) {
                    e.printStackTrace();
                } catch (IOException e ) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }



    public static class DrawingView extends View  {
        // Paint
        private Paint drawPaint;
        private Paint canvasPaint;
        // Holds draw calls
         private Canvas drawCanvas;
        // Bitmap to hold pixels
        private Bitmap canvasBitmap;
        private Path path = new Path();
        float x = 0;
        float y = 0;
        static int paintColor=Color.BLACK;
        static String paintShape;
        static int strokeWidth = 20;
        HashMap<Path,Integer> colorsMap = new HashMap<Path,Integer>();
        HashMap<Path,Integer> strokeWidthMap = new HashMap<Path,Integer>();
        ArrayList<Path> paths = new ArrayList<Path>();
        Display display;
        Paint bitmapPaint;
        Bitmap mbitmap;
        private float brushSize, lastBrushSize;
        int oldcolor=Color.BLACK;
        int defaultColor=Color.BLACK;
        private boolean erase=false;
//        public DrawingView(Context context) {
//
//            super(context);
//
//            //setupDrawing();
//
//        }

        public DrawingView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setupDrawing();

        }
//        public DrawingView(Context context, AttributeSet attrs, int defStyle)
//        {
//            super(context, attrs, defStyle);
//            //setupDrawing();
//        }
        public void  setupDrawing()
        {
//prepare for drawing and setup paint stroke properties

            lastBrushSize = brushSize;
            path = new Path();
            drawPaint = new Paint();
            drawPaint.setColor(defaultColor);
            drawPaint.setAntiAlias(true);
            drawPaint.setStrokeWidth(strokeWidth);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeJoin(Paint.Join.ROUND);
            drawPaint.setStrokeCap(Paint.Cap.ROUND);
            canvasPaint = new Paint(Paint.DITHER_FLAG);


        }
        public void setLastBrushSize(float lastSize){
            lastBrushSize=lastSize;
        }
        public float getLastBrushSize(){
            return lastBrushSize;
        }

        @Override
        protected void onDraw(Canvas canvas)
        {

            //super.onDraw(canvas);
            ////System.out.println("canvas="+canvas+" canvasBitmap="+canvasBitmap+" canvasPaint="+canvasPaint);
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(path, drawPaint);

            /* START: OLD CODE COMMENTED */
            // Change the color of paint to use
             super.onDraw(canvas);


           // canvas.drawBitmap(mbitmap, 0, 0, bitmapPaint);
            //baseBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            //drawPaint.setStyle(Paint.Style.FILL);

           drawPaint.setAntiAlias(true);

            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeJoin(Paint.Join.ROUND);
            drawPaint.setAlpha(255);
            //drawPaint.setStrokeWidth(20);



            for (Path p : paths)
            {

                drawPaint.setColor(colorsMap.get(p));
                drawPaint.setStrokeWidth(strokeWidthMap.get(p));
                canvas.drawPath(p, drawPaint);

                oldcolor = colorsMap.get(p);

            }


            if(paintColor==Color.WHITE && erase==false)
            {
                //System.out.println("Color is white...eraser is off");
                drawPaint.setColor(oldcolor);

            }
            else
            {

                oldcolor = paintColor;
                drawPaint.setColor(oldcolor);
                //System.out.println("oldcolor="+oldcolor+" paintColor="+paintColor);
            }

            drawPaint.setStrokeWidth(strokeWidth);

            canvas.drawPath(path, drawPaint);
            // Toast.makeText(getContext(), "In onDraw...paintcolor= " + drawPaint.getColor(), Toast.LENGTH_LONG).show();
            //System.out.println("paintColor="+paintColor);
            oldcolor=paintColor;
        /* END: OLD CODE COMMENTED */

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // Make a new bitmap that stores each pixel on 4 bytes
            //System.out.println("w="+w+" h="+h);
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // float eventX = event.getX();
            //float eventY = event.getY();
           // //System.out.println("In onTouchevent canvas="+drawCanvas);

            drawCanvas.drawPath(path, drawPaint);

                    //path.moveTo(eventX, eventY);

                    //baseBitmap = Bitmap.createBitmap(myViewInstance.getWidth(), myViewInstance.getHeight(), Bitmap.Config.ARGB_8888);
                    if(paintShape.equals("circle"))
                    {

                        drawCanvas.drawCircle(event.getX(), event.getY(), 100, drawPaint);
                    }
                    else if (paintShape.equals("square"))
                    {

                        drawCanvas.drawRect(event.getX()+80,event.getY()+80,event.getX()-80,event.getY()-80,drawPaint);
                    }
                    else if (paintShape.equals("none"))
                    {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                path.moveTo(event.getX(), event.getY());
                                path.lineTo(event.getX(), event.getY());
                                break;
                            case MotionEvent.ACTION_MOVE:
                                x = event.getX();
                                y = event.getY();
                                path.lineTo(x, y);
                                //invalidate();
                                break;
                            case MotionEvent.ACTION_UP:

                                colorsMap.put(path,new Integer(paintColor));
                                strokeWidthMap.put(path, new Integer(strokeWidth));
                                paths.add(path);
                                drawCanvas.drawPath(path, drawPaint);
                                path.lineTo(event.getX(), event.getY());

                                path.reset();
                                break;

                                /*Now*/
//                                drawCanvas.drawPath(path, drawPaint);
//                                path.reset();
//                                break;
                            case MotionEvent.ACTION_CANCEL:
                                break;
                            default:
                                break;
                    }

                }


            // Schedules a repaint.
            invalidate();

            return true;
        }

        public void setColor(String newColorStr,int newStrokeWidth){
            int newColor;

            if(newColorStr.equals("black")) {
                newColor = Color.BLACK;
            }
            else if(newColorStr.equals("red")) {
                newColor = Color.RED;
            }
            else if(newColorStr.equals("green"))
            {
                newColor = Color.GREEN;
            }
            else if(newColorStr.equals("blue"))
            {
                newColor = Color.BLUE;
            }
            else if(newColorStr.equals("white"))
            {
                newColor = Color.WHITE;
            }
            else
            {
                newColor = Color.GRAY;
            }
            paintColor = newColor;

            strokeWidth = newStrokeWidth;
            //System.out.println("Color in DrawingView="+paintColor+" strokewidth ="+strokeWidth);

            //invalidate();
        }

        public void setShape(String newShapeStr){
            String newShape="circle";

            if(newShapeStr.toLowerCase().equals("circle")) {
                newShape = "circle";
            }
            else if(newShapeStr.equals("square")) {
                newShape = "square";
            }
            if(newShapeStr.toLowerCase().equals("none")) {
                newShape = "none";
            }

            paintShape = newShape;

            //invalidate();
        }
        public void setCustomStroke(int stroke){
            //invalidate();
           //System.out.println("Stroke width = "+stroke);
            brushSize=stroke;
            strokeWidth=stroke;
            drawPaint.setStrokeWidth(brushSize);


        }

        public void startNew(){
            //System.out.println("Before clearing");
//            mbitmap = Bitmap.createBitmap(myViewInstance.getWidth(), myViewInstance.getHeight(), Bitmap.Config.ARGB_8888);
//            drawCanvas = new Canvas(mbitmap);
//            path.reset();
//            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//            drawPaint.reset();
//            path = new Path();
//            drawPaint = new Paint();
//            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            drawCanvas.drawRect(0, 0, 0, 0, drawPaint);

//            mbitmap = Bitmap.createBitmap(myViewInstance.getWidth(), myViewInstance.getHeight(), Bitmap.Config.ARGB_8888);
//            drawCanvas = new Canvas(mbitmap);

            //drawCanvas.drawColor(Color.WHITE);
            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            invalidate();
        }
        public Bitmap getBitmap()
        {
            //this.measure(100, 100);
            //this.layout(0, 0, 100, 100);
            this.setDrawingCacheEnabled(true);
            this.buildDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
            this.setDrawingCacheEnabled(false);


            return bmp;
        }
        public void setErase(boolean isErase){

            erase=isErase;

            if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            else drawPaint.setXfermode(null);
            if(erase) {
                //paintColor = Color.WHITE;
            }
            //System.out.println("In ERASE = "+erase+" paintColor="+paintColor);


        }

    }


    public class ShapesAdapter extends ArrayAdapter<String> {

        public ShapesAdapter(Context context, int textViewResourceId,
                             String[] objects) {
            super(context, textViewResourceId, objects);
// TODO Auto-generated constructor stub
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
// TODO Auto-generated method stub
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
//return super.getView(position, convertView, parent);

            LayoutInflater inflater=getLayoutInflater();
            View row=inflater.inflate(R.layout.shapes_spinner, parent, false);
//            TextView label=(TextView)row.findViewById(R.id.colorName);
//            label.setText(spinnerShapeNameArray[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.shapeIcon);

            if (spinnerShapeNameArray[position].toLowerCase().equals("circle")){
                icon.setImageResource(R.drawable.circle);
            }
            else  if (spinnerShapeNameArray[position].toLowerCase().equals("square")){
                icon.setImageResource(R.drawable.square);
            }
            else  if (spinnerShapeNameArray[position].toLowerCase().equals("none")){
                icon.setImageResource(R.drawable.none);
            }

            return row;
        }
    }


    public class ColorsAdapter extends ArrayAdapter<String> {

        public ColorsAdapter(Context context, int textViewResourceId,
                             String[] objects) {
            super(context, textViewResourceId, objects);
// TODO Auto-generated constructor stub
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
// TODO Auto-generated method stub
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
//return super.getView(position, convertView, parent);

            LayoutInflater inflater=getLayoutInflater();
            View row=inflater.inflate(R.layout.colors_spinner, parent, false);
            //TextView label=(TextView)row.findViewById(R.id.colorName);
            //label.setText(spinnerNameArray[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.colorIcon);

            if (spinnerNameArray[position]=="Red"){
                icon.setImageResource(R.drawable.red);
            }
            else  if (spinnerNameArray[position]=="Blue"){
                icon.setImageResource(R.drawable.blue);
            }
            else  if (spinnerNameArray[position]=="Green"){
                icon.setImageResource(R.drawable.green);
            }
            else if (spinnerNameArray[position]=="Black"){
                icon.setImageResource(R.drawable.black);
            }

            return row;
        }
    }

}
