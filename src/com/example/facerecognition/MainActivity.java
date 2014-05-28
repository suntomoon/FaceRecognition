package com.example.facerecognition;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class MainActivity extends Activity {
	private static final String TAG = "FaceRegnitionActivity";
	private Camera mCamera;
    private CameraPreview mPreview;
    private Button button_capture;
    private Bitmap img = null;
    private TextView textView;
    private String personInfo="";

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
        textView = (TextView) findViewById(R.id.textView1);
		
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

//	    	String fileName = String.format("/sdcard/camtest/%d.jpg", System.currentTimeMillis());
//	        File pictureFile = new File(fileName);
//	        
//	        if (pictureFile == null){
//	            Log.d(TAG, "Error creating media file, check storage permissions");
//	            return;
//	        }
	        
//	        InputStream is = new ByteArrayInputStream(data);
//	        
//	        Options options = new Options();
//	        options.inSampleSize = Math.max(1, (int)Math.ceil(Math.max((double)options.outWidth / 1024f, (double)options.outHeight / 1024f)));
//			options.inJustDecodeBounds = false;
//	        img = BitmapFactory.decodeStream(is, null, options);

	        try {
	        	FaceppDetect faceppDetect = new FaceppDetect();
	        	
	        	faceppDetect.setDetectCallback(new DetectCallback() {
					
					public void detectResult(JSONObject rst) {
						//Log.v(TAG, rst.toString());
						
						//use the red paint
//						Paint paint = new Paint();
//						paint.setColor(Color.RED);
//						paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);
//
//						//create a new canvas
//						Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
//						Canvas canvas = new Canvas(bitmap);
//						canvas.drawBitmap(img, new Matrix(), null);
//						
						
						try {
							//find out all faces
							final int count = rst.getJSONArray("face").length();
							String person = "";
							
							for (int i = 0; i < count; ++i) {
								//get the center point
								String age = rst.getJSONArray("face").getJSONObject(i)
										.getJSONObject("attribute").getJSONObject("age").getString("value");
								String range = rst.getJSONArray("face").getJSONObject(i)
										.getJSONObject("attribute").getJSONObject("age").getString("range");
								String gender = rst.getJSONArray("face").getJSONObject(i)
										.getJSONObject("attribute").getJSONObject("gender").getString("value");
								String race = rst.getJSONArray("face").getJSONObject(i)
										.getJSONObject("attribute").getJSONObject("race").getString("value");
								float smilingValue = (float)rst.getJSONArray("face").getJSONObject(i)
										.getJSONObject("attribute").getJSONObject("smiling").getDouble("value");
								
								String smiling = "Yes";
								
								if(Double.compare(smilingValue, 5.00) < 0) {
									smiling = "No";
								}
								
								//get face size
								//w = (float)rst.getJSONArray("face").getJSONObject(i)
								//		.getJSONObject("position").getDouble("width");
								//h = (float)rst.getJSONArray("face").getJSONObject(i)
								//		.getJSONObject("position").getDouble("height");
								
								//change percent value to the real size
								//x = x / 100 * img.getWidth();
								//w = w / 100 * img.getWidth() * 0.7f;
								//y = y / 100 * img.getHeight();
								//h = h / 100 * img.getHeight() * 0.7f;

								person += "Age: " + age + "(+/-" +range + "), Gender: " + gender + 
										", Race: " + race + ", \r\nSmiling: " + smiling;
							}
							
							//save new image
							//img = bitmap;
							
							personInfo = person;
							
							MainActivity.this.runOnUiThread(new Runnable() {
								
								public void run() {
									
									//show the image
									//imageView.setImageBitmap(img);
									//textView.setTextSize(40);
								    //textView.setText(String.valueOf(x));
								    //textView.setText(String.valueOf(y));

									textView.setText(personInfo);
									
								}
							});
							
							mCamera.stopPreview();
							mCamera.startPreview();
							
						} catch (JSONException e) {
							e.printStackTrace();
							MainActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									textView.setText("Error to connect remote server!");
								}
							});
							
							mCamera.stopPreview();
							mCamera.startPreview();
						}
					}
				});
	        	
	        	textView.setText("Waiting......");
	        	
	        	faceppDetect.detect(data);
	        	
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
					HttpRequests httpRequests = new HttpRequests("4480afa9b8b364e30ba03819f3e9eff5", "Pz9VFT8AP3g_Pz8_dz84cRY_bz8_Pz8M", true, false);
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
