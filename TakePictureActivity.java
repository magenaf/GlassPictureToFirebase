package com.example.magena.glasspicturetofirebase;

/**
 * Created by Magena on 3/5/2017.
 */

import android.os.FileObserver;
import android.provider.MediaStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.magena.glasspicturetofirebase.card.EmbeddedCardLayoutActivity;
import com.example.magena.glasspicturetofirebase.opengl.OpenGlService;
import com.example.magena.glasspicturetofirebase.slider.SliderActivity;
import com.example.magena.glasspicturetofirebase.theming.TextAppearanceActivity;
import com.example.magena.glasspicturetofirebase.touchpad.SelectGestureDemoActivity;
import com.example.magena.glasspicturetofirebase.voicemenu.VoiceMenuActivity;
import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import com.example.magena.glasspicturetofirebase.card.CardAdapter;
import com.example.magena.glasspicturetofirebase.card.CardBuilderActivity;
import com.example.magena.glasspicturetofirebase.card.CardScrollViewActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TakePictureActivity extends Activity{

    private static final int TAKE_PICTURE_REQUEST = 1;
    public final String TAG = "TakePictureActivity";
    private Camera mCamera;
    private boolean mInPreview = false;
    private boolean mCameraConfigured = false;
    String picturePath;

    private GestureDetector mGestureDetector;
    private CardScrollView mCardScroller;
    private CardAdapter mAdapter;

    static final int TAKE_PICTURE = 0;
    static final int SEND_TO_FIREBASE = 1;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // so screen stays bright
    }

    private List<CardBuilder> createCards(Context context) { // build 2-element list of scrollable cards
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(TAKE_PICTURE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Take a photo"));
        cards.add(SEND_TO_FIREBASE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Send to Firebase"));
        return cards;
    }

    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int soundEffect = Sounds.TAP;
                switch (position) {
                    case TAKE_PICTURE:
                        CardBuilder card = new CardBuilder(getApplicationContext(), CardBuilder.Layout.TEXT);
                        card.setText("Opening Camera");
                        View cardView = card.getView();
                        setContentView(cardView);
                        //startActivity(new Intent(TakePictureActivity.this, ZoomActivity.class));

                        // Do camera stuff here. Item 5 on paper prototype:

                        // setup file to save image:
                        takePicture();
                    break;

                    // To do: check whether picture has already been taken. For now, just call takePicture()
                    case SEND_TO_FIREBASE:
                        takePicture();
                        break;

                    default:
                        soundEffect = Sounds.ERROR;
                }
                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }


    // From Glass GDK Camera API (the simpler way to save pictures to glass storage):
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            uploadToFirebase(pictureFile);
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

    public void uploadToFirebase(File pictureFile){
        Intent i = new Intent(TakePictureActivity.this, MyUploadService.class);
        i.putExtra("EXTRA_FILE_URL", pictureFile);
        startActivity(i);
    }

    /** The complicated way to do it. The built-in glassware returns a file path automatically, so doesn't need this.

     * Create a File for saving an image or video.  */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SmartCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    Camera.PictureCallback mjpeg = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // copied from http://developer.android.com/guide/topics/media/camera.html#custom-camera
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.v(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Intent intent = new Intent(AppService.appService(), ImageViewActivity.class);
                intent.putExtra("picturefilepath", pictureFile);
                startActivity(intent);

                finish(); // works! (after com.example.magena.glasspicturetofirebase.card inserted to timeline)

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v(TAG,  "onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_CAMERA) { // for both quick press (image capture) and long press (video capture)
            Log.v(TAG,  "KEYCODE_CAMERA: "+ (event.isLongPress()?"long press": "not long press"));

            if (event.isLongPress()) // video capture
                return true; // If you return true from onKeyDown(), your activity consumes the event and the Glass camera
            // doesn't start. Do this only if there is no way to interrupt your activity's use of the camera (for example,
            // if you are capturing continuous video).


            // Stop the preview and release the camera.
            // Execute your logic as quickly as possible
            // so the capture happens quickly.

            if ( mInPreview ) {
                mCamera.stopPreview();

                mCamera.release();
                mCamera = null;
                mInPreview = false;
            }

            return false;


        } else {
            Log.v(TAG,  "NOT KEYCODE_CAMERA");

            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause()
    {
        if ( mInPreview ) {
            mCamera.stopPreview();

            mCamera.release();
            mCamera = null;
            mInPreview = false;
        }
        super.onPause();
    }
}
