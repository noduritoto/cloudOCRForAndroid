package abbyy.ocrsdk.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;

import android.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final int TAKE_PICTURE = 0;
	private final int SELECT_FILE = 1;
	private final int MY_PERMISSION_REQUEST_STORAGE = 0;
	
	private String resultUrl = "result.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkPermission();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	
	public void captureImageFromSdCard( View view ) {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType("image/*");

    	startActivityForResult(intent, SELECT_FILE);
    }
	
	public static final int MEDIA_TYPE_IMAGE = 1;

	private static Uri getOutputMediaFileUri(){
	      return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ABBYY Cloud OCR SDK Demo App");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    }

	    // Create a media file name
	    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "image.jpg" );

	    return mediaFile;
	}
    
    public void captureImageFromCamera( View view) {
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	Uri fileUri = getOutputMediaFileUri(); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        
        startActivityForResult(intent, TAKE_PICTURE);
    } 
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		
		String imageFilePath = null;

		switch (requestCode) {
		case TAKE_PICTURE:
			imageFilePath = getOutputMediaFileUri().getPath();
			break;
		case SELECT_FILE: {
			Uri imageUri = data.getData();
			Log.i("noduritoto_image", "image uri : " + imageUri);

			String[] projection = { MediaStore.Images.Media.DATA };
			Log.i("noduritoto_image", "check Mediastore.images.media : " + MediaStore.Images.Media.DATA);
			Cursor cur = getContentResolver().query(imageUri, projection, null, null, null);
			Log.i("noduritoto_image", "check cur : " + cur);
			cur.moveToFirst();
			imageFilePath = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
			Log.i("noduritoto_image", "image file path : " + imageFilePath);
			}
			break;
		}

		//Remove output file
		deleteFile(resultUrl);
		
        Intent results = new Intent( this, ResultsActivity.class);
    	results.putExtra("IMAGE_PATH", imageFilePath);
    	results.putExtra("RESULT_PATH", resultUrl);
    	startActivity(results);
    }


	private void checkPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				// Explain to the user why we need to write the permission.
				Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
			}

			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					MY_PERMISSION_REQUEST_STORAGE);

			// MY_PERMISSION_REQUEST_STORAGE is an
			// app-defined int constant

		} else {
			// 다음 부분은 항상 허용일 경우에 해당이 됩니다.
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSION_REQUEST_STORAGE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {



					// permission was granted, yay! do the
					// calendar task you need to do.

				} else {

					Log.d("noduritoto", "Permission always deny");

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				break;
		}
	}
}
