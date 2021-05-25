# gdx-gameservices

Game services wrapper for [libgdx](https://github.com/libgdx/libgdx). An alternative to the excellent [gdx-gamesvcs](https://github.com/MrStahlfelge/gdx-gamesvcs) library from MrStahlfelge, check it out!

Currently supports **Google Play Games** on desktop and android, **Amazon GameCircle** on android, **Game Center** on iOS (using mobidevelop's roboVM fork) and **Playtomic** on every platform.

## Setup

Your project needs libgdx >= 1.9.4.

Add following **bold** parts into your _build.gradle_ file:

<pre>
    allprojects {
        ext {
            <b>gdxGameservicesVersion = '1.2.1'</b>
        }
    }

    ...

    project(":core") {

        ...

        dependencies {
            ...
            <b>compile "games.spooky.gdx:gdx-gameservices:$gdxGameservicesVersion"</b>
        }
    }

    // For Google Play Games on desktop
    project(":desktop") {

        ...

        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-gameservices-googleplay-desktop:$gdxGameservicesVersion"</b>
        }
    }

    // For Google Play Games on Android
    project(":android") {

        ...

        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-gameservices-googleplay-android:$gdxGameservicesVersion"</b>
        }
    }

    // For Amazon GameCircle on Android
    project(":android") {

        ...

        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-gameservices-gamecircle:$gdxGameservicesVersion"</b>
        }
    }

    // For Game Center on iOS (roboVM)
    project(":ios") {

        ...

        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-gameservices-gamecenter:$gdxGameservicesVersion"</b>
        }
    }
</pre>

## Usage

#### General word

The asynchronous aspect here is handled through the use of callback methods (say, a la Javascript) which may, by the way, be the one important difference with MrStahlfelge's implementation that uses a listener instead.

Most methods take as last parameter a ServiceCallback<T> object, exhibiting two methods: onSuccess(...) and onFailure(...). Those are the methods where you will be able to use the results of your calls.

#### Connection

    // Assuming you created a platform-specific ConnectionHandler
    ConnectionHandler myConnectionHandler;

    // Connect
    myConnectionHandler.login(new ServiceCallback<Void>() {
        @Override
        public void onSuccess(Void result, ServiceResponse response) {
        	// Get player id and name
            Gdx.app.log("gdx-gameservices", "Welcome back! [" + myConnectionHandler.getPlayerId() + "] " + myConnectionHandler.getPlayerName());
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to login: " + response.getErrorMessage());
        }
    });

    // Check connection -- Will probably return false as connection is asynchronous (and potentially long)
    Gdx.app.log("gdx-gameservices", "Am I logged yet? " + myConnectionHandler.isLoggedIn());

    ...

    // Disconnect
    myConnectionHandler.logOut();

#### Achievements

    // Assuming you created a platform-specific AchievementsHandler
    AchievementsHandler myAchievementsHandler;

    // Get achievements
    myAchievementsHandler.getAchievements(new ServiceCallback<Iterable<Achievement>>() {
        @Override
        public void onSuccess(Iterable<Achievement> result, ServiceResponse response) {
            for (Achievement achievement : result)
                if (achievement.getState() == AchievementState.Unlocked)
                    Gdx.app.log("gdx-gameservices", "Achievement unlocked: " + achievement.getName());
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to fetch achievements: " + response.getErrorMessage());
        }
    });

    ...

    // Unlock some achievement
    final String achievementId = "<achievement-id>";
    myAchievementsHandler.unlockAchievement(achievementId, new ServiceCallback<Void>() {
        @Override
        public void onSuccess(Void result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Achievement unlocked: " + achievementId);
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to unlock achievement " + achievementId + ": " + response.getErrorMessage());
        }
    });

#### Leaderboards

    // Assuming you created a platform-specific LeaderboardsHandler
    LeaderboardsHandler myLeaderboardsHandler;

    // Get current user score
    LeaderboardOptions options = null;	// We stick to default options here
    myLeaderboardsHandler.getPlayerScore("<leaderboard-id>", options, new ServiceCallback<LeaderboardEntry>() {
        @Override
        public void onSuccess(LeaderboardEntry result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Today's rank: " + result.getRank());
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to get leaderboard entry: " + response.getErrorMessage());
        }
    });

    ...

    // Get scores
    // Starting from the bottom, friends only
    LeaderboardOptions options = new LeaderboardOptions();
    options.sort = LeaderboardOptions.Sort.Bottom;
    options.collection = LeaderboardOptions.Collection.Friends;
    myLeaderboardsHandler.getScores("<leaderboard-id>", options, new ServiceCallback<Iterable<Achievement>>() {
        @Override
        public void onSuccess(Iterable<LeaderboardEntry> result, ServiceResponse response) {
            for (LeaderboardEntry entry : result)
                Gdx.app.log("gdx-gameservices", entry.getPlayerName() + " has score " + entry.getScore());
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to get leaderboard entries: " + response.getErrorMessage());
        }
    });

    ...

    // Submit new highscore
    myLeaderboardsHandler.submitScore("<leaderboard-id>", 123, new ServiceCallback<Void>() {
        @Override
        public void onSuccess(Void result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Score successfully submitted");
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to submit score: " + response.getErrorMessage());
        }
    });

#### Saved games

    // Assuming you created a platform-specific SavedGamesHandler
    SavedGamesHandler mySavedGamesHandler;

    // Get metadata for all saved games
    mySavedGamesHandler.getSavedGames(new ServiceCallback<Iterable<SavedGame>>() {
        @Override
        public void onSuccess(Iterable<SavedGame> result, ServiceResponse response) {
            int count = 0;
            for (SavedGame save : result)
                count ++;
            Gdx.app.log("gdx-gameservices", count + " game saves retrieved");
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to get saved games: " + response.getErrorMessage());
        }
    });

    ...

    // Load saved data for a game
    GameWorldSave save;	// Could be one of those retrieved earlier
    mySavedGamesHandler.loadSavedGameData(save, new ServiceCallback<byte[]>() {
        @Override
        public void onSuccess(byte[] result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Loaded raw game data of size " + result.length);
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to load saved game data: " + response.getErrorMessage());
        }
    });

    ...

    // Submit new game save
    GameWorldSave save;	// Time passed and game evolved
    byte[] saveData;	// This is roughly a binary representation of game state
    mySavedGamesHandler.submitScore(save, saveData, new ServiceCallback<Void>() {
        @Override
        public void onSuccess(Void result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Game save successfully submitted");
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to submit game save: " + response.getErrorMessage());
        }
    });

    ...

    // Delete game save
    GameWorldSave save;	// Could be one of those retrieved earlier
    mySavedGamesHandler.deleteSavedGame(save, new ServiceCallback<Void>() {
        @Override
        public void onSuccess(Void result, ServiceResponse response) {
            Gdx.app.log("gdx-gameservices", "Game save successfully deleted");
        }
        @Override
        public void onFailure(ServiceResponse response) {
            Gdx.app.error("gdx-gameservices", "Unable to delete game save: " + response.getErrorMessage());
        }
    });

## Service-specific shenanigans

* [Google Play Games (android)](gdx-gameservices-googleplay-android/README.md)
* [Google Play Games (desktop)](gdx-gameservices-googleplay-desktop/README.md)
* [Amazon GameCircle (android)](gdx-gameservices-gamecircle/README.md)
* [Game Center (ios - roboVM)](gdx-gameservices-gamecenter/README.md)
* [Playtomic](gdx-gameservices-playtomic/README.md)

## Feature support

| Platform | Connection | Achievements | Leaderboard | Saved games |
| :--- | :---: | :---: | :---: | :---: |
| Google Play Games (android) | ✓ | ✓ | ✓ | ✓ |
| Google Play Games (desktop) | ✓ | ✓ | ✓ | ✓ |
| Amazon GameCircle (android) | ✓ | ✓ | ✓ | ✓ |
| Game Center (ios - roboVM)  | ✓ | ✓ | ✓ | ✓ |
| Playtomic                   | ✓ | ✓ | ✓ | ✕ |

## Roadmap

* Refine API
* A functional cross-platform cross-service demo
* Support m0ar platforms and services!
