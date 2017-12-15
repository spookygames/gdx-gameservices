# gdx-gameservices - Amazon GameCircle (android)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup) and fill your AndroidManifest.xml file with correct information, as specified from [Amazon documentation](https://developer.amazon.com/docs/gamecircle/initialize-android.html#StepThree).
Do not forget to replace **YOUR_PACKAGE_NAME_HERE** with your correct package name!
    
    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
        ...
    
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
        <application>
        
            <activity
                android:name="com.amazon.ags.html5.overlay.GameCircleUserInterface"
                android:theme="@style/GCOverlay" android:hardwareAccelerated="false">
            </activity>
            
            <activity
                android:name="com.amazon.identity.auth.device.authorization.AuthorizationActivity"
                android:theme="@android:style/Theme.NoDisplay"
                android:allowTaskReparenting="true"
                android:launchMode="singleTask">
             
                <intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data android:host="YOUR_PACKAGE_NAME_HERE" android:scheme="amzn" />
                </intent-filter>
            </activity>
             
            <activity
                android:name="com.amazon.ags.html5.overlay.GameCircleAlertUserInterface"
                android:theme="@style/GCAlert" android:hardwareAccelerated="false">
            </activity>
             
            <receiver
                android:name="com.amazon.identity.auth.device.authorization.PackageIntentReceiver"
                android:enabled="true">
             
                <intent-filter>
                    <action android:name="android.intent.action.PACKAGE_INSTALL" />
                    <action android:name="android.intent.action.PACKAGE_ADDED" />
                    <data android:scheme="package" />
                </intent-filter>
            </receiver>
            
            ...
            
        </application>
    
    </manifest>

Then, create a GameCircle handler in your platform-specific code. For general advice about such initialization, please refer to [libGDX documentation](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

Following such core game:
    
    public class MyAwesomeGame implements ApplicationListener {
        ConnectionHandler services;
        public MyAwesomeGame(ConnectionHandler services) {
            super();
            this.services = services;
        }
        ...
    }

Android initialization code needs to use the View object.

    public class MyAwesomeGameAndroid extends AndroidApplication {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ...
              // Create handler
            GameCircleServicesHandler gamecircle = new GameCircleServicesHandler();
            boolean useAchievements = true;
            boolean useLeaderboards = true;
            boolean useCloudSaves = true;
            gamecircle.setContext(this, useAchievements, useLeaderboards, useCloudSaves);
            
            // Create game
            MyAwesomeGame game = new MyAwesomeGame(gamecircle);
            
            initialize(game, new AndroidApplicationConfiguration());
        }
    }

## Idiosyncrasies

* LeaderboardOptions.sort and LeaderboardOptions.itemsPerPage are not available for leaderboard entries.
* Deleting saved games is not implemented as there is no direct way to perform that with Amazon Whispersync.
