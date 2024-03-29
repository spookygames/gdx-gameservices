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
package games.spooky.gdx.gameservices.demo;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import games.spooky.gdx.gameservices.ServiceCompletionCallback;
import games.spooky.gdx.gameservices.ServiceError;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;

public class LeaderboardsDemoTable extends GdxGameservicesDemoTable {

	protected TextField leaderboardIdTextField;
	protected TextField leaderboardScoreTextField;
	
	public LeaderboardsDemoTable(Skin skin) {
		super(skin);
	}
	
	public void initialize(final LeaderboardsHandler leaderboards, final Preferences prefs) {
		Skin skin = getSkin();
		
		// Leaderboard ID
		leaderboardIdTextField = new TextField(prefs.getString("leaderboards_leaderboardid", ""), skin);
		leaderboardIdTextField.setMessageText("Leaderboard ID");
		
		// Get player score
		TextButton playerScoreButton = new TextButton("Get player score", skin);
		playerScoreButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final String id = leaderboardIdTextField.getText();

				prefs.putString("leaderboards_leaderboardid", id);
				prefs.flush();

				log("Get player score for leaderboard " + id + "...");
				try {
					leaderboards.getPlayerScore(id, null).onCompletion(new ServiceCompletionCallback<LeaderboardEntry>() {
						@Override
						public void onSuccess(LeaderboardEntry result) {
							log("Received player score for leaderboard " + id);
							log(result.toString());
						}

						@Override
						public void onError(ServiceError error) {
							error("Leaderboard error: " + error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Get scores
		TextButton scoresButton = new TextButton("Get scores", skin);
		scoresButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final String id = leaderboardIdTextField.getText();

				prefs.putString("leaderboards_leaderboardid", id);
				prefs.flush();

				log("Get scores for leaderboard " + id + "...");
				try {
					leaderboards.getScores(id, null).onCompletion(new ServiceCompletionCallback<Iterable<LeaderboardEntry>>() {
						@Override
						public void onSuccess(Iterable<LeaderboardEntry> result) {
							log("Received scores for leaderboard " + id);
							for (LeaderboardEntry entry : result)
								log(entry.toString());
						}

						@Override
						public void onError(ServiceError error) {
							error("Leaderboard error: " + error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Score value
		leaderboardScoreTextField = new TextField(prefs.getString("leaderboards_leaderboardscore", ""), skin);
		leaderboardScoreTextField.setMessageText("Leaderboard score");
		leaderboardScoreTextField.setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
		
		// Submit score
		TextButton submitScoreButton = new TextButton("Submit score", skin);
		submitScoreButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final String id = leaderboardIdTextField.getText();
				String scoreText = leaderboardScoreTextField.getText();

				prefs.putString("leaderboards_leaderboardid", id);
				prefs.putString("leaderboards_leaderboardscore", scoreText);
				prefs.flush();
				try {
					final long score = Long.parseLong(scoreText);

					log("Submit score " + score + " for leaderboard " + id + "...");

					leaderboards.submitScore(id, score).onCompletion(new ServiceCompletionCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							log("Submitted score " + score + " for leaderboard " + id);
						}

						@Override
						public void onError(ServiceError error) {
							error("Leaderboard error: " + error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		row();
		add(leaderboardIdTextField);
		
		row();
		add(playerScoreButton);
		
		row();
		add(scoresButton);
		
		row();
		add(leaderboardScoreTextField);
		
		row();
		add(submitScoreButton);
	}
	
}
