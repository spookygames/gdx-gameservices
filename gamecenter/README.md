# gdx-gameservices - Game Center (iOS - roboVM)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup) and fill your robovm.xml file with correct information (_CloudKit_ only needed for cloud save support).

    <config>
        ...
        <iosEntitlementsPList>entitlements.plist</iosEntitlementsPList>
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

With a new or existing file `entitlements.plist` containing at least:

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>com.apple.developer.game-center</key>
	<boolean>true</boolean>
</dict>
</plist>
```

Then, create Game Center handler in your platform-specific code. For general advice about such initialization, please refer to [libGDX documentation](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

Following such core game:
    
    public class MyAwesomeGame implements ApplicationListener {
        GameServicesHandler services;
        public MyAwesomeGame(GameServicesHandler services) {
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

## Specificities

* Uses mobidevelop's roboVM version 2.3.14.
* Unable to center leaderboard entries on player

