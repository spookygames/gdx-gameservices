/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Spooky Games
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
package net.spookygames.gdx.gameservices.googleplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import net.spookygames.gdx.gameservices.ConnectionHandler;
import net.spookygames.gdx.gameservices.ServiceCallback;
import net.spookygames.gdx.gameservices.ServiceResponse;
import net.spookygames.gdx.gameservices.TransformIterable;
import net.spookygames.gdx.gameservices.achievement.Achievement;
import net.spookygames.gdx.gameservices.achievement.AchievementsHandler;
import net.spookygames.gdx.gameservices.leaderboard.LeaderboardEntry;
import net.spookygames.gdx.gameservices.leaderboard.LeaderboardOptions;
import net.spookygames.gdx.gameservices.leaderboard.LeaderboardOptions.Collection;
import net.spookygames.gdx.gameservices.leaderboard.LeaderboardOptions.Sort;
import net.spookygames.gdx.gameservices.leaderboard.LeaderboardsHandler;
import net.spookygames.gdx.gameservices.savedgame.SavedGame;
import net.spookygames.gdx.gameservices.savedgame.SavedGamesHandler;

import java.io.IOException;


public class GooglePlayServicesHandler implements ConnectionHandler, AchievementsHandler, LeaderboardsHandler, SavedGamesHandler {

	public static final int REQUEST_RESOLVE_ERROR = 1001;
	public static final int REQUEST_LEADERBOARD = 1002;

	private GoogleApiClient client = null;

	private boolean resolvingError = false;

	private ServiceCallback<Void> connectionCallback;

	private int resolutionPolicy = Snapshots.RESOLUTION_POLICY_LAST_KNOWN_GOOD;

	public GoogleApiClient getGoogleApiClient() {
		return client;
	}

	/**
	 * Get the current resolution policy that should handle {@link Snapshot} conflicts.
	 * @return
	 */
	public int getResolutionPolicy() {
		return resolutionPolicy;
	}

	/**
	 * Set the resolution policy that should handle {@link Snapshot} conflicts.
	 * Input a valid value from @{@link Snapshots} or suffer a hundred painful deaths.
	 * @param resolutionPolicy the new resolution policy
	 */
	public void setResolutionPolicy(int resolutionPolicy) {
		this.resolutionPolicy = resolutionPolicy;
	}

	// Lifecycle

	public void setContext(final AndroidApplication app, View view) {
		addAndroidEventListener(app, initializeContext(app, app.getContext(), view, false));
	}

	public void setContext(final AndroidFragmentApplication app, View view) {
		addAndroidEventListener(app, initializeContext(app, app.getContext(), view, false));
	}

	public void setContext(final AndroidApplication app, Context context, View view) {
		addAndroidEventListener(app, initializeContext(app, context, view, false));
	}

	public void setContext(final AndroidFragmentApplication app, Context context, View view) {
		addAndroidEventListener(app, initializeContext(app, context, view, false));
	}

	public void setContext(final AndroidApplication app, View view, boolean handleSaves) {
		addAndroidEventListener(app, initializeContext(app, app.getContext(), view, handleSaves));
	}

	public void setContext(final AndroidFragmentApplication app, View view, boolean handleSaves) {
		addAndroidEventListener(app, initializeContext(app, app.getContext(), view, handleSaves));
	}

	public void setContext(final AndroidApplication app, Context context, View view, boolean handleSaves) {
		addAndroidEventListener(app, initializeContext(app, context, view, handleSaves));
	}

	public void setContext(final AndroidFragmentApplication app, Context context, View view, boolean handleSaves) {
		addAndroidEventListener(app, initializeContext(app, context, view, handleSaves));
	}

	private AndroidEventListener initializeContext(final AndroidApplicationBase app, final Context context, View view, boolean handleSaves) {
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle bundle) {
						debug("Google API connected");

						if (isLoggedIn()) {

							if (connectionCallback != null)
								connectionCallback.onSuccess(null, new GooglePlayServicesBasicResponse(true, 0));
						}
					}

					@Override
					public void onConnectionSuspended(int i) {
						debug("Google API connection suspended");

						// Attempt to reconnect
						client.reconnect();
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult connectionResult) {
						int errorCode = connectionResult.getErrorCode();
						error("Google API connection failed " + connectionResult.getErrorMessage() + " (" + errorCode + ")");

						if (resolvingError) {
							// Already attempting to resolve an error.
							return;
						} else if (connectionResult.hasResolution()) {
							try {
								resolvingError = true;
								connectionResult.startResolutionForResult((Activity) context, REQUEST_RESOLVE_ERROR);
							} catch (IntentSender.SendIntentException e) {
								// There was an error with the resolution intent. Try again.
								client.connect();
							}

						} else {
							error("Login failed");
							resolvingError = true;

							if (connectionCallback != null)
								connectionCallback.onFailure(new GooglePlayServicesBasicResponse(false, errorCode, "Login failed"));
						}
					}
				})
				.addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.setViewForPopups(view);

		if (handleSaves) {
			builder.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);
		}

		client = builder.build();

		return new AndroidEventListener() {
			@Override
			public void onActivityResult(int requestCode, final int resultCode, Intent data) {
				debug("Google API response " + requestCode + " -- " + resultCode);

				if (requestCode == REQUEST_RESOLVE_ERROR) {
					resolvingError = false;

					switch (resultCode) {
						case Activity.RESULT_OK:
							// Make sure the app is not already connected or attempting to connect
							login(connectionCallback);
							break;

						case Activity.RESULT_CANCELED:
							error("Login cancelled");

							if (connectionCallback != null)
								connectionCallback.onFailure(new GooglePlayServicesBasicResponse(false, resultCode, "Login cancelled"));
							break;

						case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
						default:
							error("Login failed");

							if (connectionCallback != null)
								connectionCallback.onFailure(new GooglePlayServicesBasicResponse(false, resultCode, "Login failed"));
							break;
					}
				}

				if (requestCode == REQUEST_LEADERBOARD && resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
					// Force a disconnect to sync up state, ensuring that client reports "not connected"
					logout();
				}
			}
		};
	}

	// Dodgy code from lack of proper interface

	private static void addAndroidEventListener(final AndroidApplication application, final AndroidEventListener androidEventListener) {
		application.addAndroidEventListener(androidEventListener);
		application.addLifecycleListener(new LifecycleListener() {
			@Override
			public void pause() {
			}

			@Override
			public void resume() {
			}

			@Override
			public void dispose() {
				application.removeAndroidEventListener(androidEventListener);
				application.removeLifecycleListener(this);
			}
		});
	}

	private static void addAndroidEventListener(final AndroidFragmentApplication application, final AndroidEventListener androidEventListener) {
		application.addAndroidEventListener(androidEventListener);
		application.addLifecycleListener(new LifecycleListener() {
			@Override
			public void pause() {
			}

			@Override
			public void resume() {
			}

			@Override
			public void dispose() {
				application.removeAndroidEventListener(androidEventListener);
				application.removeLifecycleListener(this);
			}
		});
	}

	// Connection

	@Override
	public boolean isLoggedIn() {
		return client != null && client.isConnected();
	}

	@Override
	public void login(ServiceCallback<Void> callback) {
		if (!isLoggedIn()) {
			this.connectionCallback = callback;
			client.connect();
		}
	}

	public void logout() {
		if (isLoggedIn()) {
			client.disconnect();
		}
	}

	@Override
	public String getPlayerId() {
		return Games.Players.getCurrentPlayer(client).getPlayerId();
	}

	@Override
	public String getPlayerName() {
		return Games.Players.getCurrentPlayer(client).getDisplayName();
	}

	// Achievements

	@Override
	public void getAchievements(final ServiceCallback<Iterable<Achievement>> callback) {
		PendingResult<Achievements.LoadAchievementsResult> intent = Games.Achievements.load(client, true);
		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>() {
				@Override
				public void onResult(Achievements.LoadAchievementsResult loadAchievementsResult) {
					Status status = loadAchievementsResult.getStatus();
					ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
					if (status.isSuccess()) {
						callback.onSuccess(
								new TransformIterable<com.google.android.gms.games.achievement.Achievement, Achievement>(loadAchievementsResult.getAchievements()) {
									@Override
									protected Achievement transform(com.google.android.gms.games.achievement.Achievement item) {
										return new GooglePlayAchievementWrapper(item);
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
	public void unlockAchievement(String achievementId, final ServiceCallback<Void> callback) {
		PendingResult<Achievements.UpdateAchievementResult> intent = Games.Achievements.unlockImmediate(client, achievementId);
		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Achievements.UpdateAchievementResult>() {
				@Override
				public void onResult(@NonNull Achievements.UpdateAchievementResult updateAchievementResult) {
					Status status = updateAchievementResult.getStatus();
					ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
					if (status.isSuccess()) {
						callback.onSuccess(null, response);
					} else {
						callback.onFailure(response);
					}
				}
			});
		}
	}

	// Leaderboards

	@Override
	public void getPlayerScore(String leaderboardId, LeaderboardOptions options, final ServiceCallback<LeaderboardEntry> callback) {
		int collection;
		if (options != null && options.collection == Collection.Friends) {
			collection = LeaderboardVariant.COLLECTION_SOCIAL;
		} else {
			collection = LeaderboardVariant.COLLECTION_PUBLIC;
		}
		PendingResult<Leaderboards.LoadPlayerScoreResult> intent = Games.Leaderboards
				.loadCurrentPlayerLeaderboardScore(client, leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection);
		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
					ServiceResponse response = new GooglePlayServicesStatusWrapper(loadPlayerScoreResult.getStatus());
					if (response.isSuccessful()) {
						LeaderboardScore score = loadPlayerScoreResult.getScore();
						if (score == null) {
							callback.onFailure(response);
						} else {
							callback.onSuccess(new GooglePlayLeaderboardScoreWrapper(score), response);
						}
					} else {
						callback.onFailure(response);
					}
				}
			});
		}
	}

	@Override
	public void getScores(String leaderboardId, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {

		boolean top;
		int collection;
		int items;

		if (options == null) {
			top = true;
			collection = LeaderboardVariant.COLLECTION_PUBLIC;
			items = 20;
		} else {
			Sort sort = options.sort;
			if (sort == null) {
				top = true;
			} else {
				switch (sort) {
				case CenteredOnPlayer:
					top = false;
					break;
				case Bottom:
					error("Sort.Bottom is not available for Google Play, using Sort.Top instead");
				case Top:
				default:
					top = true;
					break;
				}
			}
			Collection c = options.collection;
			if (c == Collection.Friends) {
				collection = LeaderboardVariant.COLLECTION_SOCIAL;
			} else {
				collection = LeaderboardVariant.COLLECTION_PUBLIC;
			}

			int perPage = options.itemsPerPage;
			if (perPage > 0)
				items = perPage;
			else
				items = 20;
		}

		PendingResult<Leaderboards.LoadScoresResult> intent;

		if (top) {
			intent = Games.Leaderboards.loadTopScores(client, leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection, items);
		} else {
			intent = Games.Leaderboards.loadPlayerCenteredScores(client, leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection, items);
		}

		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.LoadScoresResult loadScoresResult) {
					ServiceResponse response = new GooglePlayServicesStatusWrapper(loadScoresResult.getStatus());
					if (response.isSuccessful()) {
						callback.onSuccess(
								new TransformIterable<LeaderboardScore, LeaderboardEntry>(loadScoresResult.getScores()) {
									@Override
									protected LeaderboardEntry transform(LeaderboardScore item) {
										return new GooglePlayLeaderboardScoreWrapper(item);
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
		PendingResult<Leaderboards.SubmitScoreResult> intent = Games.Leaderboards.submitScoreImmediate(client, leaderboardId, score);
		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Leaderboards.SubmitScoreResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.SubmitScoreResult submitScoreResult) {
					Status status = submitScoreResult.getStatus();
					ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
					if (status.isSuccess()) {
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
		PendingResult<Snapshots.LoadSnapshotsResult> intent = Games.Snapshots.load(client, true);

		if (callback != null) {
			intent.setResultCallback(new ResultCallback<Snapshots.LoadSnapshotsResult>() {
				@Override
				public void onResult(Snapshots.LoadSnapshotsResult loadSnapshotsResult) {
					Status status = loadSnapshotsResult.getStatus();
					ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
					if (status.isSuccess()) {
						callback.onSuccess(
								new TransformIterable<SnapshotMetadata, SavedGame>(loadSnapshotsResult.getSnapshots()) {
									@Override
									protected SavedGame transform(final SnapshotMetadata item) {
										return new GooglePlaySnapshotWrapper(item);
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
	public void loadSavedGameData(SavedGame save, final ServiceCallback<byte[]> callback) {
		extractMetadata(save, false, resolutionPolicy, new ServiceCallback<SnapshotMetadata>() {
			@Override
			public void onSuccess(SnapshotMetadata metadata, ServiceResponse response) {

				try {
					PendingResult<Snapshots.OpenSnapshotResult> intent = Games.Snapshots.open(client, metadata, resolutionPolicy);

					if (callback != null) {
						intent.setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
							@Override
							public void onResult(@NonNull Snapshots.OpenSnapshotResult openSnapshotResult) {
								Status status = openSnapshotResult.getStatus();
								ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
								if (status.isSuccess()) {
									Snapshot snapshot = openSnapshotResult.getSnapshot();
									try {
										// We stop to read all the content
										byte[] data = snapshot.getSnapshotContents().readFully();
										callback.onSuccess(data, response);
									} catch (IOException e) {
										error(e.getLocalizedMessage());
										callback.onFailure(response);
									}
								} else {
									callback.onFailure(response);
								}
							}
						});
					}
				} catch (Throwable e) {
					if (callback != null)
						callback.onFailure(new GooglePlayServicesBasicResponse(false, -1, e.getLocalizedMessage()));
				}
			}

			@Override
			public void onFailure(ServiceResponse response) {
				if (callback != null)
					callback.onFailure(response);
			}
		});
	}

	@Override
	public void submitSavedGame(final SavedGame save, final byte[] data, final ServiceCallback<Void> callback) {
		extractMetadata(save, true, resolutionPolicy, new ServiceCallback<SnapshotMetadata>() {
			@Override
			public void onSuccess(final SnapshotMetadata properMetadata, ServiceResponse response) {
				try {
					// Open metadata in order to get proper objects
					Games.Snapshots.open(client, save.getTitle(), true, resolutionPolicy)
							.setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
								@Override
								public void onResult(@NonNull Snapshots.OpenSnapshotResult openSnapshotResult) {
									Status status = openSnapshotResult.getStatus();
									ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
									if (status.isSuccess()) {
										// Then and only then will we be able to manipulate our dear Snapshot object
										Snapshot snapshot = openSnapshotResult.getSnapshot();

										snapshot.getSnapshotContents().writeBytes(data);

										SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
												.fromMetadata(properMetadata)
												.setPlayedTimeMillis(save.getPlayedTime())
												.setDescription(save.getDescription())
												.build();

										try {
											PendingResult<Snapshots.CommitSnapshotResult> intent = Games.Snapshots.commitAndClose(client, snapshot, metadataChange);
											if (callback != null) {
												intent.setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
													@Override
													public void onResult(@NonNull Snapshots.CommitSnapshotResult commitSnapshotResult) {
														Status status = commitSnapshotResult.getStatus();
														ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
														if (status.isSuccess()) {
															callback.onSuccess(null, response);
														} else {
															callback.onFailure(response);
														}
													}
												});
											}
										} catch (Throwable e) {
											if (callback != null)
												callback.onFailure(new GooglePlayServicesBasicResponse(false, -1, e.getLocalizedMessage()));
										}
									} else {
										callback.onFailure(response);
									}
								}
						});
				} catch (Throwable e) {
					if (callback != null)
						callback.onFailure(new GooglePlayServicesBasicResponse(false, -1, e.getLocalizedMessage()));
				}
			}

			@Override
			public void onFailure(ServiceResponse response) {
				if (callback != null)
					callback.onFailure(response);
			}
		});
	}

	@Override
	public void deleteSavedGame(SavedGame save, final ServiceCallback<Void> callback) {
		extractMetadata(save, false, resolutionPolicy, new ServiceCallback<SnapshotMetadata>() {
			@Override
			public void onSuccess(SnapshotMetadata properMetadata, ServiceResponse response) {
				try {
					PendingResult<Snapshots.DeleteSnapshotResult> intent = Games.Snapshots.delete(client, properMetadata);

					if (callback != null) {
						intent.setResultCallback(new ResultCallback<Snapshots.DeleteSnapshotResult>() {
							@Override
							public void onResult(@NonNull Snapshots.DeleteSnapshotResult deleteSnapshotResult) {
								Status status = deleteSnapshotResult.getStatus();
								ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
								if (status.isSuccess()) {
									callback.onSuccess(null, response);
								} else {
									callback.onFailure(response);
								}
							}
						});
					}
				} catch (Throwable e) {
					if (callback != null)
						callback.onFailure(new GooglePlayServicesBasicResponse(false, -1, e.getLocalizedMessage()));
				}
			}

			@Override
			public void onFailure(ServiceResponse response) {
				if (callback != null)
					callback.onFailure(response);
			}
		});
	}

	private void extractMetadata(final SavedGame savedGame, boolean createIfNeeded, int resolutionPolicy, final ServiceCallback<SnapshotMetadata> callback) {
		if (savedGame instanceof GooglePlaySnapshotWrapper) {
			callback.onSuccess(((GooglePlaySnapshotWrapper) savedGame).getWrapped(), null);
		} else {
			// Open from API in order to get proper metadata (or create if none)
			Games.Snapshots.open(client, savedGame.getTitle(), createIfNeeded, resolutionPolicy)
					.setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
						@Override
						public void onResult(@NonNull Snapshots.OpenSnapshotResult openSnapshotResult) {
							Status status = openSnapshotResult.getStatus();
							ServiceResponse response = new GooglePlayServicesStatusWrapper(status);
							if (status.isSuccess()) {
								// Then and only then will we be able to manipulate our dear Snapshot object
								Snapshot snapshot = openSnapshotResult.getSnapshot();
								SnapshotMetadata metadata = snapshot.getMetadata();

								callback.onSuccess(metadata, response);
							} else {
								callback.onFailure(response);
							}
						}
					});
		}
	}

	// Utilities

	protected void debug(String text) {

	}

	protected void error(String error) {

	}

	public static void toast(final Context context, final int resource) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(context, resource, Toast.LENGTH_LONG).show();
				Looper.loop();
			}

		}.start();
	}

	public static void toast(final Context context, final String text) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
				Looper.loop();
			}

		}.start();
	}
}