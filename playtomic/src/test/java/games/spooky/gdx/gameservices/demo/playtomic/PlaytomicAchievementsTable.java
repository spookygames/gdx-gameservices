/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2021 Spooky Games
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

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import games.spooky.gdx.gameservices.ServiceCallback;
import games.spooky.gdx.gameservices.ServiceResponse;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementsHandler;
import games.spooky.gdx.gameservices.demo.AchievementsDemoTable;
import games.spooky.gdx.gameservices.playtomic.PlaytomicServicesHandler;

public class PlaytomicAchievementsTable extends AchievementsDemoTable {

	public PlaytomicAchievementsTable(Skin skin) {
		super(skin);
	}
	
	@Override
	public void initialize(final AchievementsHandler achievements, final Preferences prefs) {
		
		final PlaytomicServicesHandler playtomic = (PlaytomicServicesHandler) achievements;
		Skin skin = getSkin();
		
		// All achievements
		TextButton allAchievementsButton = new TextButton("Get all achievements (Playtomic special)", skin);
		allAchievementsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					log("Get all achievements...");

					playtomic.getAllAchievements(new ServiceCallback<Iterable<Achievement>>() {
						@Override
						public void onSuccess(Iterable<Achievement> result, ServiceResponse response) {
							log(response.getErrorMessage());
							for (Achievement entry : result)
								log(entry.toString());
						}
						
						@Override
						public void onFailure(ServiceResponse response) {
							error(response.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Stream achievements
		TextButton streamAchievementsButton = new TextButton("Stream achievements (Playtomic special)", skin);
		streamAchievementsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					log("Stream achievements...");

					playtomic.streamAchievements(new ServiceCallback<Iterable<Object>>() {
						@Override
						public void onSuccess(Iterable<Object> result, ServiceResponse response) {
							log(response.getErrorMessage());
							for (Object entry : result)
								log(entry.toString());
						}
						
						@Override
						public void onFailure(ServiceResponse response) {
							error(response.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		super.initialize(achievements, prefs);
		
		row();
		add(allAchievementsButton);
		
		row();
		add(streamAchievementsButton);
	}

}
