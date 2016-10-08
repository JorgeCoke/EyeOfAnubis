package org.jojoma.safe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by cokelas on 30/9/16.
 */

public class BackgroundService extends Service{
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 12345;

    MediaPlayer mp;
    Handler handler;
    Runnable periodicTask;
    static final long TASK_PERIOD = 200;
    int cameraIndex;// Integer to keep track of which camera is open.
    int smileValue = 0;
    int leftEyeBlink = 0;
    int rightEyeBlink = 0;
    int faceRollValue = 0;
    int pitch = 0;
    int yaw = 0;
    int horizontalGaze = 0;
    int verticalGaze = 0;
    boolean calibrating = true;
    int calibratedTimes = 0;
    private static final int TIMES_TO_CALIBRATE = 25;
    boolean fpFeatureSupported = false;
    FacialProcessing faceProc;
    private final int FRONT_CAMERA_INDEX = 1;
    Camera cameraObj;
    private CameraSurfacePreview mPreview;
    private DrawView drawView;
    FrameLayout preview;
    Display display;

    EyeControl eyeControl = new EyeControl();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();

        mp = MediaPlayer.create(this,R.raw.alarm);

        handler = new Handler();
        periodicTask = new Runnable(){

            @Override
            public void run(){
                // Save params
                eyeControl.addValueHorizontal(horizontalGaze);
                eyeControl.addValueVertical(verticalGaze);

                Log.d(TAG, "calibrating: " + calibrating);
                // Check if frame is used to calibrate
                if (calibrating){

                    calibratedTimes+=1;
                    Log.d(TAG, "calibratedTimes: " + calibratedTimes);
                    if (calibratedTimes == TIMES_TO_CALIBRATE){
                        // Got all the necessary frames
                        //FaceRecogHelper.setAllMeasureReferences();
                        calibrating = false;
                    }
                }
                else{

                    // Check whether an alert should be popped up
                    //List<Object> alertInfo = FaceRecogHelper.getAlertLevel();
                    //changeProgress(progressBar, ((Double) alertInfo.get(1)).intValue());
                    //Log.d(TAG, "alert: " + (boolean) alertInfo.get(0));
                    //if ((boolean) alertInfo.get(0)){
                    if (false){
                        // Pop up an alert
                        if (!mp.isPlaying()) {
                            Log.d(TAG, "starting sound");
                            mp.start();
                        }
                        //alertIcon.setVisibility(View.VISIBLE);
                    }
                    else{
                        // Stop alert
                        if (mp.isPlaying()) {
                            Log.d(TAG, "stopping sound");
                            mp.stop();
                            try {
                                mp.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //alertIcon.setVisibility(View.INVISIBLE);
                    }
                }
                // Delay N millis
                handler.postDelayed(periodicTask, TASK_PERIOD);
            }
        };

        // Check to see if the FacialProc feature is supported in the device or no.
        fpFeatureSupported = FacialProcessing.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING);

        if (fpFeatureSupported && faceProc == null) {
            Log.e("TAG", "Feature is supported");
            faceProc = FacialProcessing.getInstance();  // Calling the Facial Processing Constructor.
            faceProc.setProcessingMode(FacialProcessing.FP_MODES.FP_MODE_VIDEO);
        } else {
            Log.e("TAG", "Feature is NOT supported");
            return;
        }

        cameraIndex = FRONT_CAMERA_INDEX;// Start with front Camera

        try {
            cameraObj = Camera.open(cameraIndex); // attempt to get a Camera instance
            //CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            // String[] cams = manager.getCameraIdList();
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");// Camera is not available (in use or does not exist)
        }

        // Change the sizes according to phone's compatibility.
        mPreview = new CameraSurfacePreview(BackgroundService.this, cameraObj, faceProc);
        preview.removeView(mPreview);
        //preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        //cameraObj.setPreviewCallback(CameraPreviewActivity.this);

        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        // Start the periodic task
        handler.post(periodicTask);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO: aqui se reciben los datos de la actividad
        Log.e("TAG", "Received start id " + startId + ": " + intent);
        String value = intent.getExtras().getString("KEY");
        String value2 = intent.getStringExtra("KEY");
        Log.e("TAG","Value: "+value + "VAlue2: " +  value2);
        Toast.makeText(this, "Mensaje recibido es: " + value, Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "LOCAL SERVIDCE STOPPED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Servicio en segundo plano activo";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CameraPreviewActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Safetify")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}
