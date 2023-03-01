# gdx-gameservices - Google Play Games (android)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup) and fill your AndroidManifest.xml file with correct information:
    
    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
        <uses-sdk
            android:minSdkVersion="21"
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
        GameServicesHandler services;
        public MyAwesomeGame(GameServicesHandler services) {
            super();
            this.services = services;
        }
        ...
    }

Android initialization is as follows:

    public class MyAwesomeGameAndroid extends AndroidApplication {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ...
              // Create handler
            GooglePlayServicesHandler googlePlay = new GooglePlayServicesHandler();
            
            // Create game
            MyAwesomeGame game = new MyAwesomeGame(googlePlay);

            initialize(game, new AndroidApplicationConfiguration());
            
            googlePlay.setContext(this);
        }
    }

## Specificities

* This library uses Google's _play-services-games-v2_ version 17.0.0. If a different version is needed, fork the project, bump the version to your liking and you should be good to go.
