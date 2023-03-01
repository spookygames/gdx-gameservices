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
package games.spooky.gdx.gameservices.googleplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.badlogic.gdx.utils.Array;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.*;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataBuffer;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.*;
import games.spooky.gdx.gameservices.*;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions.Scope;
import games.spooky.gdx.gameservices.savedgame.SavedGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressLint("VisibleForTests")
public class GooglePlayServicesHandler implements GameServicesHandler {

	private Activity activity;
	private GamesSignInClient authenticationClient;
	private PlayersClient playersClient;
	private AchievementsClient achievementsClient;
	private LeaderboardsClient leaderboardsClient;
	private SnapshotsClient snapshotsClient;

	private int resolutionPolicy = GooglePlaySnapshotResolutionPolicy.LAST_KNOWN_GOOD.rawValue;

	/**
	 * Get the current resolution policy that should handle {@link Snapshot} conflicts.
	 * @return the resolution policy
	 */
	public GooglePlaySnapshotResolutionPolicy getResolutionPolicy() {
		return GooglePlaySnapshotResolutionPolicy.fromRawValue(resolutionPolicy);
	}

	/**
	 * Set the resolution policy that should handle {@link Snapshot} conflicts.
	 * @param resolutionPolicy the new resolution policy
	 */
	public void setResolutionPolicy(GooglePlaySnapshotResolutionPolicy resolutionPolicy) {
		this.resolutionPolicy = resolutionPolicy.rawValue;
	}

	/**
	 * Set the resolution policy that should handle {@link Snapshot} conflicts.
	 * Valid values are accessible from @{@link SnapshotsClient}.
	 * @param resolutionPolicy the new resolution policy
	 */
	public void setResolutionPolicyRaw(int resolutionPolicy) {
		this.resolutionPolicy = resolutionPolicy;
	}

	// Lifecycle

	public void setContext(final Activity activity) {
		PlayGamesSdk.initialize(activity);
		this.activity = activity;
		this.authenticationClient = PlayGames.getGamesSignInClient(activity);
		this.playersClient = PlayGames.getPlayersClient(activity);
		this.achievementsClient = PlayGames.getAchievementsClient(activity);
		this.leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
		this.snapshotsClient = PlayGames.getSnapshotsClient(activity);
	}

	// Connection

	@Override
	public AsyncServiceResult<Boolean> isLoggedIn() {
		return new GooglePlayAsyncServiceResult<AuthenticationResult, Boolean>(authenticationClient.isAuthenticated()) {
			@Override protected Boolean transformResult(AuthenticationResult result) {
				return result.isAuthenticated();
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> login() {
		return new GooglePlayVoidAsyncServiceResult<>(authenticationClient.isAuthenticated().continueWithTask(new Continuation<AuthenticationResult, Task<AuthenticationResult>>() {
			@Override
			public Task<AuthenticationResult> then(@NonNull Task<AuthenticationResult> task) {
				if (task.isSuccessful() && !task.getResult().isAuthenticated()) {
					return authenticationClient.signIn();
				} else {
					return task;
				}
			}
		}));
	}

	@Override
	public AsyncServiceResult<String> getPlayerId() {
		return new GooglePlayAsyncServiceResult<Player, String>(playersClient.getCurrentPlayer()) {
			@Override
			protected String transformResult(Player player) {
				return player.getPlayerId();
			}
		};
	}

	@Override
	public AsyncServiceResult<String> getPlayerName() {
		return new GooglePlayAsyncServiceResult<Player, String>(playersClient.getCurrentPlayer()) {
			@Override
			protected String transformResult(Player player) {
				return player.getDisplayName();
			}
		};
	}

	@Override
	public AsyncServiceResult<byte[]> getPlayerAvatar() {
		return new CallbackAsyncServiceResult<Drawable, byte[]>() {
			@Override
			protected void callAsync(final Callback<Drawable> callback) {
				playersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
					@Override
					public void onComplete(@NonNull Task<Player> task) {
						if (task.isSuccessful()) {
							final Uri avatarUri = task.getResult().getIconImageUri();
							if (avatarUri == null) {
								callback.onSuccess(null);
							} else {
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										ImageManager.OnImageLoadedListener listener = new ImageManager.OnImageLoadedListener() {
											@Override
											public void onImageLoaded(@NonNull Uri uri, Drawable drawable, boolean isRequestedDrawable) {
												callback.onSuccess(drawable);
											}
										};
										ImageManager.create(activity).loadImage(listener, avatarUri);
									}
								});
							}
						} else {
							callback.onError(task.getException());
						}
					}
				});
			}

			@Override
			protected byte[] transformResult(Drawable drawable) {
				return drawable == null ? null : drawableToBytes(drawable);
			}
		};
	}

	// Achievements

	@Override
	public boolean handlesAchievements() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<Achievement>> getAchievements() {
		return new GooglePlayAsyncServiceResult<AchievementBuffer, Iterable<Achievement>>(
				achievementsClient.load(false /* force-reload */)
						.continueWith(resolveAnnotated(AchievementBuffer.class))
		) {
			@Override
			protected Iterable<Achievement> transformResult(AchievementBuffer result) {
				Array<Achievement> achievements = new Array<>(result.getCount());
				for (com.google.android.gms.games.achievement.Achievement achievement : result) {
					achievements.add(new GooglePlayAchievement(achievement));
				}
				result.release();
				return achievements;
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> unlockAchievement(String achievementId) {
		return new GooglePlayVoidAsyncServiceResult<>(achievementsClient.unlockImmediate(achievementId));
	}

	// Leaderboards

	@Override
	public boolean handlesLeaderboards() {
		return true;
	}

	@Override
	public AsyncServiceResult<LeaderboardEntry> getPlayerScore(String leaderboardId, LeaderboardOptions options) {
		int collection;
		if (options != null && options.getScope() == Scope.Friends) {
			collection = LeaderboardVariant.COLLECTION_FRIENDS;
		} else {
			collection = LeaderboardVariant.COLLECTION_PUBLIC;
		}
		return new GooglePlayAsyncServiceResult<LeaderboardScore, LeaderboardEntry>(
				leaderboardsClient.loadCurrentPlayerLeaderboardScore(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection)
						.continueWith(resolveAnnotated(LeaderboardScore.class))
		) {
			@Override
			protected LeaderboardEntry transformResult(LeaderboardScore result) {
				return new GooglePlayLeaderboardEntry(result);
			}
		};
	}

	@Override
	public AsyncServiceResult<Iterable<LeaderboardEntry>> getScores(String leaderboardId, LeaderboardOptions options) {

		boolean playerCentered;
		int collection;
		int items;

		if (options == null) {
			playerCentered = false;
			collection = LeaderboardVariant.COLLECTION_PUBLIC;
			items = 20;
		} else {
			LeaderboardOptions.Window window = options.getWindow();
			playerCentered = window == LeaderboardOptions.Window.CenteredOnPlayer;
			Scope scope = options.getScope();
			collection = scope == Scope.Friends ? LeaderboardVariant.COLLECTION_FRIENDS : LeaderboardVariant.COLLECTION_PUBLIC;

			int perPage = options.getMaxResults();
			if (perPage > 0)
				items = perPage;
			else
				items = 20;
		}

		Task<AnnotatedData<LeaderboardsClient.LeaderboardScores>> task = playerCentered ?
			leaderboardsClient.loadPlayerCenteredScores(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection, items) :
			leaderboardsClient.loadTopScores(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, collection, items);

		return new GooglePlayAsyncServiceResult<LeaderboardsClient.LeaderboardScores, Iterable<LeaderboardEntry>>(
				task.continueWith(resolveAnnotated(LeaderboardsClient.LeaderboardScores.class))) {
			@Override
			protected Iterable<LeaderboardEntry> transformResult(LeaderboardsClient.LeaderboardScores result) {
				Array<LeaderboardEntry> scores = new Array<>(result.getScores().getCount());
				for (LeaderboardScore score : result.getScores()) {
					scores.add(new GooglePlayLeaderboardEntry(score));
				}
				result.release();
				return scores;
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitScore(String leaderboardId, long score) {
		return new GooglePlayVoidAsyncServiceResult<>(leaderboardsClient.submitScoreImmediate(leaderboardId, score));
	}

	// Saved games

	@Override
	public boolean handlesSavedGames() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<SavedGame>> getSavedGames() {
		return new GooglePlayAsyncServiceResult<SnapshotMetadataBuffer, Iterable<SavedGame>>(
				snapshotsClient.load(false /* force-reload */)
						.continueWith(resolveAnnotated(SnapshotMetadataBuffer.class))
		) {
			@Override
			protected Iterable<SavedGame> transformResult(SnapshotMetadataBuffer result) {
				Array<SavedGame> savedGames = new Array<>(result.getCount());
				for (SnapshotMetadata snapshotMetadata : result) {
					savedGames.add(new GooglePlaySavedGame(snapshotMetadata));
				}
				result.release();
				return savedGames;
			}
		};
	}

	@Override
	public AsyncServiceResult<byte[]> loadSavedGameData(final SavedGame save) {
		return new GooglePlayAsyncServiceResult<Snapshot, byte[]>(
				snapshotsClient.open(save.getTitle(), false, resolutionPolicy).continueWith(resolveDataOrConflict(save, Snapshot.class))
		) {
			@Override
			protected byte[] transformResult(Snapshot result) throws IOException {
				return result.getSnapshotContents().readFully();
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitSavedGame(final SavedGame save, final byte[] data) {
		return new GooglePlayVoidAsyncServiceResult<>(
				snapshotsClient.open(save.getTitle(), true, resolutionPolicy)
						.continueWith(resolveDataOrConflict(save, Snapshot.class))
						.continueWithTask(new Continuation<Snapshot, Task<SnapshotMetadata>>() {
							@Override
							public Task<SnapshotMetadata> then(@NonNull Task<Snapshot> task) {
								Snapshot snapshot = task.getResult();
								snapshot.getSnapshotContents().writeBytes(data);

								SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
										.fromMetadata(snapshot.getMetadata())
										.setPlayedTimeMillis(save.getPlayedTime())
										.setDescription(save.getDescription())
										.build();

								return snapshotsClient.commitAndClose(snapshot, metadataChange);
							}})
				);
	}

	@Override
	public AsyncServiceResult<Void> deleteSavedGame(SavedGame save) {
		return new GooglePlayVoidAsyncServiceResult<>(
				snapshotsClient.open(save.getTitle(), false, resolutionPolicy)
						.continueWith(resolveDataOrConflict(save, Snapshot.class))
						.continueWithTask(new Continuation<Snapshot, Task<String>>() {
							@Override
							public Task<String> then(@NonNull Task<Snapshot> task) {
								return snapshotsClient.delete(task.getResult().getMetadata());
							}
						}));
	}

	// Utilities

	private static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable)
			return ((BitmapDrawable)drawable).getBitmap();

		// We ask for the bounds if they have been set as they would be most
		// correct, then we check we are  > 0
		Rect bounds = drawable.getBounds();
		final int width = bounds.isEmpty() ? drawable.getIntrinsicWidth() : bounds.width();
		final int height = bounds.isEmpty() ? drawable.getIntrinsicHeight() : bounds.height();

		// Now we check we are > 0
		final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	private static byte[] drawableToBytes(Drawable drawable) {
		Bitmap bitmap = drawableToBitmap(drawable);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	private static abstract class GooglePlayAsyncServiceResult<TTask, TCallback> implements AsyncServiceResult<TCallback> {
		private final Task<TTask> task;

		private GooglePlayAsyncServiceResult(Task<TTask> task) {
			this.task = task;
		}

		@Override
		public void onSuccess(final ServiceSuccessCallback<TCallback> callback) {
			task.addOnSuccessListener(new OnSuccessListener<TTask>() {
				@Override
				public void onSuccess(TTask taskResult) {
					try {
						succeed(taskResult, callback);
					} catch (Exception ignored) {}
				}
			});
		}

		@Override
		public void onError(final ServiceErrorCallback callback) {
			task.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					fail(exception, callback);
				}
			});
		}

		@Override
		public void onCompletion(final ServiceCompletionCallback<TCallback> callback) {
			task.addOnCompleteListener(new OnCompleteListener<TTask>() {
				@Override
				public void onComplete(@NonNull Task<TTask> task) {
					if (task.isSuccessful()) {
						try {
							succeed(task.getResult(), callback);
						} catch (Exception e) {
							fail(e, callback);
						}
					} else {
						fail(task.getException(), callback);
					}
				}
			});
		}

		protected abstract TCallback transformResult(TTask result) throws Exception;

		private void succeed(TTask taskResult, ServiceSuccessCallback<TCallback> callback) throws Exception {
			callback.onSuccess(transformResult(taskResult));
		}

		private void fail(Exception exception, ServiceErrorCallback callback) {
			if (exception == null) {
				callback.onError(SimpleServiceError.error(-1));
			} else if (exception instanceof ApiException) {
				callback.onError(new GooglePlayStatusServiceError(((ApiException) exception).getStatus()));
			} else {
				callback.onError(new ExceptionServiceError(exception));
			}
		}
	}

	private static class GooglePlayVoidAsyncServiceResult<T> extends GooglePlayAsyncServiceResult<T, Void> {
		private GooglePlayVoidAsyncServiceResult(Task<T> task) { super(task); }
		@Override protected Void transformResult(T result) { return null; }
	}

	private <T> Continuation<SnapshotsClient.DataOrConflict<T>, T> resolveDataOrConflict(final SavedGame save, Class<T> typeHint) {
		return new Continuation<SnapshotsClient.DataOrConflict<T>, T>() {
			@Override
			public T then(@NonNull Task<SnapshotsClient.DataOrConflict<T>> task) {
				SnapshotsClient.DataOrConflict<T> result = task.getResult();
				if (result.isConflict()) {
					throw new RuntimeException("Google Play snapshot " + save.getTitle() + " is in conflict state: " + result.getConflict());
				} else {
					return result.getData();
				}
			}
		};
	}

	private <T> Continuation<AnnotatedData<T>, T> resolveAnnotated(Class<T> typeHint) {
		return new Continuation<AnnotatedData<T>, T>() {
			@Override
			public T then(@NonNull Task<AnnotatedData<T>> task) {
				T result = task.getResult().get();
				if (result == null) {
					throw new RuntimeException("Google Play annotated data is null");
				} else {
					return result;
				}
			}
		};
	}

}