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
package games.spooky.gdx.gameservices.gamecircle;

import android.app.Activity;

import com.amazon.ags.api.AGResponseCallback;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AmazonGamesCallback;
import com.amazon.ags.api.AmazonGamesClient;
import com.amazon.ags.api.AmazonGamesFeature;
import com.amazon.ags.api.AmazonGamesStatus;
import com.amazon.ags.api.achievements.GetAchievementsResponse;
import com.amazon.ags.api.achievements.UpdateProgressResponse;
import com.amazon.ags.api.leaderboards.GetPlayerScoreResponse;
import com.amazon.ags.api.leaderboards.GetScoresResponse;
import com.amazon.ags.api.leaderboards.Score;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.api.overlay.PopUpLocation;
import com.amazon.ags.api.player.Player;
import com.amazon.ags.api.player.RequestPlayerResponse;
import com.amazon.ags.api.whispersync.GameDataMap;
import com.amazon.ags.api.whispersync.WhispersyncClient;
import com.amazon.ags.api.whispersync.model.SyncableString;
import com.amazon.ags.constants.LeaderboardFilter;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.utils.Base64Coder;

import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.PlainServiceResponse;
import games.spooky.gdx.gameservices.ServiceCallback;
import games.spooky.gdx.gameservices.ServiceResponse;
import games.spooky.gdx.gameservices.TransformIterable;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementsHandler;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;
import games.spooky.gdx.gameservices.savedgame.SavedGame;
import games.spooky.gdx.gameservices.savedgame.SavedGamesHandler;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

public class GameCircleServicesHandler implements ConnectionHandler, AchievementsHandler, LeaderboardsHandler, SavedGamesHandler {

	private Activity context;
	private EnumSet<AmazonGamesFeature> featureSet;

	private AmazonGamesClient client = null;
	private boolean connected;
	private String playerId = null;
	private String playerName = null;

	private PopUpLocation popUpLocation = null;

	public AmazonGamesClient getAmazonGamesClient() {
		return client;
	}

	/**
	 * Get the location where GameCircle popups would appear.
	 * Can be null.
	 * @return the location for GameCircle popups
	 */
	public PopUpLocation getPopUpLocation() {
		return popUpLocation;
	}

	/**
	 * Set the location where GameCircle popups would appear.
	 * Can be null, will then default to GameCircle's default (BOTTOM_CENTER).
	 * @param popUpLocation the location for GameCircle popups
	 */
	public void setPopUpLocation(PopUpLocation popUpLocation) {
		this.popUpLocation = popUpLocation;
	}

	// Lifecycle

	public void setContext(final AndroidApplication app) {
		setContext(app, true, true, false);
	}

	public void setContext(final AndroidApplication app, boolean handleAchievements, boolean handleLeaderboards, boolean handleSaves) {
		this.context = app;
		this.featureSet = defineFeatureSet(handleAchievements, handleLeaderboards, handleSaves);

		app.addLifecycleListener(new LifecycleListener() {
			@Override
			public void pause() {
				if (client != null) {
					AmazonGamesClient.release();
					debug("Amazon GameCircle client released for pause");
				}
			}

			@Override
			public void resume() {
				if (client != null) {
					AmazonGamesClient.initialize(context, new AmazonGamesCallback() {
						@Override
						public void onServiceReady(AmazonGamesClient amazonGamesClient) {
							debug("Amazon GameCircle client successfully re-initialized");
						}

						@Override
						public void onServiceNotReady(AmazonGamesStatus amazonGamesStatus) {
							error("Unable to bring back Amazon GameCircle: " + amazonGamesStatus.toString());
						}
					}, featureSet);
				}
			}

			@Override
			public void dispose() {
				app.removeLifecycleListener(this);
			}
		});
	}

	private static EnumSet<AmazonGamesFeature> defineFeatureSet(boolean achievements, boolean leaderboards, boolean saves) {
		EnumSet<AmazonGamesFeature> features = EnumSet.noneOf(AmazonGamesFeature.class);
		if (achievements)
			features.add(AmazonGamesFeature.Achievements);
		if (leaderboards)
			features.add(AmazonGamesFeature.Leaderboards);
		if (saves)
			features.add(AmazonGamesFeature.Whispersync);
		return features;
	}

	// Connection

	@Override
	public boolean isLoggedIn() {
		return client != null && connected;
	}

	@Override
	public void login(final ServiceCallback<Void> callback) {
		if (!isLoggedIn()) {

			AmazonGamesClient.initialize(context, new AmazonGamesCallback() {
				@Override
				public void onServiceReady(AmazonGamesClient amazonGamesClient) {

					client = amazonGamesClient;
					if (popUpLocation != null)
						client.setPopUpLocation(popUpLocation);

					connected = true;

					debug("Amazon GameCircle client successfully initialized, now fetching user name...");

					// Request Alias
					client.getPlayerClient().getLocalPlayer().setCallback(
							new AGResponseCallback<RequestPlayerResponse>() {
								@Override
								public void onComplete(final RequestPlayerResponse response) {
									if (response.isError()) {
										error("Unable to fetch local user name for Amazon GameCircle: " + response.getError());

										if (callback != null)
											callback.onFailure(new GameCircleResponseWrapper(response));
									} else {

										debug("Amazon GameCircle client successfully initialized and logged on");

										Player player = response.getPlayer();
										playerId = player.getPlayerId();
										playerName = player.getAlias();

										if (callback != null)
											callback.onSuccess(null, PlainServiceResponse.success());
									}
								}
							}
					);

				}

				@Override
				public void onServiceNotReady(AmazonGamesStatus amazonGamesStatus) {
					error("Unable to initialize Amazon GameCircle: " + amazonGamesStatus);
					connected = false;

					if (callback != null)
						callback.onFailure(PlainServiceResponse.error(amazonGamesStatus.toString()));
				}
			}, featureSet);
		}
	}

	public void logout() {
		if (isLoggedIn()) {
			AmazonGamesClient.shutdown();
			debug("Amazon GameCircle client shut down for logout");
			client = null;
			playerId = null;
			playerName = null;
		}
	}

	@Override
	public String getPlayerId() {
		return playerId;
	}

	@Override
	public String getPlayerName() {
		return playerName;
	}

	// Achievements

	@Override
	public void getAchievements(final ServiceCallback<Iterable<Achievement>> callback) {
		AGResponseHandle<GetAchievementsResponse> handle = client.getAchievementsClient().getAchievements();
		if (callback != null) {
			handle.setCallback(new AGResponseCallback<GetAchievementsResponse>() {
				@Override
				public void onComplete(GetAchievementsResponse getAchievementsResponse) {
					ServiceResponse response = new GameCircleResponseWrapper(getAchievementsResponse);
					if (getAchievementsResponse.isError()) {
						callback.onFailure(response);
					} else {
						callback.onSuccess(
								new TransformIterable<com.amazon.ags.api.achievements.Achievement, Achievement>(getAchievementsResponse.getAchievementsList()) {
									@Override
									protected Achievement transform(com.amazon.ags.api.achievements.Achievement item) {
										return new GameCircleAchievementWrapper(item);
									}
								}, response);
					}
				}
			});
		}
	}

	@Override
	public void unlockAchievement(String achievementId, final ServiceCallback<Void> callback) {
		AGResponseHandle<UpdateProgressResponse> handle = client.getAchievementsClient().updateProgress(achievementId, 100.0f);
		if (callback != null) {
			handle.setCallback(new AGResponseCallback<UpdateProgressResponse>() {
				@Override
				public void onComplete(UpdateProgressResponse updateProgressResponse) {
					ServiceResponse response = new GameCircleResponseWrapper(updateProgressResponse);
					if (updateProgressResponse.isError()) {
						callback.onFailure(response);
					} else {
						callback.onSuccess(null, response);
					}
				}
			});
		}
	}

	// Leaderboards

	@Override
	public void getPlayerScore(String leaderboardId, LeaderboardOptions options, final ServiceCallback<LeaderboardEntry> callback) {
		LeaderboardFilter filter;
		if (options != null && options.collection == LeaderboardOptions.Collection.Friends) {
			filter = LeaderboardFilter.FRIENDS_ALL_TIME;
		} else {
			filter = LeaderboardFilter.GLOBAL_ALL_TIME;
		}
		AGResponseHandle<GetPlayerScoreResponse> handle = client.getLeaderboardsClient().getLocalPlayerScore(leaderboardId, filter);
		if (callback != null) {
			handle.setCallback(new AGResponseCallback<GetPlayerScoreResponse>() {
				@Override
				public void onComplete(final GetPlayerScoreResponse getPlayerScoreResponse) {
					ServiceResponse response = new GameCircleResponseWrapper(getPlayerScoreResponse);
					if (response.isSuccessful()) {
						callback.onSuccess(new LeaderboardEntry() {
							
							@Override
							public String getSource() {
								return "Amazon GameCircle";
							}
							
							@Override
							public long getScore() {
								return getPlayerScoreResponse.getScoreValue();
							}
							
							@Override
							public long getRank() {
								return getPlayerScoreResponse.getRank();
							}
							
							@Override
							public String getPlayerName() {
								return playerName;
							}
							
							@Override
							public String getPlayerId() {
								return playerId;
							}
							
							@Override
							public Date getDate() {
								return new Date();
							}
						}, response);
					} else {
						callback.onFailure(response);
					}
				}
			});
		}
	}

	@Override
	public void getScores(String leaderboardId, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {

		LeaderboardFilter filter;
		if (options != null && options.collection == LeaderboardOptions.Collection.Friends) {
			filter = LeaderboardFilter.FRIENDS_ALL_TIME;
		} else {
			filter = LeaderboardFilter.GLOBAL_ALL_TIME;
		}

		AGResponseHandle<GetScoresResponse> handle = client.getLeaderboardsClient().getScores(leaderboardId, filter);
		if (callback != null) {
			handle.setCallback(new AGResponseCallback<GetScoresResponse>() {
				
				@Override
				public void onComplete(GetScoresResponse getScoresResponse) {
					ServiceResponse response = new GameCircleResponseWrapper(getScoresResponse);
					if (response.isSuccessful()) {
						callback.onSuccess(
								new TransformIterable<Score, LeaderboardEntry>(getScoresResponse.getScores()) {
									@Override
									protected LeaderboardEntry transform(Score item) {
										return new GameCircleLeaderboardScoreWrapper(item);
									}
								}, response);
					} else {
						callback.onFailure(response);
					}
				}
			});
		}
	}

	@Override
	public void submitScore(String leaderboardId, long score, final ServiceCallback<Void> callback) {
		AGResponseHandle<SubmitScoreResponse> handle = client.getLeaderboardsClient().submitScore(leaderboardId, score);
		if (callback != null) {
			handle.setCallback(new AGResponseCallback<SubmitScoreResponse>() {
				@Override
				public void onComplete(SubmitScoreResponse submitScoreResponse) {
					ServiceResponse response = new GameCircleResponseWrapper(submitScoreResponse);
					if (response.isSuccessful()) {
						callback.onSuccess(null, response);
					} else {
						callback.onFailure(response);
					}
				}
			});
		}
	}

	// Saved games

	@Override
	public void getSavedGames(final ServiceCallback<Iterable<SavedGame>> callback) {
        final GameDataMap gameDataMap = AmazonGamesClient.getWhispersyncClient().getGameData();
        Set<String> keys = gameDataMap.getLatestStringKeys();

		if (callback != null) {
			ServiceResponse response = new PlainServiceResponse(true, 0, null);
			callback.onSuccess(
					new TransformIterable<String, SavedGame>(keys) {
						@Override
						protected SavedGame transform(final String item) {
							return new GameCircleGameDataWrapper(item, gameDataMap.getLatestString(item));
						}
					}, response);
		}
	}

	@Override
	public void loadSavedGameData(SavedGame save, final ServiceCallback<byte[]> callback) {
        GameDataMap gameDataMap = AmazonGamesClient.getWhispersyncClient().getGameData();
        SyncableString data = gameDataMap.getLatestString(save.getId());
        if (data.isSet()) {
			if (callback != null) {
				try {
		        	byte[] raw = Base64Coder.decode(data.getValue());
					callback.onSuccess(raw, PlainServiceResponse.success());
				} catch (Exception e) {
					error(e.getLocalizedMessage());
					callback.onFailure(PlainServiceResponse.error(e.getMessage()));
				}
			}
        } else {
			if (callback != null)
				callback.onFailure(PlainServiceResponse.error("Data not found for key " + save.getId()));
        }
	}

	@Override
	public void submitSavedGame(final SavedGame save, final byte[] data, final ServiceCallback<Void> callback) {
		WhispersyncClient whispersyncClient = AmazonGamesClient.getWhispersyncClient();
		GameDataMap gameDataMap = whispersyncClient.getGameData();
		try {
			SyncableString saved = gameDataMap.getLatestString(save.getId());
			String encoded = new String(Base64Coder.encode(data));
			saved.set(encoded);
			whispersyncClient.flush();
			if (callback != null)
				callback.onSuccess(null, PlainServiceResponse.success());
		} catch (Exception e) {
			error(e.getLocalizedMessage());
			if (callback != null)
				callback.onFailure(PlainServiceResponse.error(e.getMessage()));
		}
	}

	@Override
	public void deleteSavedGame(SavedGame save, final ServiceCallback<Void> callback) {
		if (callback != null) {
			callback.onFailure(new PlainServiceResponse(false, -1, "Amazon GameCircle does not allow saved game deletion"));
		}
	}

	// Utilities

	protected void debug(String text) {

	}

	protected void error(String error) {

	}
}