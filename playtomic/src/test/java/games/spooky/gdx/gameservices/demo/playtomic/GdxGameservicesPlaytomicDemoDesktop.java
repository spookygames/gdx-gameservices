/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2018 Spooky Games
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
package games.spooky.gdx.gameservices.demo.playtomic;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.demo.AchievementsDemoTable;
import games.spooky.gdx.gameservices.demo.ConnectionDemoTable;
import games.spooky.gdx.gameservices.demo.GdxGameservicesDemo;
import games.spooky.gdx.gameservices.demo.LeaderboardsDemoTable;
import games.spooky.gdx.gameservices.demo.SavedGamesDemoTable;
import games.spooky.gdx.gameservices.playtomic.PlaytomicNet;
import games.spooky.gdx.gameservices.playtomic.PlaytomicServicesHandler;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

public class GdxGameservicesPlaytomicDemoDesktop {
	public static void main(String[] args) throws Exception {
		
		DesktopFileChooser fileChooser = new DesktopFileChooser();
		
		GdxGameservicesDemo demo = new GdxGameservicesDemo("Playtomic", fileChooser) {
			@Override
			protected ConnectionHandler buildHandler() {
				return new PlaytomicServicesHandler(new PlaytomicNet() {
					@Override
					protected void debug(String text) {
						logDebug(text);
					}
					
					@Override
					protected void error(Throwable error) {
						logError(error.getLocalizedMessage());
					}
				});
			}
			
			@Override
			protected ConnectionDemoTable buildConnectionTable(Skin skin) {
				return new PlaytomicConnectionTable(skin);
			}
			
			@Override
			protected AchievementsDemoTable buildAchievementsTable(Skin skin) {
				return new PlaytomicAchievementsTable(skin);
			}
			
			@Override
			protected LeaderboardsDemoTable buildLeaderboardsTable(Skin skin) {
				return new PlaytomicLeaderboardsTable(skin);
			}
			
			@Override
			protected SavedGamesDemoTable buildSavedGamesTable(Skin skin) {
				return super.buildSavedGamesTable(skin);
			}
		};
				
		new LwjglApplication(demo, "", 800, 600);
	}
}
