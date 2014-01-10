/**
 * Notifry for Android.
 *
 * Copyright 2011 Daniel Foote
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.notifry.android;

import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.notifry.android.database.NotifryAccount;
import com.notifry.android.database.NotifryMessage;
import com.notifry.android.database.NotifrySource;

/**
 * Notification service. This service triggers the notifications on the android
 * device.
 */
public class NotificationService extends Service
{
	/**
	 * Global or override boolean value.
	 * 
	 * @param setting
	 *            the setting
	 * @param preferences
	 *            the shared preferences
	 * @param source
	 *            the source
	 * @param defaultValue
	 *            the default value
	 * @return the boolean value
	 */
	public boolean globalOrOverrideBoolean( int setting, SharedPreferences preferences, NotifrySource source, boolean defaultValue )
	{
		if( source.getUseGlobalNotification().booleanValue() )
		{
			// Use the global setting for this notification.
			return preferences.getBoolean(getString(setting), defaultValue);
		}

		// Determine the setting, and then return the appropriate setting.
		if( R.string.playRingtone == setting )
		{
			return source.getRingtone().booleanValue();
		}
		else if( R.string.vibrateNotify == setting )
		{
			return source.getVibrate().booleanValue();
		}
		else if( R.string.ledFlash == setting )
		{
			return source.getLedFlash().booleanValue();
		}
		else if( R.string.speakMessage == setting )
		{
			return source.getSpeakMessage().booleanValue();
		}

		return defaultValue;
	}

	/**
	 * Global or override string value.
	 * 
	 * @param setting
	 *            the setting
	 * @param preferences
	 *            the shared preferences
	 * @param source
	 *            the source
	 * @param defaultValue
	 *            the default value
	 * @return the string value
	 */
	public String globalOrOverrideString( int setting, SharedPreferences preferences, NotifrySource source, String defaultValue )
	{
		if( source.getUseGlobalNotification().booleanValue() )
		{
			// Use the global setting for this notification.
			return preferences.getString(getString(setting), defaultValue);
		}

		// Determine the setting, and then return the appropriate setting.
		if( R.string.choosenNotification == setting )
		{
			return source.getCustomRingtone();
		}

		return defaultValue;
	}

	@Override
	public IBinder onBind( Intent arg0 )
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		// Fetch out our notification service.
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		int result = super.onStartCommand(intent, flags, startId);

		// On null intent, give up.
		if( intent == null )
		{
			return result;
		}

		// Determine our action.
		String operation = intent.getStringExtra("operation");

		if( operation.equals("notifry") )
		{
			// Is the master enable off? Then don't bother doing anything.
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			if( settings.getBoolean(getString(R.string.masterEnable), true) == false )
			{
				Log.d(TAG, "Master enable is off, so not doing anything.");
				return result;
			}

			// We were provided with a message ID. Load it and then handle it.
			Long messageId = Long.valueOf(intent.getLongExtra("messageId", 0));

			NotifryMessage message = NotifryMessage.FACTORY.get(this, messageId);

			// If the message is NULL, then we've been passed an invalid message
			// - return.
			if( message == null )
			{
				Log.d(TAG, "Message " + messageId + " not found, so not doing anything.");
				return result;
			}

			// Make a decision on the message.
			NotifyDecision decision = NotifyDecision.shouldNotify(this, message);

			if( decision.getShouldNotify().booleanValue() )
			{
				// OK, let's start notifying!
				Notification notification = this.setLatestEventInfo(message.getSource(), message);

				// Now, other notification methods.
				if( globalOrOverrideBoolean(R.string.playRingtone, settings, message.getSource(), true) )
				{
					String tone = globalOrOverrideString(R.string.choosenNotification, settings, message.getSource(), "");
					Log.d(TAG, "Notification selected by user: " + tone);
					if( tone.equals("") )
					{
						// Set the default notification tone.
						notification.defaults |= Notification.DEFAULT_SOUND;
					}
					else
					{
						// Load the notification and add it.
						notification.sound = Uri.parse(tone);
					}
				}
				if( globalOrOverrideBoolean(R.string.vibrateNotify, settings, message.getSource(), true) )
				{
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
				if( globalOrOverrideBoolean(R.string.ledFlash, settings, message.getSource(), true) )
				{
					if( settings.getBoolean(getString(R.string.fastLedFlash), false) )
					{
						// Special "fast flash" mode for phones with poor
						// notification LEDs.
						// i.e., my G2 that flashes very slowly so it's hard to
						// notice.
						notification.ledARGB = 0xff00ff00;
						notification.ledOnMS = 300;
						notification.ledOffMS = 1000;
						notification.flags |= Notification.FLAG_SHOW_LIGHTS;
					}
					else
					{
						// Use the default device flash notifications.
						notification.defaults |= Notification.DEFAULT_LIGHTS;
					}
				}

				// Put the notification in the tray. Use the source's local ID
				// to identify it.
				this.notificationManager.notify(message.getSource().getNotificationId(), notification);

				// If we're speaking, dispatch the message to the speaking
				// service.
				if( globalOrOverrideBoolean(R.string.speakMessage, settings, message.getSource(), true) )
				{
					Intent intentData = new Intent(getBaseContext(), SpeakService.class);
					Log.d(TAG, "Speaking text: " + decision.getOutputMessage());
					intentData.putExtra("text", decision.getOutputMessage());
					startService(intentData);
				}
			}
		}
		else if( operation.equals("update") )
		{
			// Clear the notifications for a given source - if there are no
			// unread messages.
			NotifrySource source = NotifrySource.FACTORY.get(this, Long.valueOf(intent.getLongExtra("sourceId", 0)));

			if( source != null )
			{
				this.updateNotificationFor(source);
			}
			else
			{
				// Do it for all sources.
				List<NotifryAccount> accounts = NotifryAccount.FACTORY.listAll(this);

				for( NotifryAccount account : accounts )
				{
					List<NotifrySource> sources = NotifrySource.FACTORY.listAll(this, account.getAccountName());
					for( NotifrySource thisSource : sources )
					{
						this.updateNotificationFor(thisSource);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Set latest event information.
	 * 
	 * @param source
	 *            source
	 * @param message
	 *            message
	 * @return the notification created
	 */
	public Notification setLatestEventInfo( NotifrySource source, NotifryMessage message )
	{
		Notification.Builder builder = new Notification.Builder(getApplicationContext());

		int icon = R.drawable.icon_statusbar;
		builder.setSmallIcon(icon);

		long when = (message == null ? System.currentTimeMillis() : message.getLongTimestamp());
		builder.setWhen(when);

		String contentTitle = (message == null ? getString(R.string.app_name) : message.getTitle());
		String contentText = "";

		int unreadMessagesOfType = NotifryMessage.FACTORY.countUnread(this, source);

		if( unreadMessagesOfType == 1 && message != null )
		{
			// Only one message of this type. Set the title to be the message's
			// title, and then
			// content to be the message itself.
			contentTitle = message.getTitle();
			contentText = message.getMessage();
		}
		else
		{
			// More than one message. Instead, the title is the source name,
			// and the content is the number of unseen messages.
			contentTitle = source.getTitle();
			contentText = String.format(Locale.getDefault(), "%d unseen messages", Integer.valueOf(unreadMessagesOfType));
		}

		// Generate the intent to go to that message list.
		Intent notificationIntent = new Intent(this, MessageList.class);
		// notificationIntent.putExtra("sourceId", source.getId());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the notification data.
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(contentIntent);

		return builder.getNotification();
	}

	private void updateNotificationFor( NotifrySource source )
	{
		if( NotifryMessage.FACTORY.countUnread(this, source) == 0 )
		{
			this.notificationManager.cancel(source.getNotificationId());
		}
		else
		{
			// Change it to the real number of read messages.
			Notification notification = setLatestEventInfo(source, null);
			this.notificationManager.notify(source.getNotificationId(), notification);
		}
	}

	private static final String TAG = "Notifry";

	private NotificationManager notificationManager = null;
}
