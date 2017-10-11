# gdx-gameservices - Google Play Games (desktop)

## Setup

First, add correct dependency to your gradle file as described [here](../README.md#setup).

You also have to create a specific json file with your client id and secret:

    {
        "installed":
        {
            "client_id": "<your-app-id>.apps.googleusercontent.com",
            "client_secret": "<your-secret>"
        }
    }

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

Desktop initialization code is rather straightforward, mind the call to initialize() though:

    public class MyAwesomeGameDesktop {
        public static void main(String[] args) {
            // Create handler
            DesktopGooglePlayServicesHandler googlePlay = new DesktopGooglePlayServicesHandler();
            
            // Define metadata
            String applicationName = "MyAwesome";
            FileHandle clientSecretFile = ...; // Get FileHandle with json content written above
            String dataStoreDirectory = "my_awesome_game";
            
            // Create game
            MyAwesomeGame game = new MyAwesomeGame(googlePlay);
            googlePlay.initialize(applicationName, clientSecretFile, dataStoreDirectory);
            
            // Start
            new LwjglApplication(game, new LwjglApplicationConfiguration());
        }
    }

Instead of a credentials file, you may also directly provide client id and secret as String to another overload of the initialize() method. Do as you please.

## Idiosyncrasies

* This library uses Google API version 1.22.0.
* Strong inspiration from the implementation on MrStahlfelge's repo (by mgsx-dev), thank you both!
* Browser-based authentication
* Sort.Bottom is not available for leaderboard entries
* SavedGame.getDeviceName() returns an empty String
