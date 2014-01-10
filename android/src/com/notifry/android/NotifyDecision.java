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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.notifry.android.database.NotifryMessage;

/**
 * Notify decision.
 */
public class NotifyDecision
{
	/**
	 * Get the notification message.
	 * 
	 * @return the message
	 */
	public NotifryMessage getMessage()
	{
		return message;
	}

	/**
	 * Get the output message.
	 * 
	 * @return the output message
	 */
	public String getOutputMessage()
	{
		return outputMessage;
	}

	/**
	 * Return <code>true</code> if should be notified.
	 * 
	 * @return <code>true</code> if should be notified
	 */
	public Boolean getShouldNotify()
	{
		return shouldNotify;
	}

	/**
	 * Set the notification message.
	 * 
	 * @param message
	 *            the message
	 */
	public void setMessage( NotifryMessage message )
	{
		this.message = message;
	}

	/**
	 * Set the output message.
	 * 
	 * @param outputMessage
	 *            the message
	 */
	public void setOutputMessage( String outputMessage )
	{
		this.outputMessage = outputMessage;
	}

	/**
	 * Set if should be notified.
	 * 
	 * @param shouldNotify
	 *            Set to <code>true</code> if should be notified
	 */
	public void setShouldNotify( Boolean shouldNotify )
	{
		this.shouldNotify = shouldNotify;
	}

	/**
	 * Determine if we should notify about this message or not.
	 * 
	 * @param context
	 *            the context
	 * @param message
	 *            the message
	 * @return the decision
	 */
	public static NotifyDecision shouldNotify( Context context, NotifryMessage message )
	{
		NotifyDecision decision = new NotifyDecision();
		decision.setShouldNotify(message.getSource().getLocalEnabled());

		// Set the message that should be spoken.
		decision.setMessage(message);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String format = settings.getString("speakFormat", "%t. %m");

		StringBuffer buffer = new StringBuffer(format);
		formatString(buffer, "%t", message.getTitle());
		formatString(buffer, "%m", message.getMessage());
		formatString(buffer, "%s", message.getSource().getTitle());
		formatString(buffer, "%a", message.getSource().getAccountName());
		formatString(buffer, "%%", "%");
		decision.setOutputMessage(buffer.toString());

		return decision;
	}

	/**
	 * Replace a keyword with a value every time it appears in the buffer.
	 * 
	 * @param buffer
	 * @param keyword
	 * @param value
	 */
	private static void formatString( StringBuffer buffer, String keyword, String value )
	{
		int position = -1;
		while( (position = buffer.indexOf(keyword)) != -1 )
		{
			buffer.replace(position, position + keyword.length(), value);
		}
	}

	private Boolean shouldNotify;

	private NotifryMessage message;

	private String outputMessage;
}
