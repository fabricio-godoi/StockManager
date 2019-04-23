package com.example.stockmanager.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.stockmanager.R;
import com.example.stockmanager.camera.QRCodeReader;
import com.example.stockmanager.database.Database;
import com.example.stockmanager.eventbus.MessageEvent;
import com.example.stockmanager.services.NetworkService;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;

import com.example.stockmanager.services.SyncDataService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.example.stockmanager.camera.QRCodeReader.rotateImage;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = MainActivity.class.getSimpleName();
    public static Context CONTEXT;

    private static final int REQUEST_PERMISSIONS_CODE = 0;
    private static final int PICK_IMAGE = 1;
    private static final int NUMBER_OF_PAGES = 2;

    private static final int DASHBOARD_PAGE = 0;
    private static final int CAMERA_PAGE = 1;


    LayoutInflater inflater;    //Used to create individual pages
    ViewPager viewPager;        //Reference to class to swipe views

    // Check if the QRCode is set to start as soon as the page is flipped, ar silently
    private boolean isSilentChange = false;

    private TextView qrCodeResultView;
    private LinearLayout cameraPreview;
    private QRCodeReader qrCodeReader;


    private boolean permissionsAreGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CONTEXT  = this;

        EventBus.getDefault().register(this);

        /*
         * Request peripherals access
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
//                            Manifest.permission.GET_ACCOUNTS
                    },
                    REQUEST_PERMISSIONS_CODE);
        } else {
            permissionsAreGranted = true;
        }

        /*
         * Manage application settings
         */
        Database.getInstance(CONTEXT).load();


        /*
         * Manage application services
         */

        // Start Network Service
        startService(new Intent(CONTEXT, NetworkService.class));
        startService(new Intent(CONTEXT, SyncDataService.class));

        /*
         * Create the activity layout
         */

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Reference ViewPager defined in activity
        viewPager =(ViewPager)findViewById(R.id.viewPager);
        //set the adapter that will create the individual pages
        viewPager.setAdapter(new MyPagesAdapter(this));
        viewPager.addOnPageChangeListener(onPageChangeListener);

        if(permissionsAreGranted) {
            qrCodeReader = new QRCodeReader(CONTEXT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Take picture
                if(ContextCompat.checkSelfPermission(CONTEXT, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    startActivity(new Intent(CONTEXT, TakePictureActivity.class));
                } else {
                    Toast.makeText(CONTEXT, getResources().getString(R.string.camera_permission_not_granted), Toast.LENGTH_LONG);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsAreGranted = true;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Created by Fabrício N Godoi");
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Open a popup menu to choose from qrcodes that was read to open the url in browser
        if (id == R.id.nav_qrcode) {
            SharedPreferences settings = CONTEXT.getSharedPreferences("qrcode_list", MODE_PRIVATE);
            Set<String> qrCodes = new HashSet<>();
            qrCodes = settings.getStringSet("qrcode_list", qrCodes);
            final String[] qrCode = new String[qrCodes.size()];
            Iterator qci = qrCodes.iterator();
            int i = 0;
            while(qci.hasNext()){
                qrCode[i++] = (String) qci.next();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select QRCode to open");
            builder.setItems(qrCode, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(qrCode[item]));
                    startActivity(i);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check if the selected image has a qrcode
        if (requestCode == PICK_IMAGE) {
            try {

                Uri selectedImage = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    if (bitmap != null) {
                        onDecodeImage(bitmap);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not format image", e);
                }

//                onDecodeImage(new File(getPathFromURI(selectedImage)));
            } catch (Exception e) {
                Log.e(TAG, "Image not found", e);
            }
        }
    }

    /**
     * Get the file real path from URI
     * @param contentUri
     * @return
     */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        if (res == null){
            res = contentUri.getPath();
        }
        cursor.close();
        return res;
    }

    /**
     * Manages actions when swiping through pages
     */
    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrollStateChanged(int state) {}
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        public void onPageSelected(int position) {
            // Check if this is the page you want.
            switch (position){
                case CAMERA_PAGE:
                    ((FloatingActionButton) findViewById(R.id.fab)).hide();

                    // Check if the permissions were granted to start the QRCode reader properly
                    if(!isSilentChange) {
                        cameraPreview.setBackground(null);
                        cameraPreview.getChildAt(0).setVisibility(View.VISIBLE);
                        if (qrCodeReader == null && permissionsAreGranted) {
                            qrCodeReader = new QRCodeReader(CONTEXT);
                        }
                        if (qrCodeReader != null) {
                            qrCodeReader.stop();
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            if (qrCodeResultView != null) {
                                qrCodeResultView.setText(getResources().getString(R.string.searching));
                            }
                            qrCodeReader.start();
                        }
                    }
                    break;
                case DASHBOARD_PAGE:
                default:
                    ((FloatingActionButton) findViewById(R.id.fab)).show();
                    isSilentChange = false;
                    if(qrCodeReader != null) {
                        qrCodeReader.stop();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    break;
            }
        }
    };


    //Implement PagerAdapter Class to handle individual page creation
    class MyPagesAdapter extends PagerAdapter {

        private Context CONTEXT;

        public MyPagesAdapter(Context context){
            this.CONTEXT = context;
        }

        @Override
        public int getCount() {
            //Return total pages, here one for each data item
//            return pageData.length;
            return NUMBER_OF_PAGES;
        }

        //Create the given page (indicated by position)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = null;
            switch (position){
                case CAMERA_PAGE:
                    page = inflater.inflate(R.layout.qrcode_page, null);
                    ((ViewPager) container).addView(page, position);
                    cameraPreview = (LinearLayout) findViewById(R.id.cPreview);
                    qrCodeResultView = (TextView) findViewById(R.id.qrDecode);

                    if(permissionsAreGranted) {
                        if (qrCodeReader!= null) {
                            cameraPreview.addView(qrCodeReader);
                        }
                    }

                    break;
                case DASHBOARD_PAGE:
                default:
//                    takePictureHandler.removeCallbacksAndMessages(null);
                    page = inflater.inflate(R.layout.dashboard_page, null);
                    ((ViewPager) container).addView(page, position);

                    // Create dashboard view
                    LinearLayout ln = findViewById(R.id.dashboardLayout);

                    FragmentManager fragMan = getFragmentManager();
                    final FragmentTransaction fragTransaction = fragMan.beginTransaction();

                    final ChartsFragment charts = new ChartsFragment();
                    fragTransaction.add(ln.getId(), charts, "dashboard_fragment");
                    fragTransaction.commit();


                    // Manage product creation view

                    Button accept = findViewById(R.id.acceptButton);
                    Button cancel = findViewById(R.id.cancelButton);
                    Button wipe = findViewById(R.id.wipeButton);

                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String sName = ((EditText) findViewById(R.id.stateName)).getText().toString();
                            String pName = ((EditText) findViewById(R.id.productName)).getText().toString();
                            String pValue = ((EditText) findViewById(R.id.productValue)).getText().toString();
                            try {
                                Double value = Double.parseDouble(pValue);
                                Database.getInstance(CONTEXT).createProduct(pName, value, sName);
                                charts.updateView();
                                findViewById(R.id.productRegScrollView).setVisibility(View.GONE);
                                findViewById(R.id.dashboardScrollView).setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Toast.makeText(CONTEXT, getResources().getString(R.string.value_not_supported), Toast.LENGTH_LONG);
                            }

                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.productRegScrollView).setVisibility(View.GONE);
                            findViewById(R.id.dashboardScrollView).setVisibility(View.VISIBLE);
                        }
                    });

                    wipe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Database.getInstance(CONTEXT).wipe();
                                charts.updateView();
                                findViewById(R.id.productRegScrollView).setVisibility(View.GONE);
                                findViewById(R.id.dashboardScrollView).setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Log.e(TAG, "Could not wipe the database", e);
                            }
                        }
                    });

                    break;

            }
//            ((TextView)page.findViewById(R.id.textMessage)).setText(pageData[position]);
            //Add the page to the front of the queue
            return page;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            //See if object from instantiateItem is related to the given view
            //required by API
            return arg0==(View)arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
            object=null;
        }
    }


    /**
     * Get events from any service or controller and parse to update the view accordingly
     * @param event is the event generated when some action occurred.
     *              Check MessageCode for more information
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.what){
            case QRCODE_CALLBACK:
                if(qrCodeResultView != null){
                    if(event.args.length > 0 && event.args[0] instanceof String) {
                        String arg = (String) event.args[0];
                        switch (arg){
                            case "RESULT":
                                if(event.args.length > 1) {
                                    qrCodeResultView.setText((String) event.args[1]);
                                }
                                break;
                            case "RESTARTED":
                                qrCodeResultView.setText(getResources().getString(R.string.searching));
                            default:

                                break;
                        }
                    }
                }
                break;
            case TAKEPICTURE_CALLBACK:
                try {
                    File picture = (File) event.args[0];

                    onDecodeImage(picture);
                } catch (Exception e) {
                    Log.e(TAG, "Could not parse image from TakePictureActivity", e);
                }
                break;
        }
    }


    /**
     * Try to decode a image and update the UI
     * @param bitmap Picture in bitmap format
     */
    void onDecodeImage(Bitmap bitmap){
        if (bitmap != null) {
            String decode = qrCodeReader.hasQRCode(bitmap);
            if(decode != null){
                qrCodeResultView.setText(decode);
            }
            else{
                qrCodeResultView.setText(getResources().getString(R.string.qrcode_not_found));
            }
            isSilentChange = true;
            viewPager.setCurrentItem(CAMERA_PAGE);


            // Check if the image rotation is right, checking with the device rotation
            final int rotation = ((WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case Surface.ROTATION_90:
                    break;
                case Surface.ROTATION_180:
                    bitmap = rotateImage(bitmap, 270);
                    break;
                case Surface.ROTATION_270:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                default:
                    break;
            }

            cameraPreview.setBackground(new BitmapDrawable(getResources(), bitmap));
            cameraPreview.getChildAt(0).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Try to decode a image and update the UI
     * @param picture file of the picture taken
     */
    private void onDecodeImage(File picture){
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(picture.getPath());
            onDecodeImage(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Could not parse image from TakePictureActivity", e);
        }
    }

}
