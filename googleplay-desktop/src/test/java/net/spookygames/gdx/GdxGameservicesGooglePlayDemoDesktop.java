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
package net.spookygames.gdx;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.demo.*;
import games.spooky.gdx.gameservices.googleplay.desktop.GooglePlayServicesHandler;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class GdxGameservicesGooglePlayDemoDesktop {
	public static void main(String[] args) {
		
		DesktopFileChooser fileChooser = new DesktopFileChooser();
		
		GdxGameservicesDemo demo = new GdxGameservicesDemo("Google Play (Desktop)", fileChooser) {
			@Override
			protected ConnectionHandler buildHandler() {
				GooglePlayServicesHandler handler = new GooglePlayServicesHandler() {
					@Override
					protected int authenticationPort() {
						return super.authenticationPort();	// TODO Set custom redirection port here if needed
					}

					@Override
					protected void debug(String text) {
						logDebug(text);
					}

					@Override
					protected void error(String error) {
						logError(error);
					}
				};

				// TODO Input real values here
				String appName = "demo-app";
				String clientSecretsJson = "{ \"installed\": { \"client_id\": \"abcdefgh.apps.googleusercontent.com\", \"client_secret\": \"loremipsumsecret\" }	}";

				try {
					handler.initialize(appName, new ByteArrayInputStream(clientSecretsJson.getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return handler;
			}
			
			@Override
			protected ConnectionDemoTable buildConnectionTable(Skin skin) {
				return super.buildConnectionTable(skin);
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
				
		new LwjglApplication(demo, "", 800, 600);
	}
}
