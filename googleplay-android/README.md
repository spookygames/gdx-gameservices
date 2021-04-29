# gdx-gameservices - Google Play Games (android)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup) and fill your AndroidManifest.xml file with correct information (**bold lines**):
    
    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
        <uses-sdk
            android:minSdkVersion="9"
            ... />
    
        <uses-permission android:name="android.permission.INTERNET" />
    
        <application>
        
            <meta-data android:name="com.google.android.gms.games.APP_ID" android:value="@string/app_id" />
            <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version"/>
       
            ...
        </application>
    
    </manifest>

Then, create Google Play Games handler in your platform-specific code. For general advice about such initialization, please refer to [libGDX documentation](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

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
            GooglePlayServicesHandler googlePlay = new GooglePlayServicesHandler();
            boolean useCloudSaves = true;
            
            // Create game
            MyAwesomeGame game = new MyAwesomeGame(googlePlay);
            
            // Initialize view and handler
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            
            View view = initializeForView(game, new AndroidApplicationConfiguration());
            googlePlay.setContext(this, view, useCloudSaves);
            
            setContentView(graphics.getView(), createLayoutParams());
        }
    }

## Idiosyncrasies

* This library uses Google's _play-services-games_ version 11.0.4. If a different version is needed, fork the project, bump the version to your liking and you should be good to go.
* Sort.Bottom is not available for leaderboard entries.
