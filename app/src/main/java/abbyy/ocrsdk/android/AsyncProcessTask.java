package abbyy.ocrsdk.android;

import java.io.FileOutputStream;

import com.abbyy.ocrsdk.*;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncProcessTask extends AsyncTask<String, String, Boolean> {

	public AsyncProcessTask(ResultsActivity activity) {
		this.activity = activity;
		dialog = new ProgressDialog(activity);
	}

	private ProgressDialog dialog;
	/** application context. */
	private final ResultsActivity activity;

	protected void onPreExecute() {
		dialog.setMessage("Processing");
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	protected void onPostExecute(Boolean result) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		
		activity.updateResults(result);
	}

	@Override
	protected Boolean doInBackground(String... args) {

		String inputFile = args[0];
		String outputFile = args[1];

		try {
			Client restClient = new Client();
			
			//!!! Please provide application id and password and remove this line. !!!
			// To create an application and obtain a password,
			// register at http://cloud.ocrsdk.com/Account/Register
			// More info on getting your application id and password at
			// http://ocrsdk.com/documentation/faq/#faq3
			
			// Name of application you created
			restClient.applicationId = "BookMarkerForAndroid";
			// You should get e-mail from ABBYY Cloud OCR SDK service with the application password
			restClient.password = "dk5cr2To5d1Lw1Focy5u6Ew7";
			
			// Obtain installation id when running the application for the first time
			SharedPreferences settings = activity.getPreferences(Activity.MODE_PRIVATE);
			String instIdName = "installationId";
			if( !settings.contains(instIdName)) {
				// Get installation id from server using device id
				String deviceId = android.provider.Settings.Secure.getString(activity.getContentResolver(), 
						android.provider.Settings.Secure.ANDROID_ID);
				Log.i("noduritoto", "1. deviceId id :" + deviceId);

				// Obtain installation id from server
				publishProgress( "First run: obtaining installation id..");
				String installationId = restClient.activateNewInstallation(deviceId);
				publishProgress( "Done. Installation id is '" + installationId + "'");
				Log.i("noduritoto", "2. installation id :" + installationId);
				
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(instIdName, installationId);
				editor.commit();
			} 
			
			String installationId = settings.getString(instIdName, "");
			restClient.applicationId += installationId;
			
			publishProgress( "Uploading image...");
			Log.i("noduritoto", "3. Uploading image");

			String language = "English,Korean"; // Comma-separated list: Japanese,English or German,French,Spanish etc.
			
			ProcessingSettings processingSettings = new ProcessingSettings();
			processingSettings.setOutputFormat( ProcessingSettings.OutputFormat.txt );
			processingSettings.setLanguage(language);
			
			publishProgress("Uploading..");
			Log.i("noduritoto", "4. Uploading");

			// If you want to process business cards, uncomment this
			/*
			BusCardSettings busCardSettings = new BusCardSettings();
			busCardSettings.setLanguage(language);
			busCardSettings.setOutputFormat(BusCardSettings.OutputFormat.xml);
			Task task = restClient.processBusinessCard(filePath, busCardSettings);
			*/
			Log.i("noduritoto", "5. Before Task");
			Log.i("noduritoto", "5-1. input file : " + inputFile);
			Log.i("noduritoto", "5-2. processingSettings" + processingSettings);
			Bitmap img= BitmapFactory.decodeResource(activity.getResources(), R.drawable.testtext2);
			Task task = restClient.testProcessImage(img, processingSettings);
			//Task task = restClient.processImage(inputFile, processingSettings);
			Log.i("noduritoto", " check 'task' value : " + task);
			
			while( task.isTaskActive() ) {
				// Note: it's recommended that your application waits
				// at least 2 seconds before making the first getTaskStatus request
				// and also between such requests for the same task.
				// Making requests more often will not improve your application performance.
				// Note: if your application queues several files and waits for them
				// it's recommended that you use listFinishedTasks instead (which is described
				// at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).

				Thread.sleep(5000);
				publishProgress( "Waiting.." );
				Log.i("noduritoto", "5. waiting");
				task = restClient.getTaskStatus(task.Id);
				Log.i("noduritoto", "6. check 'task' value : " + task);
			}
			
			if( task.Status == Task.TaskStatus.Completed ) {
				publishProgress( "Downloading.." );
				FileOutputStream fos = activity.openFileOutput(outputFile,Context.MODE_PRIVATE);
				
				try {
					restClient.downloadResult(task, fos);
				} finally {
					fos.close();
				}
				
				publishProgress( "Ready" );
			} else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
				Log.i("noduritoto", "No Credit");
				throw new Exception( "Not enough credits to process task. Add more pages to your application's account." );
			} else {
				Log.i("noduritoto", "Task failed");
				throw new Exception( "Task failed" );
			}
			
			return true;
		} catch (Exception e) {
			final String message = "Error: " + e.getMessage();
			publishProgress( message);
			activity.displayMessage(message);
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		String stage = values[0];
		dialog.setMessage(stage);
		// dialog.setProgress(values[0]);
	}

}
