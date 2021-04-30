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
package games.spooky.gdx.gameservices.playtomic;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.ServiceCallback;
import games.spooky.gdx.gameservices.ServiceResponse;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementsHandler;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions.Collection;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;

public class PlaytomicServicesHandler implements ConnectionHandler, LeaderboardsHandler, AchievementsHandler {

	private final PlaytomicNet network;
	
	private final JsonObjectBuilder builder;
	
	private String playerName;
	private String playerId;
	private String playerSource;
	
	private ObjectMap<String, String> achievementCache;

	public PlaytomicServicesHandler() {
		this(new PlaytomicNet());
	}

	public PlaytomicServicesHandler(PlaytomicNet networkHandler) {
		super();
		this.builder = new JsonObjectBuilder();
		this.network = networkHandler;
	}

	public String getServer() {
		return network.getBaseUrl();
	}

	public void setServer(String server) {
		this.network.setBaseUrl(server);
	}

	public String getPublicKey() {
		return network.getPublicKey();
	}

	public void setPublicKey(String publicKey) {
		this.network.setPublicKey(publicKey);
	}

	public String getPrivateKey() {
		return network.getPrivateKey();
	}

	public void setPrivateKey(String privateKey) {
		this.network.setPrivateKey(privateKey);
	}

	@Override
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@Override
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	@Override
	public String getPlayerAvatarUrl() {
		return null;
	}

	public String getPlayerSource() {
		return playerSource;
	}

	public void setPlayerSource(String playerSource) {
		this.playerSource = playerSource;
	}

	@Override
	public boolean isLoggedIn() {
		return playerId != null;
	}

	@Override
	public void login(ServiceCallback<Void> callback) {
		if (playerId == null) {
			// Send back dummy response
			ServiceResponse dummyResponse = new PlaytomicResponse(false, 1);
			callback.onFailure(dummyResponse);
		} else {
			callback.onSuccess(null, new PlaytomicResponse(true, 0));
			// TODO Kinda ping server would be good
		}
	}

	@Override
	public void logout() {
		playerId = null;
	}

    private void checkConnection() {
		if (!isLoggedIn())
			throw new RuntimeException("No playerid provided");
	}

	@Override
	public void getAchievements(final ServiceCallback<Iterable<Achievement>> callback) {
		checkConnection();

		JsonObjectBuilder b = this.builder;

		b.newObject()
		.add("playerid", playerId)
		;

//    	private ObjectMap<String, ?> filters;
//    	private Array<String> friendsList;
    	
		network.send("achievements", "list", b.build(), PlaytomicAchievement[].class, new ServiceCallback<PlaytomicAchievement[]>() {
			@Override
			public void onSuccess(PlaytomicAchievement[] result, ServiceResponse response) {
				callback.onSuccess(new Array<Achievement>(result), response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}
	
	public void streamAchievements(final /*ListOptions options, final AchievementStreamHandler callback*/ServiceCallback<Iterable<Object/*PlayerAward*/>> callback) {
		network.send("achievements", "stream", null, Object[].class, new ServiceCallback<Object[]>() {
			@Override
			public void onSuccess(Object[] result, ServiceResponse response) {

//				ArrayList<PlayerAward> achievements = new ArrayList<PlayerAward>();
//				JSONArray acharray = data.optJSONArray("achievements");
//				int numachievements = data.optInt("numachievements");
//
//				for (int i = 0; i < acharray.length(); i++) {
//					JSONObject achjson = null;
//					try {
//						achjson = (JSONObject) acharray.get(i);
//					} catch (JSONException e) {
//						continue;
//					}
//					achievements.add(new PlayerAward(achjson));
//				}

				callback.onSuccess(new Array<Object>(result), response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	@Override
	public void unlockAchievement(final String achievementId, final ServiceCallback<Void> callback) {
		
		checkConnection();
		
		if (achievementCache == null) {
			// Initialize cache
			getAllAchievements(new ServiceCallback<Iterable<Achievement>>() {
				@Override
				public void onSuccess(Iterable<Achievement> result, ServiceResponse response) {
					if (response.isSuccessful() && result != null)
					achievementCache = new ObjectMap<String, String>();
					for (Achievement achievement : result) {
						achievementCache.put(achievement.getId(), achievement.getName());
					}
					unlockAchievement(achievementId, callback);	// Recursive call
				}
				
				@Override
				public void onFailure(ServiceResponse response) {
					callback.onFailure(response);
				}
			});
		} else {
			String achievementName = achievementCache.get(achievementId);
			if (achievementName == null) {
				callback.onFailure(new ServiceResponse() {
					@Override public boolean isSuccessful() { return false; }
					@Override public String getErrorMessage() {
						return "Unable to find achievement of id " + achievementId + " in achievement cache";
					}
					@Override public int getErrorCode() { return 0; }
				});
			} else {
				unlockAchievement(achievementId, achievementName, callback);	
			}
		}
	}
	
	public void getAllAchievements(final ServiceCallback<Iterable<Achievement>> callback) {

		checkConnection();

		network.send("achievements", "list", null, PlaytomicAchievement[].class, new ServiceCallback<PlaytomicAchievement[]>() {
			@Override
			public void onSuccess(PlaytomicAchievement[] result, ServiceResponse response) {
				callback.onSuccess(new Array<Achievement>(result), response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}
	
	public void unlockAchievement(String achievementId, String achievementName, ServiceCallback<Void> callback) {
		
		checkConnection();
		
		JsonObjectBuilder b = this.builder;

		b.newObject()
		.add("playerid", playerId)
		.add("playername", playerName)
		.add("achievementkey", achievementId)
		.add("achievement", achievementName)
		;
    	
//    	private ObjectMap<String, ?> fields;
//    	private Array<String> friendsList;
		
		network.send("achievements", "save", b.build(), Void.class, callback);
	}

	@Override
	public void getPlayerScore(String leaderboardId, LeaderboardOptions options, final ServiceCallback<LeaderboardEntry> callback) {
		// Prepare options, only one result per page
		if (options == null)
			options = new LeaderboardOptions();
		options.itemsPerPage = 1;

		long dummyScore = 0;
		
		// Send a dummy score of 0, then get resulting entry
		// FIXME Find some way to specify the dummy score
		saveAndList(leaderboardId, dummyScore, options, new ServiceCallback<Iterable<LeaderboardEntry>>() {
			@Override
			public void onSuccess(Iterable<LeaderboardEntry> result, ServiceResponse response) {
				LeaderboardEntry playerEntry = null;
				
				for (LeaderboardEntry entry : result) {
					playerEntry = entry;
					break;	// There should only be one
				}
				
				if (playerEntry == null) {
					// No proper entry == failure
					onFailure(response);
				} else {
					if (callback != null)
						callback.onSuccess(playerEntry, response);
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
	public void getScores(String leaderboardId, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {
		
		checkConnection();

		JsonObjectBuilder b = this.builder;

		b.newObject()
		.add("table", leaderboardId)
		;

		String action = "list";
		
		if (options == null || options.sort == null) {
			b.add("highest", true);
		} else {
			switch (options.sort) {
			case Top:
			default:
				b.add("highest", true);
				break;
			case Bottom:
				b.add("highest", false);
				break;
			case CenteredOnPlayer:
				// TODO
				break;
			}

			int perPage = options.itemsPerPage;
			if (perPage > 0)
				b.add("perpage", perPage);
		}
    	
//    	// Listing options
//    	private ObjectMap<String, ?> filters;
//    	private int page;
//    	private Array<String> friendsList;
		
    	network.send("leaderboards", action, b.build(), PlaytomicLeaderboardEntry[].class, new ServiceCallback<PlaytomicLeaderboardEntry[]>() {

			@Override
			public void onSuccess(PlaytomicLeaderboardEntry[] result, ServiceResponse response) {
				callback.onSuccess(new Array<LeaderboardEntry>(result), response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
    	});
	}

	@Override
	public void submitScore(String leaderboardId, long score, ServiceCallback<Void> callback) {
		
		checkConnection();
		
		JsonObjectBuilder b = this.builder;

		b.newObject()
		.add("table", leaderboardId)
		.add("playername", playerName)
		.add("playerid", playerId)
		.add("source", playerSource)
		.add("allowduplicates", false)
		.add("points", score)
		;
    	
//    	private ObjectMap<String, ?> fields;
//    	private ObjectMap<String, ?> filters;
//    	private String rDate;
//    	private String date;
//    	private int rank;
//    	
//    	// Listing options
//    	private boolean highest;
////    	private boolean lowest; = !highest
//    	private boolean submitted;
//    	private int perPage;
//    	private Array<String> friendsList;
    	
        network.send("leaderboards", "save", b.build(), Void.class, callback);
	}

	/**
     * Saves a score and fetches the high score table with the page corresponding
     * to the submitted score
     * @param score		score to send
     * @param callback	LeaderboardListHandler for receiving the response and data
     */ 
    public void saveAndList(String leaderboardId, long score, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {
		
		checkConnection();
		
		JsonObjectBuilder b = this.builder;

		b.newObject()
		.add("table", leaderboardId)
		.add("playername", playerName)
		.add("playerid", playerId)
		.add("source", playerSource)
		.add("excludeplayerid", false)
		.add("points", score)
		;
		
		boolean global = true;

		if (options != null) {
			int perPage = options.itemsPerPage;
			if (perPage > 0) {
				b.add("perpage", perPage);
			}
			Collection collection = options.collection;
			if (collection != null && collection == Collection.Friends) {
				global = false;
			}
		}
		
		b.add("global", global);
    	
        network.send("leaderboards", "saveandlist", b.build(), PlaytomicLeaderboardEntry[].class, new ServiceCallback<PlaytomicLeaderboardEntry[]>() {
			@Override
			public void onSuccess(PlaytomicLeaderboardEntry[] result, ServiceResponse response) {
				Array<LeaderboardEntry> array = new Array<LeaderboardEntry>(result);
				callback.onSuccess(array, response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
    }
}