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
import android.view.MotionEvent;
import android.view.View;
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
import gmail.yuyang226.flickrj.sample.android.FlickrHelper;
import org.json.JSONException;

import javax.xml.parsers.ParserConfigurationException;

import gmail.yuyang226.flickrj.sample.android.FlickrjActivity;


public class FSMToqActivity extends Activity implements OnItemSelectedListener {

    // stores the spinner icons

    // stores the spinner item names
    private String[] spinnerNameArray = { "Black", "Red", "Blue", "Green" };
    ArrayList<HashMap> spinnerList = new ArrayList<HashMap>();
    HashMap map;
    Spinner mySpinner;
    static DrawingView myViewInstance;
    ImageButton eraserButton;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsmtoq);
        //setContentView(new DrawingView(this, null));




        mySpinner = (Spinner)findViewById(R.id.colors_spinner);
        mySpinner.setAdapter(new ColorsAdapter(FSMToqActivity.this, R.layout.colors_spinner, spinnerNameArray));
        mySpinner.setOnItemSelectedListener(this);

        initializeImageList();

        myViewInstance = (DrawingView)findViewById(R.id.drawing); //new DrawingView(this);
        myViewInstance.setDrawingCacheEnabled(true);
        myViewInstance.buildDrawingCache();

        addListenerOnEraserButton();
        addListenerOnSaveButton();


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

        String newColorStr = parent.getItemAtPosition(pos).toString().toLowerCase();

        myViewInstance.setColor(newColorStr,20);


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
        //ImageView imageView = new ImageView(this);
        //imageView.setBackgroundResource((spinnerList.get(0).get("Icon")));
       // spinnerList.get(0).get("Name");

        //color = (new MyOnItemSelectedListener()).selectedColor;

    }
    public void addListenerOnEraserButton()
    {
        eraserButton = (ImageButton) findViewById(R.id.eraserbutton);
        eraserButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

               // Toast.makeText(FSMToqActivity.this  "Eraser has been clicked!", Toast.LENGTH_SHORT).show();

                myViewInstance.setColor("white",40);

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


                     Toast.makeText(FSMToqActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

               //MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

               /* START: FOR UPLOADING TO FLICKR */
               showImage();
               Intent intent = new Intent(getApplicationContext(), FlickrjActivity.class);
               intent.putExtra("flickImagePath", file.getAbsolutePath());

               startActivity(intent);


                /* END : FOR UPLOADING TO FLICKR */



                   // Android equipment Gallery application will only at boot time scanning system folder
                    // The simulation of a media loading broadcast, for the preservation of images can be viewed in Gallery

                    intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
                    intent.setData(Uri.fromFile(Environment
                                     .getExternalStorageDirectory()));
                   // sendBroadcast(intent);

                  } catch (Exception e) {
                     Toast.makeText(FSMToqActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
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

                                //Write code for updating cards with a new image

                            }
                                catch (Exception e) {
                                    Toast.makeText(FSMToqActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
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
        private Paint drawPaint = new Paint();
        private Paint canvasPaint;
        // Holds draw calls
        private Canvas drawCanvas;
        // Bitmap to hold pixels
        private Bitmap canvasBitmap;
        private Path path = new Path();
        float x = 0;
        float y = 0;
        static int paintColor;
        static int strokeWidth = 20;
        HashMap<Path,Integer> colorsMap = new HashMap<Path,Integer>();
        HashMap<Path,Integer> strokeWidthMap = new HashMap<Path,Integer>();
        ArrayList<Path> paths = new ArrayList<Path>();
        Display display;
        Paint bitmapPaint;
        Bitmap mbitmap;

        public DrawingView(Context context) {

            super(context);
            bitmapPaint = new Paint(Paint.DITHER_FLAG);

        }

        public DrawingView(Context context, AttributeSet attrs) {
            super(context, attrs);

        }
        public DrawingView(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {

            // Change the color of paint to use
            super.onDraw(canvas);

            canvas.drawBitmap(mbitmap, 0, 0, bitmapPaint);

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
            }

            drawPaint.setColor(paintColor);
            drawPaint.setStrokeWidth(strokeWidth);

            canvas.drawPath(path, drawPaint);
           // Toast.makeText(getContext(), "In onDraw...paintcolor= " + drawPaint.getColor(), Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // Make a new bitmap that stores each pixel on 4 bytes

            mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(mbitmap);
        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // float eventX = event.getX();
            //float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    //path.moveTo(eventX, eventY);
                    path.moveTo(event.getX(), event.getY());
                    path.lineTo(event.getX(), event.getY());
                    //baseBitmap = Bitmap.createBitmap(myViewInstance.getWidth(), myViewInstance.getHeight(), Bitmap.Config.ARGB_8888);

                    break;
                //return true;
                case MotionEvent.ACTION_MOVE:
                    x = event.getX();
                    y = event.getY();
                    path.lineTo(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    colorsMap.put(path,new Integer(paintColor));
                    strokeWidthMap.put(path, new Integer(strokeWidth));
                    paths.add(path);
                    path = new Path();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                default:
                    break;
            }

            // Schedules a repaint.
            //invalidate();

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
            else
            {
                newColor = Color.WHITE;
            }

            paintColor = newColor;
            strokeWidth = newStrokeWidth;

            //invalidate();
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
            TextView label=(TextView)row.findViewById(R.id.colorName);
            label.setText(spinnerNameArray[position]);

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

            return row;
        }
    }

}

