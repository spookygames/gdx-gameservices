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
package games.spooky.gdx.gameservices.gamecenter;

import com.badlogic.gdx.utils.Array;
import games.spooky.gdx.gameservices.*;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions.Scope;
import games.spooky.gdx.gameservices.savedgame.SavedGame;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.gamekit.*;
import org.robovm.apple.uikit.UIImage;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;

import static games.spooky.gdx.gameservices.SyncSuccessServiceResult.sync;
import static games.spooky.gdx.gameservices.gamecenter.GameCenterSavedGameWrapper.unwrap;

/**
 * Game services handler for Apple Game Center.
 */
public class GameCenterServicesHandler implements GameServicesHandler {

	private final UIViewController viewController;

	public GameCenterServicesHandler(UIViewController viewController) {
		this.viewController = viewController;
	}

	@Override
	public AsyncServiceResult<Boolean> isLoggedIn() {
		return sync(GKLocalPlayer.getLocalPlayer().isAuthenticated());
	}

	@Override
	public AsyncServiceResult<Void> login() {
		return new GameCenterAsyncServiceResult<UIViewController, Void>() {
			@Override
			protected void call(final VoidBlock2<UIViewController, NSError> block) {
				final GKLocalPlayer localPlayer = GKLocalPlayer.getLocalPlayer();
				if (!localPlayer.isAuthenticated()) {
					localPlayer.setAuthenticateHandler(new VoidBlock2<UIViewController, NSError>() {
						@Override
						public void invoke(UIViewController view, NSError nsError) {
							if (nsError == null) {
								if (view != null) {
									viewController.presentViewController(view, true, null);
								}
							}
							block.invoke(view, nsError);
						}
					});
				}
			}

			@Override
			protected Void transformResult(UIViewController result) {
				return null;
			}
		};
	}

	@Override
	public AsyncServiceResult<String> getPlayerId() {
		return sync(GKLocalPlayer.getLocalPlayer().getTeamPlayerID());
	}

	@Override
	public AsyncServiceResult<String> getPlayerName() {
		// getAlias() matches expected behavior more than getDisplayName()
		return sync(GKLocalPlayer.getLocalPlayer().getAlias());
	}

	@Override
	public AsyncServiceResult<byte[]> getPlayerAvatar() {
		return new GameCenterAsyncServiceResult<UIImage, byte[]>() {
			@Override
			protected void call(VoidBlock2<UIImage, NSError> block) {
				GKLocalPlayer.getLocalPlayer().loadPhoto(GKPhotoSize.Normal, block);
			}

			@Override
			protected byte[] transformResult(UIImage image) {
				return imageToBytes(image);
			}
		};
	}

	@Override
	public boolean handlesAchievements() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<Achievement>> getAchievements() {
		return new GameCenterAsyncServiceResult<NSArray<GKAchievement>, Iterable<Achievement>>() {
			@Override
			protected void call(VoidBlock2<NSArray<GKAchievement>, NSError> block) {
				GKAchievement.loadAchievements(block);
			}

			@Override
			protected Iterable<Achievement> transformResult(NSArray<GKAchievement> achievements) {
				return achievements == null ?
					new Array<Achievement>(0) :
					new TransformIterable<GKAchievement, Achievement>(achievements) {
						@Override
						protected Achievement transform(GKAchievement item) {
							return new GameCenterAchievementWrapper(item);
						}
					};
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> unlockAchievement(final String achievementId) {
		return new GameCenterVoidAsyncServiceResult() {
			@Override
			protected void call(VoidBlock1<NSError> block) {
				GKAchievement achievement = new GKAchievement(achievementId);
				NSArray<GKAchievement> array = new NSArray<>(achievement);
				GKAchievement.reportAchievements(array, block);
			}
		};
	}

	@Override
	public boolean handlesLeaderboards() {
		return true;
	}

	@Override
	public AsyncServiceResult<LeaderboardEntry> getPlayerScore(String leaderboardId, LeaderboardOptions options) {
		GKLeaderboard leaderboard = getLeaderboard(leaderboardId, options);
		GKScore score = leaderboard.getLocalPlayerScore();
		return score == null ?
				SyncErrorServiceResult.<LeaderboardEntry>syncError(SimpleServiceError.error("No local score found")) :
				SyncSuccessServiceResult.<LeaderboardEntry>sync(new GameCenterLeaderboardEntryWrapper(score));
	}

	@Override
	public AsyncServiceResult<Iterable<LeaderboardEntry>> getScores(final String leaderboardId, final LeaderboardOptions options) {
		return new GameCenterAsyncServiceResult<NSArray<GKScore>, Iterable<LeaderboardEntry>>() {
			@Override
			protected void call(VoidBlock2<NSArray<GKScore>, NSError> block) {
				GKLeaderboard leaderboard = getLeaderboard(leaderboardId, options);
				leaderboard.loadScores(block);
			}

			@Override
			protected Iterable<LeaderboardEntry> transformResult(NSArray<GKScore> scores) {
				return scores == null ?
						new Array<LeaderboardEntry>(0) :
						new TransformIterable<GKScore, LeaderboardEntry>(scores) {
							@Override
							protected LeaderboardEntry transform(GKScore item) {
								return new GameCenterLeaderboardEntryWrapper(item);
							}
						};
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitScore(final String leaderboardId, final long score) {
		return new GameCenterVoidAsyncServiceResult() {
			@Override
			protected void call(VoidBlock1<NSError> block) {
				GKScore gkScore = new GKScore();
				gkScore.setLeaderboardIdentifier(leaderboardId);
				gkScore.setValue(score);
				NSArray<GKScore> scores = new NSArray<>(gkScore);
				GKScore.reportScores(scores, block);
			}
		};
	}
	
	private GKLeaderboard getLeaderboard(String leaderboardId, LeaderboardOptions options) {

		GKLeaderboard leaderboard = new GKLeaderboard();
		leaderboard.setIdentifier(leaderboardId);

		if (options != null) {
			// Window options are not available on Game Center, will use Window.Top

			Scope scope = options.getScope();
			if (scope != null) {
				switch (scope) {
				case Public:
					leaderboard.setPlayerScope(GKLeaderboardPlayerScope.Global);
					break;
				case Friends:
					leaderboard.setPlayerScope(GKLeaderboardPlayerScope.FriendsOnly);
					break;
				}
			}

			int max = options.getMaxResults();
			if (max > 0) {
				leaderboard.setRange(new NSRange(1, max));
			}
		}
		
		return leaderboard;
	}

	@Override
	public boolean handlesSavedGames() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<SavedGame>> getSavedGames() {
		return new GameCenterAsyncServiceResult<NSArray<GKSavedGame>, Iterable<SavedGame>>() {
			@Override
			protected void call(VoidBlock2<NSArray<GKSavedGame>, NSError> block) {
				GKLocalPlayer.getLocalPlayer().fetchSavedGames(block);
			}

			@Override
			protected Iterable<SavedGame> transformResult(NSArray<GKSavedGame> savedGames) {
				return savedGames == null ? new Array<SavedGame>(0) : new TransformIterable<GKSavedGame, SavedGame>(savedGames) {
					@Override
					protected SavedGame transform(GKSavedGame item) {
						return new GameCenterSavedGameWrapper(item);
					}
				};
			}
		};
	}

	@Override
	public AsyncServiceResult<byte[]> loadSavedGameData(final SavedGame metadata) {
		return new GameCenterAsyncServiceResult<NSData, byte[]>() {
			@Override
			protected void call(VoidBlock2<NSData, NSError> block) {
				GKSavedGame game = unwrap(metadata);
				game.loadData(block);
			}

			@Override
			protected byte[] transformResult(NSData data) {
				return data.getBytes();
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitSavedGame(final SavedGame savedGame, final byte[] data) {
		return new GameCenterAsyncServiceResult<GKSavedGame, Void>() {
			@Override
			protected void call(VoidBlock2<GKSavedGame, NSError> block) {
				NSData nsData = new NSData(data);
				String id = savedGame.getId();
				GKLocalPlayer.getLocalPlayer().saveGameData(nsData, id, block);
			}

			@Override
			protected Void transformResult(GKSavedGame savedGame) {
				return null;
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> deleteSavedGame(final SavedGame savedGame) {
		return new GameCenterVoidAsyncServiceResult() {
			@Override
			protected void call(VoidBlock1<NSError> block) {
				String id = savedGame.getId();
				GKLocalPlayer.getLocalPlayer().deleteSavedGames(id, block);
			}
		};
	}


	/* Utils */

	private static byte[] imageToBytes(UIImage image) {
		if (image == null)
			return null;
		NSData data = image.toPNGData();
		if (data == null)
			data = image.toJPEGData(1.0);
		if (data == null)
			return null;
		return data.getBytes();
	}

	static abstract class GameCenterAsyncServiceResultBase<TBlock, TCallback> implements AsyncServiceResult<TCallback> {

		private final Array<ServiceSuccessCallback<TCallback>> successCallbacks = new Array<>();
		private final Array<ServiceErrorCallback> errorCallbacks = new Array<>();

		protected boolean completed;
		protected NSError error;
		protected TBlock result;

		@Override
		public void onSuccess(ServiceSuccessCallback<TCallback> callback) {
			synchronized(this) {
				successCallbacks.add(callback);
				checkExistingResponse();
			}
		}

		@Override
		public void onError(ServiceErrorCallback callback) {
			synchronized(this) {
				errorCallbacks.add(callback);
				checkExistingResponse();
			}
		}

		@Override
		public void onCompletion(ServiceCompletionCallback<TCallback> callback) {
			synchronized(this) {
				successCallbacks.add(callback);
				errorCallbacks.add(callback);
				checkExistingResponse();
			}
		}

		protected void checkExistingResponse() {
			if (completed) {
				if (error != null) {
					ServiceError serviceError = new GameCenterServiceError(error);
					for (ServiceErrorCallback callback : errorCallbacks)
						callback.onError(serviceError);
				} else {
					try {
						TCallback transformed = transformResult(result);
						for (ServiceSuccessCallback<TCallback> callback : successCallbacks)
							callback.onSuccess(transformed);
					} catch (Throwable e) {
						ServiceError serviceError = new ExceptionServiceError(e);
						for (ServiceErrorCallback callback : errorCallbacks)
							callback.onError(serviceError);
					}
				}
			}
		}

		protected abstract TCallback transformResult(TBlock result);
	}

	static abstract class GameCenterAsyncServiceResult<TBlock, TCallback> extends GameCenterAsyncServiceResultBase<TBlock, TCallback> {

		protected abstract void call(VoidBlock2<TBlock, NSError> block);

		{
			call(new VoidBlock2<TBlock, NSError>() {
				@Override
				public void invoke(TBlock result, NSError nsError) {
					synchronized (GameCenterAsyncServiceResult.this) {
						GameCenterAsyncServiceResult.this.completed = true;
						GameCenterAsyncServiceResult.this.error = error;
						GameCenterAsyncServiceResult.this.result = result;
						checkExistingResponse();
					}
				}
			});
		}
	}

	static abstract class GameCenterVoidAsyncServiceResult extends GameCenterAsyncServiceResultBase<Void, Void> {

		@Override
		protected Void transformResult(Void result) {
			return result;
		}

		protected abstract void call(VoidBlock1<NSError> block);

		{
			call(new VoidBlock1<NSError>() {
				@Override
				public void invoke(NSError nsError) {
					synchronized (GameCenterVoidAsyncServiceResult.this) {
						GameCenterVoidAsyncServiceResult.this.error = nsError;
						checkExistingResponse();
					}
				}
			});
		}
	}
}