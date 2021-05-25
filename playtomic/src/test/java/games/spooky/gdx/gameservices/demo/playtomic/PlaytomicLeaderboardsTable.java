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
import games.spooky.gdx.gameservices.demo.LeaderboardsDemoTable;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;
import games.spooky.gdx.gameservices.playtomic.PlaytomicServicesHandler;

public class PlaytomicLeaderboardsTable extends LeaderboardsDemoTable {

	public PlaytomicLeaderboardsTable(Skin skin) {
		super(skin);
	}
	
	@Override
	public void initialize(final LeaderboardsHandler leaderboards, final Preferences prefs) {
		
		final PlaytomicServicesHandler playtomic = (PlaytomicServicesHandler) leaderboards;
		Skin skin = getSkin();
		
		// Save and list
		TextButton saveAndListButton = new TextButton("Save and list (Playtomic special)", skin);
		saveAndListButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				String id = leaderboardIdTextField.getText();
				String scoreText = leaderboardScoreTextField.getText();

				prefs.putString("playtomic_leaderboardid", id);
				prefs.putString("playtomic_leaderboardscore", scoreText);
				prefs.flush();

				try {
					long score = Long.parseLong(scoreText);

					log("Save score " + score + " and list for leaderboard " + id + "...");

					playtomic.saveAndList(id, score, null, new ServiceCallback<Iterable<LeaderboardEntry>>() {
						@Override
						public void onSuccess(Iterable<LeaderboardEntry> result, ServiceResponse response) {
							log(response.getErrorMessage());
							for (LeaderboardEntry entry : result)
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
		
		super.initialize(leaderboards, prefs);
		
		row();
		add(saveAndListButton);
	}

}
