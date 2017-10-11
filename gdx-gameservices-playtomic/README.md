# gdx-gameservices - Playtomic

## Setup

You first need a proper Playtomic server, of course. Look at [the official documentation](http://playtomic.org/) for Playtomic or [the github repository](https://github.com/playtomic/apiserver) for more information.

Then you have to add correct dependency to your gradle file as described [here](../README.md#setup).

Playtomic handler does not depend on any platform specificity, so you may create it from your _core_ project if you wish. **You need to initialize it with proper public/private keys**.
    
    public class MyAwesomeGame implements ApplicationListener {
        PlaytomicServicesHandler services;
        public MyAwesomeGame() {
            super();
            this.playtomic = new PlaytomicServicesHandler();
            
            // Initialize keys
            playtomic.setServer("url_of_playtomic_server");  // "apiurl" from the docs
            playtomic.setPublicKey("public_key");  // "publicKey"
            playtomic.setPrivateKey("private_key");  // "privateKey"
            
            // Optional: set source for leaderboards
            playtomic.setPlayerSource("my_platform_source");
        }
        ...
    }

## Idiosyncrasies

* Only returns unlocked achievements
* Sort.CenteredOnPlayer is not available for leaderboard entries
* Does not support game saves
