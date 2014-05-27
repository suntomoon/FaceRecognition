package com.example.facerecognition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class MainActivity extends Activity {
	private static final String TAG = "FaceRegnitionActivity";
	private Camera mCamera;
    private CameraPreview mPreview;
    private Button button_capture;
    private Bitmap img = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        button_capture = (Button) findViewById(R.id.button_capture);
		
        button_capture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	@Override
	protected void onPause() {
		if(mCamera != null) {
			mCamera.stopPreview();
			//mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		super.onPause();
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	    	String fileName = String.format("/sdcard/camtest/%d.jpg", System.currentTimeMillis());
	        File pictureFile = new File(fileName);
	        
	        if (pictureFile == null){
	            Log.d(TAG, "Error creating media file, check storage permissions");
	            return;
	        }

	        try {
	        	new FaceppDetect().detect(data);
	        	
	            //FileOutputStream fos = new FileOutputStream(pictureFile);
	            //fos.write(data);
	            //fos.close();
	        } catch (Exception e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        }
//	        } catch (IOException e) {
//	            Log.d(TAG, "Error accessing file: " + e.getMessage());
//	        }
	    }
	};
	
	private class FaceppDetect {
    	DetectCallback callback = null;
    	
    	public void setDetectCallback(DetectCallback detectCallback) { 
    		callback = detectCallback;
    	}

    	public void detect(final byte[] array) {
    		
    		new Thread(new Runnable() {
				
				public void run() {
					HttpRequests httpRequests = new HttpRequests("f943775cc90883a683022dccceacfbed", "7OaYYHydlmvUyf1bJaR8Cp0sMewt1VBK", false, false);
		    		//Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());
		    		
//		    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		    		float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
//		    		Matrix matrix = new Matrix();
//		    		matrix.postScale(scale, scale);
//
//		    		Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
//		    		//Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());
//		    		
//		    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//		    		byte[] array = stream.toByteArray();
		    		
		    		try {
		    			//detect
						JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
						//finished , then call the callback function
						if (callback != null) {
							callback.detectResult(result);
						}
					} catch (FaceppParseException e) {
						e.printStackTrace();
						MainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								//textView.setText("Network error.");
							}
						});
					}
					
				}
			}).start();
    	}
    }
	
	interface DetectCallback {
	    void detectResult(JSONObject rst);
	}
}
