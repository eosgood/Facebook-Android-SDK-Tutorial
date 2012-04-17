package com.eosgood.tutorials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FacebookTutorialActivity extends Activity {

	public static final int LOGIN = Menu.FIRST;
	public static final int GET_EVENTS = Menu.FIRST + 1;
	public static final int GET_ID = Menu.FIRST + 2;

	public static final String APP_ID = "";
	public static final String TAG = "FACEBOOK CONNECT";

	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler = new Handler();

	private static final String[] PERMS = new String[] { "user_events" };

	private TextView mText;
	private LinearLayout eventLayout;

	private ArrayList<FbEvent> events = new ArrayList<FbEvent>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);

		if (APP_ID == null || APP_ID.equals("")) {
			Util.showAlert(this, "Warning", "Facebook Applicaton ID must be "
					+ "specified before running");
		}

		// setup the content view
		initLayout();
		mText.setText("Login Please");

		// setup the facebook session
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}

	protected void initLayout() {
		LinearLayout rootView = new LinearLayout(this.getApplicationContext());
		rootView.setOrientation(LinearLayout.VERTICAL);

		this.mText = new TextView(this.getApplicationContext());
		this.mText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		rootView.addView(this.mText);

		this.setContentView(rootView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, LOGIN, Menu.NONE, "Login Text");
		menu.add(Menu.NONE, GET_EVENTS, Menu.NONE, "Get Events");
		menu.add(Menu.NONE, GET_ID, Menu.NONE, "Get UserID");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(Menu.FIRST);
		MenuItem getID = menu.findItem(GET_ID);

		if (mFacebook.isSessionValid()) {
			loginItem.setTitle("Logout");
			getID.setEnabled(true);
		} else {
			loginItem.setTitle("Login");
			getID.setEnabled(false);
		}
		loginItem.setEnabled(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case LOGIN:
			if (mFacebook.isSessionValid()) {
				mText.setText("Logging out...");
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(
						mFacebook);
				asyncRunner.logout(this, new LogoutRequestListener());
			} else {
				mFacebook.authorize(this, PERMS, new LoginDialogListener());
			}
			break;
		case GET_ID:
			mAsyncRunner.request("me", new IDRequestListener());
			break;
		case GET_EVENTS:
			mAsyncRunner.request("me/events", new EventRequestListener());
			break;
		default:
			return false;
		}
		return true;
	}

	private class LoginDialogListener implements DialogListener {

		public void onComplete(Bundle values) {
			mText.setText("Facebook login successful. Press Menu...");
		}

		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub

		}

		public void onError(DialogError e) {
			// TODO Auto-generated method stub

		}

		public void onCancel() {
			// TODO Auto-generated method stub

		}
	}

	private class LogoutRequestListener implements RequestListener {

		public void onComplete(String response, Object state) {
			// Dispatch on its own thread
			mHandler.post(new Runnable() {
				public void run() {
					mText.setText("Logged out");
				}
			});
		}

		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub

		}

		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub

		}
	}

	private class IDRequestListener implements RequestListener {

		public void onComplete(String response, Object state) {
			try {
				// process the response here: executed in background thread
				Log.d(TAG, "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String id = json.getString("id");
 
				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."
				FacebookTutorialActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						mText.setText("Hello there, " + id + "!");
					}
				});
			} catch (JSONException e) {
				Log.w(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub

		}

		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub

		}
	}
	
	private class EventRequestListener implements RequestListener {

		public void onComplete(String response, Object state) {
			try {
				// process the response here: executed in background thread
				Log.d(TAG, "Response: " + response.toString());
				final JSONObject json = new JSONObject(response);
				JSONArray d = json.getJSONArray("data");
 
				for (int i = 0; i < d.length(); i++) {
					JSONObject event = d.getJSONObject(i);
					FbEvent newEvent = new FbEvent(event.getString("id"),
							event.getString("name"),
							event.getString("start_time"),
							event.getString("end_time"),
							event.getString("location"));
					events.add(newEvent);
 
				}
 
				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."
				FacebookTutorialActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						for (FbEvent event : events) {
							TextView view = new TextView(
									getApplicationContext());
							view.setText(event.getTitle());
							view.setTextSize(16);
 
							eventLayout.addView(view);
						}
					}
				});
			} catch (JSONException e) {
				Log.w(TAG, "JSON Error in response");
			}
		}

		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
	}
}