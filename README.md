# gdx-gameservices

Game services wrapper for [libgdx](https://github.com/libgdx/libgdx).

Currently supports **Google Play Games** on desktop and android and **Game Center** on iOS (using mobidevelop's roboVM fork).

## Setup

Your project needs libgdx >= 1.10.0.

Add following **bold** parts into your _build.gradle_ file:

<pre>
    allprojects {
        ext {
            <b>gdxGameservicesVersion = '3.0.0'</b>
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

Most methods take as result an AsyncServiceResult<T> object, exhibiting three methods: onCompletion(...), onSuccess(...) and onError(...). These methods accept a Listener where you will be able to use the result of your call.

#### Demo usage

* [How to handle Authentication.](demo-core/src/main/java/games/spooky/gdx/gameservices/demo/AuthenticationDemoTable.java) 
* [How to get and submit Achievements.](demo-core/src/main/java/games/spooky/gdx/gameservices/demo/AchievementsDemoTable.java)
* [How to fetch leaderboard entries and submit new ones.](demo-core/src/main/java/games/spooky/gdx/gameservices/demo/LeaderboardsDemoTable.java)
* [How to download saved game data and upload new ones.](demo-core/src/main/java/games/spooky/gdx/gameservices/demo/SavedGamesDemoTable.java)

## Service-specific shenanigans

* [Google Play Games (android)](googleplay-android/README.md)
* [Google Play Games (desktop)](googleplay-desktop/README.md)
* [Game Center (ios - roboVM)](gamecenter/README.md)

## Feature support

| Platform | Connection | Achievements | Leaderboard | Saved games |
| :--- | :---: | :---: | :---: | :---: |
| Google Play Games (android) | ✓ | ✓ | ✓ | ✓ |
| Google Play Games (desktop) | ✓ | ✓ | ✓ | ✓ |
| Game Center (ios - roboVM)  | ✓ | ✓ | ✓ | ✓ |
