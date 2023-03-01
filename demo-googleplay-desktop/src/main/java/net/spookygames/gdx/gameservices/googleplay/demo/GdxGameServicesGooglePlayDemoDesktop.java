/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.spookygames.gdx.gameservices.googleplay.demo;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import games.spooky.gdx.gameservices.GameServicesHandler;
import games.spooky.gdx.gameservices.demo.AchievementsDemoTable;
import games.spooky.gdx.gameservices.demo.AuthenticationDemoTable;
import games.spooky.gdx.gameservices.demo.GdxGameServicesDemo;
import games.spooky.gdx.gameservices.demo.LeaderboardsDemoTable;
import games.spooky.gdx.gameservices.demo.SavedGamesDemoTable;
import games.spooky.gdx.gameservices.googleplay.GooglePlayServicesHandler;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

public class GdxGameServicesGooglePlayDemoDesktop {
	public static void main(String[] args) {

		DesktopFileChooser fileChooser = new DesktopFileChooser();

		// Input real values here
		String applicationName = "MyGame";
		String clientId = "123456-a5cdf45df6.apps.googleusercontent.com";
		String clientSecret = "abc123ABC";

		GdxGameServicesDemo demo = new GdxGameServicesDemo("Google Play (Android)", fileChooser) {
			@Override
			protected GameServicesHandler buildHandler(Preferences preferences) {
				GooglePlayServicesHandler handler = new GooglePlayServicesHandler() {
					@Override
					protected int authenticationPort() {
						return 49410;	// Set custom redirection port here if needed
					}

					@Override
					protected void error(String error) {
						logError(error);
					}
				};

				handler.initialize(applicationName, clientId, clientSecret);
				return handler;
			}

			@Override
			protected AuthenticationDemoTable buildAuthenticationTable(Skin skin) {
				return super.buildAuthenticationTable(skin);
			}

			@Override
			protected AchievementsDemoTable buildAchievementsTable(Skin skin) {
				return super.buildAchievementsTable(skin);
			}

			@Override
			protected LeaderboardsDemoTable buildLeaderboardsTable(Skin skin) {
				return super.buildLeaderboardsTable(skin);
			}

			@Override
			protected SavedGamesDemoTable buildSavedGamesTable(Skin skin) {
				return super.buildSavedGamesTable(skin);
			}
		};

		new LwjglApplication(demo, applicationName, 1024, 768);
	}
}
