# Notifry 'O' #

## Description ##

This project is a customized version of [Notifry](https://notifrier.appspot.com),
a push notification service for Android.

**This repository is for reference only, I cannot provide support for setup, development and deployment of this project.**

Should you need support, please use [Newtifry](https://newtifry.appspot.com/) or
[NewtifryPro](https://newtifry.appspot.com/page/pro).

### Legacy Code ###

[Notifry](https://notifrier.appspot.com) service has been shut down on June 6th, 2013.

The original code is open source, and available via the project on [Google Code](http://code.google.com/p/notifry).

### Modifications ###

This project includes the following modifications:

* Changed application name.
* Changed application icon.
* Changed button style.
* Removed dependency to *ActionBarSherlock*.
* Added Recent Messages screen as main activity, for quick access as an activity shortcut.

## Setup ##

### Prerequisites ###

1. Create a Google App Engine project at https://appengine.google.com/ and copy "*Google APIs Console Project Number*".
2. Get the *API Key* for your AppEngine project from the [Google API Console](https://code.google.com/apis/console), *API Access* / *Simple API Key*.
3. Install [Eclipse](http://www.eclipse.org).
4. Install [PyDev plugin](https://developers.google.com/appengine/articles/eclipse) to edit and deploy your Google App Engine application.
5. Install [Android Development Toolkit (ADT)](http://developer.android.com/sdk/installing/installing-adt.html) plugin to edit, compile and create the Android APK.
6. Import the Notifry 'O' projects in Eclipse.

### AppEngine ###

1. Change `backend_url` in source file `res/values/backend.xml`.
2. Deploy the project
3. Visit `https://your-app-id.appspot.com/admin/`, add your *API Key* using *Create AC2DM Token*.

There are also a few `XXXXX` that may need updating: email address, Google Analytics...

### Android ###

1. Change `application:` in source file `app.yaml`.
2. Generate the APK and install it to your device.

Or you can activate USB debugging on your phone, plug it to the computer and run AndroidManifest.xml as an Android Application from Eclipse.

## License ##

Just like the original [Notifry](https://notifrier.appspot.com) project,
this project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Disclaimer ##

Products and company names mentioned herein may be trademarks or registered trademarks of their respective owners.
