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
package games.spooky.gdx.gameservices.gamecenter;

import org.robovm.apple.foundation.FoundationVersionNumber;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.gamekit.GKAchievement;
import org.robovm.apple.gamekit.GKLeaderboard;
import org.robovm.apple.gamekit.GKLeaderboardPlayerScope;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.gamekit.GKSavedGame;
import org.robovm.apple.gamekit.GKScore;
import org.robovm.apple.uikit.UIAlertAction;
import org.robovm.apple.uikit.UIAlertActionStyle;
import org.robovm.apple.uikit.UIAlertController;
import org.robovm.apple.uikit.UIAlertControllerStyle;
import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;

import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.PlainServiceResponse;
import games.spooky.gdx.gameservices.ServiceCallback;
import games.spooky.gdx.gameservices.TransformIterable;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementsHandler;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions.Collection;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;
import games.spooky.gdx.gameservices.savedgame.SavedGame;
import games.spooky.gdx.gameservices.savedgame.SavedGamesHandler;

@SuppressWarnings("deprecation")
public class GameCenterServicesHandler implements ConnectionHandler, AchievementsHandler, LeaderboardsHandler, SavedGamesHandler {

	@Override
	public boolean isLoggedIn() {
		return GKLocalPlayer.getLocalPlayer().isAuthenticated();
	}

	@Override
	public void login(final ServiceCallback<Void> callback) {
		if (!isLoggedIn()) {
			final GKLocalPlayer localPlayer = GKLocalPlayer.getLocalPlayer();
			localPlayer.setAuthenticateHandler(new VoidBlock2<UIViewController, NSError>() {
				@Override
				public void invoke(UIViewController viewController, NSError nsError) {
					final GameCenterErrorWrapper response = new GameCenterErrorWrapper(nsError);
					if (localPlayer.isAuthenticated()) {
						debug("Successfully logged into GameCenter");
						callback.onSuccess(null, response);
					} else {
						// GameCenter is disabled or operation was cancelled by user
						debug("Game Center account is required");
						callback.onFailure(response);
					}
				}
			});
		}
	}

	@Override
	public void logout() {
		if (isLoggedIn()) {
			// TODO
		}
	}

	@Override
	public String getPlayerId() {
		return GKLocalPlayer.getLocalPlayer().getPlayerID();
	}

	@Override
	public String getPlayerName() {
		// getAlias() matches wanted behavior more than getDisplayName()
		return GKLocalPlayer.getLocalPlayer().getAlias();
	}

	@Override
	public void getAchievements(final ServiceCallback<Iterable<Achievement>> callback) {
		
		GKAchievement.loadAchievements(new VoidBlock2<NSArray<GKAchievement>, NSError>() {
			@Override
			public void invoke(NSArray<GKAchievement> achievements, NSError error) {
				if (callback == null)
					return;
				
            	final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
            	if (response.isSuccessful()) {
					callback.onSuccess(
							new TransformIterable<GKAchievement, Achievement>(achievements) {
								@Override
								protected Achievement transform(GKAchievement item) {
									return new GameCenterAchievementWrapper(item);
								}
							}, response);
            	} else {
					callback.onFailure(response);
            	}
			}
		});
	}

	@Override
	public void unlockAchievement(String achievementId, final ServiceCallback<Void> callback) {
		
		GKAchievement achievement = new GKAchievement(achievementId);
		NSArray<GKAchievement> array = new NSArray<GKAchievement>(achievement);
		
		GKAchievement.reportAchievements(array, new VoidBlock1<NSError>() {
			@Override
			public void invoke(NSError error) {
				if (callback == null)
					return;
				
            	final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
            	if (response.isSuccessful()) {
					callback.onSuccess(null, response);
            	} else {
					callback.onFailure(response);
            	}
			}
		});
	}

	@Override
	public void getPlayerScore(String leaderboardId, LeaderboardOptions options, final ServiceCallback<LeaderboardEntry> callback) {

		GKLeaderboard leaderboard = getLeaderboard(leaderboardId, options);
		
		GKScore score = leaderboard.getLocalPlayerScore();
		
		if (callback != null) {
			if (score != null) {
				callback.onSuccess(new GameCenterLeaderboardEntryWrapper(score), new PlainServiceResponse(true, 0, null));
			} else {
				callback.onFailure(new PlainServiceResponse(false, -1, "No local score found"));
			}
		}
	}

	@Override
	public void getScores(String leaderboardId, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {

		GKLeaderboard leaderboard = getLeaderboard(leaderboardId, options);
		
		leaderboard.loadScores(new VoidBlock2<NSArray<GKScore>, NSError>() {
			@Override 
			public void invoke(NSArray<GKScore> scores, NSError error) {
				if (callback == null)
					return;

            	final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
            	if (response.isSuccessful()) {
					callback.onSuccess(
							new TransformIterable<GKScore, LeaderboardEntry>(scores) {
								@Override
								protected LeaderboardEntry transform(GKScore item) {
									return new GameCenterLeaderboardEntryWrapper(item);
								}
							}, response);
            	} else {
					callback.onFailure(response);
            	}
			}
		});
	}

	@Override
	public void submitScore(String leaderboardId, long score, ServiceCallback<Void> callback) {
		GKScore gkScore = new GKScore();
        gkScore.setLeaderboardIdentifier(leaderboardId);
        gkScore.setValue(score);
        
        NSArray<GKScore> scores = new NSArray<GKScore>(gkScore);

        GKScore.reportScores(scores, new VoidBlock1<NSError>() {
			@Override
			public void invoke(NSError error) {
	            if (error == null) {
	                debug("submitted score successfully");
	            }
			}
		});
	}
	
	private GKLeaderboard getLeaderboard(String leaderboardId, LeaderboardOptions options) {

		GKLeaderboard leaderboard = new GKLeaderboard();
		leaderboard.setIdentifier(leaderboardId);

		if (options != null) {
			if (options.sort != null) {
				error("Sorting options are not available on Game Center, will use Sort.Top");
			}
			Collection c = options.collection;
			if (c != null) {
				switch (c) {
				case Public:
					leaderboard.setPlayerScope(GKLeaderboardPlayerScope.Global);
					break;
				case Friends:
					leaderboard.setPlayerScope(GKLeaderboardPlayerScope.FriendsOnly);
					break;
				}
			}

			int perPage = options.itemsPerPage;
			if (perPage > 0) {
				leaderboard.setRange(new NSRange(1, perPage));
			}
		}
		
		return leaderboard;
	}

	@Override
	public void getSavedGames(final ServiceCallback<Iterable<SavedGame>> callback) {
		GKLocalPlayer.getLocalPlayer().fetchSavedGames(new VoidBlock2<NSArray<GKSavedGame>, NSError>() {
			@Override
			public void invoke(NSArray<GKSavedGame> savedGames, NSError error) {
				if (callback == null)
					return;
				
            	final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
            	if (response.isSuccessful()) {
					callback.onSuccess(
							new TransformIterable<GKSavedGame, SavedGame>(savedGames) {
								@Override
								protected SavedGame transform(GKSavedGame item) {
									return new GameCenterSavedGameWrapper(item);
								}
							}, response);
            	} else {
					callback.onFailure(response);
            	}
			}
		});
	}

	@Override
	public void loadSavedGameData(SavedGame metadata, final ServiceCallback<byte[]> callback) {
		GKSavedGame game = extractMetadata(metadata);

		game.loadData(new VoidBlock2<NSData, NSError>() {
			@Override
			public void invoke(NSData data, NSError error) {
				if (callback == null)
					return;

				final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
				if (response.isSuccessful()) {
					callback.onSuccess(data.getBytes(), response);
				} else {
					callback.onFailure(response);
				}
			}
		});
	}

	@Override
	public void submitSavedGame(SavedGame savedGame, byte[] data, final ServiceCallback<Void> callback) {
		NSData nsData = new NSData(data);
		
		String id = savedGame.getId();
		
		GKLocalPlayer.getLocalPlayer().saveGameData(nsData, id, new VoidBlock2<GKSavedGame, NSError>() {
			@Override 
			public void invoke(GKSavedGame savedGame, NSError error) {
				if (callback == null)
					return;

				final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
				if (response.isSuccessful()) {
					callback.onSuccess(null, response);
				} else {
					callback.onFailure(response);
				}
			}
		});
	}

	@Override
	public void deleteSavedGame(SavedGame savedGame, final ServiceCallback<Void> callback) {

		String id = savedGame.getId();

		GKLocalPlayer.getLocalPlayer().deleteSavedGames(id, new VoidBlock1<NSError>() {
			@Override
			public void invoke(NSError error) {
				if (callback == null)
					return;

				final GameCenterErrorWrapper response = new GameCenterErrorWrapper(error);
				if (response.isSuccessful()) {
					callback.onSuccess(null, response);
				} else {
					callback.onFailure(response);
				}
			}
		});
	}

	private static GKSavedGame extractMetadata(SavedGame savedGame) {
		if (savedGame instanceof GameCenterSavedGameWrapper) {
			return ((GameCenterSavedGameWrapper) savedGame).getWrapped();
		} else {
			throw new RuntimeException("GameCenterServicesHandler is only able to handle saved games coming from Game Center");
		}
	}


	/* Utils */

	protected void debug(String text) {

	}

	protected void error(String error) {

	}

	public static void alert(String text) {
		if (FoundationVersionNumber.getVersion() < FoundationVersionNumber.Version_iOS_8_0) {
			// Pre iOS8
			UIAlertView alert = new UIAlertView();
			alert.setTitle("Info");
			alert.setMessage(text);
			alert.addButton("Ok");
			alert.show();
		} else {
			// Post iOS8
			UIAlertController alert = new UIAlertController("Info", text, UIAlertControllerStyle.Alert);
			alert.addAction(new UIAlertAction("Ok", UIAlertActionStyle.Default, new VoidBlock1<UIAlertAction>() {
				@Override
				public void invoke(UIAlertAction action) { }
			}));
		}
	}
}