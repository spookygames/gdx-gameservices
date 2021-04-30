# gdx-gameservices - Game Center (iOS - roboVM)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup) and fill your robovm.xml file with correct information (_CloudKit_ only needed for cloud save support).

    <config>
        ...
        <frameworks>
            ...
            <framework>SafariServices</framework>
            <framework>CoreBluetooth</framework>
            <framework>CoreTelephony</framework>
            <framework>CoreMedia</framework>
            <framework>CoreVideo</framework>
            <framework>CoreMotion</framework>
            <framework>MessageUI</framework>
            <framework>CFNetwork</framework>
            <framework>MobileCoreServices</framework>
            <framework>GLKit</framework>
            <framework>GameKit</framework>
            <framework>CloudKit</framework>
        </frameworks>
    </config>

Then, create Game Center handler in your platform-specific code. For general advice about such initialization, please refer to [libGDX documentation](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

Following such core game:
    
    public class MyAwesomeGame implements ApplicationListener {
        ConnectionHandler services;
        public MyAwesomeGame(ConnectionHandler services) {
            super();
            this.services = services;
        }
        ...
    }

iOS initialization code is quite straightforward:

    public class MyAwesomeGameIOS extends IOSApplication.Delegate {
        @Override
        protected IOSApplication createApplication() {
              // Create handler
            GameCenterServicesHandler gamecenter = new GameCenterServicesHandler();
            
            // Create game
            MyAwesomeGame game = new MyAwesomeGame(gamecenter);
            
            // And voila
            return new IOSApplication(game, new IOSApplicationConfiguration());
        }
    }

## Idiosyncrasies

* Uses mobidevelop's roboVM version 2.3.2.
* logOut() not implemented
* getPlayerAvatarUrl() returns `null`
* Unable to use sorting on leaderboard entries

