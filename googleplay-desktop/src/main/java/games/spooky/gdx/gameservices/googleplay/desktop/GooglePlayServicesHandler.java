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
package games.spooky.gdx.gameservices.googleplay.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.StreamUtils;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.games.Games;
import com.google.api.services.games.GamesRequest;
import com.google.api.services.games.GamesScopes;
import com.google.api.services.games.model.*;
import games.spooky.gdx.gameservices.*;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementsHandler;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardsHandler;
import games.spooky.gdx.gameservices.savedgame.SavedGame;
import games.spooky.gdx.gameservices.savedgame.SavedGamesHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GooglePlayServicesHandler implements ConnectionHandler, AchievementsHandler, LeaderboardsHandler, SavedGamesHandler {

    private static final String LOCAL_PLAYER = "me";
    private static final String ALL_TIME_TIMESPAN = "ALL_TIME";
    private static final String COLLECTION_PUBLIC = "PUBLIC";
    private static final String COLLECTION_SOCIAL = "SOCIAL";
    private static final String APP_DATA = "appDataFolder";
    
    protected Games games;
    protected Drive drive;

    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    
    private GoogleClientSecrets clientSecrets;
    private FileDataStoreFactory dataStoreFactory;
    private HttpTransport httpTransport;

    private String applicationName;

    private String playerId;
    private String playerName;
    private String playerAvatarUrl;

    private boolean connected;
    private boolean connecting;

	// Lifecycle

    public void initialize(String applicationName, FileHandle clientSecretFile) {
        initialize(applicationName, clientSecretFile.read());
    }

    public void initialize(String applicationName, FileHandle clientSecretFile, String dataStoreDirectory) {
        initialize(applicationName, clientSecretFile.read(), dataStoreDirectory);
    }

    public void initialize(String applicationName, String clientId, String clientSecret) {
        initialize(applicationName, clientCredentialsToStream(clientId, clientSecret));
    }

    public void initialize(String applicationName, String clientId, String clientSecret, String dataStoreDirectory) {
        initialize(applicationName, clientCredentialsToStream(clientId, clientSecret), dataStoreDirectory);
    }
    
    private static InputStream clientCredentialsToStream(String clientId, String clientSecret) {
		String clientSecretsJson = "{ \"installed\": { \"client_id\": \"" + clientId + "\", \"client_secret\": \"" + clientSecret + "\" }	}";
		return new ByteArrayInputStream(clientSecretsJson.getBytes());
    }

    public void initialize(String applicationName, InputStream clientSecret) {
    	initialize(applicationName, clientSecret, System.getProperty("user.home") + "/.store/" + applicationName);
    }

    public void initialize(String applicationName, InputStream clientSecret, String dataStoreDirectory) {

        this.applicationName = applicationName;

        try {
            clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(clientSecret));
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(new java.io.File(dataStoreDirectory));
        } catch (GeneralSecurityException e) {
            throw new GdxRuntimeException(e);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

	// Connection

	@Override
	public boolean isLoggedIn() {
        return connected;
	}

	@Override
	public void login(final ServiceCallback<Void> callback) {
		if (!isLoggedIn() && !connecting) {
			// Make sure we do not try twice concurrently
            connecting = true;
            playerName = null;
            playerAvatarUrl = null;

            // Initiate connection on a separate thread
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            // Set up authorization code flow
                        	String userID = System.getProperty("user.name");

                        	ArrayList<String> scopes = new ArrayList<String>(2);
                            scopes.add(GamesScopes.GAMES);
                            scopes.add(GamesScopes.DRIVE_APPDATA);

                            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                            		.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
                            		.setDataStoreFactory(dataStoreFactory)
                            		.build();

                            LocalServerReceiver receiver = new LocalServerReceiver
                            		.Builder()
                            		.setHost(authenticationHost())
                            		.setPort(authenticationPort())
                            		.build();

                            // Start authorization
                            AuthorizationCodeInstalledApp authorizer = new AuthorizationCodeInstalledApp(flow, receiver) {
                                // Override open browser not working well on Linux and maybe other OSes.
                                protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                                    Gdx.net.openURI(authorizationUrl.build());
                                }
                            };
                            Credential credential = authorizer.authorize(userID);

                            games = new Games
                            		.Builder(httpTransport, jsonFactory, credential)
                            		.setApplicationName(applicationName)
                            		.build();
                            drive = new Drive
                            		.Builder(httpTransport, jsonFactory, credential)
                            		.setApplicationName(applicationName)
                            		.build();

                            connected = true;

                            // Initialize local player
                            Player player = games.players().get(LOCAL_PLAYER).execute();
                            playerId = player.getPlayerId();
                            playerName = player.getDisplayName();
                            playerAvatarUrl = player.getAvatarImageUrl();

                            // Eventually, success callback
                            if (callback != null)
                            	callback.onSuccess(null, new GooglePlayServicesResponse());

                        } catch (IOException e) {
                        	// Authentication error
                        	if (callback != null)
                                callback.onFailure(new GooglePlayServicesResponse(e));
                        }

                    } finally {
                        connecting = false;
                    }
                }
            }.start();
		}
	}

	public void logout() {
		if (isLoggedIn()) {
	        connected = false;
	        playerName = null;
	        playerAvatarUrl = null;
	        games = null;
	        drive = null;
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

	@Override
	public void getPlayerAvatar(final ServiceCallback<byte[]> callback) {

		Net.HttpRequest httpRequest = Pools.obtain(Net.HttpRequest.class);
		httpRequest.setMethod(Net.HttpMethods.GET);
		httpRequest.setUrl(playerAvatarUrl);

		Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse(Net.HttpResponse httpResponse) {
				final int statusCode = httpResponse.getStatus().getStatusCode();
				debug("Downloaded image " + playerAvatarUrl + ": " + statusCode);
				if (callback != null) {
					if (statusCode == HttpStatus.SC_OK) {
						callback.onSuccess(httpResponse.getResult(), PlainServiceResponse.success());
					} else {
						callback.onFailure(PlainServiceResponse.error(-3, "HTTP Error " + statusCode));
					}
				}
			}

			@Override
			public void failed(Throwable t) {
				if (callback != null)
					callback.onFailure(PlainServiceResponse.error(-4, t.getMessage()));
			}

			@Override
			public void cancelled() {
				if (callback != null)
					callback.onFailure(PlainServiceResponse.error(-2, "Operation cancelled"));
			}
		});
	}

	/**
	 * Hostname used by the authentication mechanism. Defaults to localhost, override at will.
	 * @return the hostname used by the authentication mechanism
	 */
	protected String authenticationHost() {
		return "localhost";
	}
	
	/**
	 * Port used by the authentication mechanism. Defaults to -1 (random), override at will.
	 * @return the port used by the authentication mechanism
	 */
	protected int authenticationPort() {
		return -1;
	}

	// Achievements

	@Override
	public void getAchievements(final ServiceCallback<Iterable<Achievement>> callback) {

		GamesRequest<PlayerAchievementListResponse> request;

		try {
			request = games.achievements().list(LOCAL_PLAYER);
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<PlayerAchievementListResponse>() {

			@Override
			public void onSuccess(PlayerAchievementListResponse result, ServiceResponse response) {
				callback.onSuccess(
						new TransformIterable<PlayerAchievement, Achievement>(result.getItems()) {
							@Override
							protected Achievement transform(PlayerAchievement item) {
								return new GooglePlayAchievementWrapper(item);
							}
						}, response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	@Override
	public void unlockAchievement(String achievementId, final ServiceCallback<Void> callback) {

		GamesRequest<AchievementUnlockResponse> request;

		try {
			request = games.achievements().unlock(achievementId);
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<AchievementUnlockResponse>() {
			@Override
			public void onSuccess(AchievementUnlockResponse result, ServiceResponse response) {
				callback.onSuccess(null, response);
			}
			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	// Leaderboards

	@Override
	public void getPlayerScore(String leaderboardId, final LeaderboardOptions options, final ServiceCallback<LeaderboardEntry> callback) {

		GamesRequest<PlayerLeaderboardScoreListResponse> request;

		try {
			request = games.scores().get(playerId, leaderboardId, ALL_TIME_TIMESPAN);
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<PlayerLeaderboardScoreListResponse>() {
			@Override
			public void onSuccess(PlayerLeaderboardScoreListResponse result, ServiceResponse response) {
				List<PlayerLeaderboardScore> items = result.getItems();
				int size = items.size();
		        if (size < 1) {
		        	error("No score found for player " + playerName);
					callback.onFailure(response);
		        } else {
		        	if (size > 1)
			        	error("Multiple scores found for player " + playerName + ", taking first one");

					final PlayerLeaderboardScore entry = items.get(0);
					final boolean social = options != null && options.collection == LeaderboardOptions.Collection.Friends;
					callback.onSuccess(new LeaderboardEntry() {
						@Override public String getSource() {
							return "";
						}
						
						@Override public long getScore() {
							return entry.getScoreValue();
						}
						
						@Override public long getRank() {
							return (social ? entry.getSocialRank() : entry.getPublicRank()).getRank();
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
							return new Date(entry.getWriteTimestamp());
						}
					}, response);
		        }
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	@Override
	public void getScores(String leaderboardId, LeaderboardOptions options, final ServiceCallback<Iterable<LeaderboardEntry>> callback) {
		boolean top;
		String collection;
		int items;

		if (options == null) {
			top = true;
			collection = COLLECTION_PUBLIC;
			items = 20;
		} else {
			LeaderboardOptions.Sort sort = options.sort;
			if (sort == null) {
				top = true;
			} else {
				switch (sort) {
				case CenteredOnPlayer:
					top = false;
					break;
				case Bottom:
					error("Sort.Bottom is not available for Google Play Games (desktop), using Sort.Top instead");
				case Top:
				default:
					top = true;
					break;
				}
			}
			LeaderboardOptions.Collection c = options.collection;
			if (c == LeaderboardOptions.Collection.Friends) {
				collection = COLLECTION_SOCIAL;
			} else {
				collection = COLLECTION_PUBLIC;
			}

			int perPage = options.itemsPerPage;
			if (perPage > 0)
				items = perPage;
			else
				items = 20;
		}

		GamesRequest<LeaderboardScores> request;

		try {
			if (top) {
				request = games.scores().listWindow(leaderboardId, collection, ALL_TIME_TIMESPAN).setMaxResults(items);
			} else {
				request = games.scores().list(leaderboardId, collection, ALL_TIME_TIMESPAN).setMaxResults(items);
			}
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<LeaderboardScores>() {
			@Override
			public void onSuccess(LeaderboardScores result, ServiceResponse response) {
				callback.onSuccess(
						new TransformIterable<com.google.api.services.games.model.LeaderboardEntry, LeaderboardEntry>(result.getItems()) {
							@Override
							protected LeaderboardEntry transform(com.google.api.services.games.model.LeaderboardEntry item) {
								return new GooglePlayLeaderboardScoreWrapper(item);
							}
						}, response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	@Override
	public void submitScore(String leaderboardId, long score, final ServiceCallback<Void> callback) {

		GamesRequest<PlayerScoreResponse> request;

		try {
			request = games.scores().submit(leaderboardId, score);
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<PlayerScoreResponse>() {
			@Override
			public void onSuccess(PlayerScoreResponse result, ServiceResponse response) {
				callback.onSuccess(null, new GooglePlayServicesResponse());
			}
			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	// Saved games

	@Override
	public void getSavedGames(final ServiceCallback<Iterable<SavedGame>> callback) {

		com.google.api.services.drive.Drive.Files.List request;

		try {
			request = drive.files()
					.list()
					.setSpaces(APP_DATA)
					.setFields("files(id, name, description, modifiedTime, createdTime)")
					;
		} catch (IOException exception) {
			if (callback != null)
				callback.onFailure(new GooglePlayServicesResponse(exception));
			return;
		}

		background(request, callback == null ? null : new ServiceCallback<FileList>() {
			@Override
			public void onSuccess(FileList result, ServiceResponse response) {
				callback.onSuccess(
						new TransformIterable<File, SavedGame>(result.getFiles()) {
							@Override
							protected SavedGame transform(File item) {
								return new GooglePlaySnapshotWrapper(item);
							}
						}, response);
			}

			@Override
			public void onFailure(ServiceResponse response) {
				callback.onFailure(response);
			}
		});
	}

	@Override
	public void loadSavedGameData(SavedGame save, final ServiceCallback<byte[]> callback) {
		extractMetadata(save, new ServiceCallback<File>() {
			@Override
			public void onSuccess(File file, ServiceResponse response) {
				if (file == null) {
					if (callback != null)
						callback.onFailure(response);
				} else {
			        InputStream stream = null;
			        byte[] data = null;
			        try {
		                stream = drive.files().get(file.getId()).executeMediaAsInputStream();
		                data = StreamUtils.copyStreamToByteArray(stream);
						if (callback != null)
							callback.onSuccess(data, response);
			        } catch (IOException e) {
						if (callback != null)
							callback.onFailure(new GooglePlayServicesResponse(e));
					} finally {
			            StreamUtils.closeQuietly(stream);
			        }
			        
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
		extractMetadata(save, new ServiceCallback<File>() {
			@Override
			public void onSuccess(File remoteFile, ServiceResponse response) {

				// No type since it is binary data
				AbstractInputStreamContent mediaContent = new ByteArrayContent(null, data);

				final AbstractGoogleClientRequest<File> request;

				try {

					if (remoteFile != null) {
						// File exists, update it
						File fileMetadata = new File()
								.setName(save.getTitle())
								.setDescription(save.getDescription())
								.setModifiedTime(new DateTime(save.getTimestamp()))
								;

						request = drive.files().update(remoteFile.getId(), fileMetadata, mediaContent);
					} else {

						// File doesn't exist, create it
						File fileMetadata = new File()
								.setName(save.getTitle())
								.setDescription(save.getDescription())
								.setModifiedTime(new DateTime(save.getTimestamp()))
								.setCreatedTime(new DateTime(System.currentTimeMillis()))
								.setParents(Collections.singletonList(APP_DATA))
								;

						request = drive.files().create(fileMetadata, mediaContent);
					}
				} catch (IOException e) {
					if (callback != null)
						callback.onFailure(new GooglePlayServicesResponse(e));
					return;
				}

				background(request, callback == null ? null : new ServiceCallback<File>() {

					@Override
					public void onSuccess(File result, ServiceResponse response) {
						callback.onSuccess(null, response);
					}

					@Override
					public void onFailure(ServiceResponse response) {
						callback.onFailure(response);
					}
				});
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
		extractMetadata(save, new ServiceCallback<File>() {
			@Override
			public void onSuccess(File remoteFile, ServiceResponse response) {
		        if (remoteFile == null) {
		        	if (callback != null)
		        		callback.onSuccess(null, response);
		        } else {
		    		try {
			        	background(drive.files().delete(remoteFile.getId()), callback);
		    		} catch (IOException e) {
		    			if (callback != null)
		    				callback.onFailure(new GooglePlayServicesResponse(e));
		    		}
		        }
			}

			@Override
			public void onFailure(ServiceResponse response) {
				if (callback != null)
					callback.onFailure(response);
			}
		});
	}

	private void extractMetadata(final SavedGame savedGame, final ServiceCallback<File> callback) {
		if (savedGame instanceof GooglePlaySnapshotWrapper) {
			if (callback != null)
				callback.onSuccess(((GooglePlaySnapshotWrapper) savedGame).getWrapped(), null);
		} else {
			// Open from API in order to get proper metadata (or create if none)

			// Find file by name
			final String name = savedGame.getTitle();
			com.google.api.services.drive.Drive.Files.List request;
			try {
				request = drive.files().list()
						.setOrderBy("modifiedTime desc")
						.setSpaces(APP_DATA)
						.setQ("name='" + name + "'")
						;
			} catch (IOException exception) {
				if (callback != null)
					callback.onFailure(new GooglePlayServicesResponse(exception));
				return;
			}

			background(request, new ServiceCallback<FileList>() {
				@Override
				public void onSuccess(FileList result, ServiceResponse response) {
					List<File> files = result.getFiles();
					int size = files.size();
					if (size < 1) {
						error("No file found with name " + name);
						if (callback != null)
							callback.onSuccess(null, response);
					} else {
						if (size > 1)
							error("Multiple files exist with name " + name + ", taking first one");
						if (callback != null)
							callback.onSuccess(files.get(0), response);
					}
				}

				@Override
				public void onFailure(ServiceResponse response) {
					if (callback != null)
						callback.onFailure(response);
				}
			});
		}
	}

	// Utilities

	protected void debug(String text) {
		// Override me
	}

	protected void error(String error) {
		// Override me
	}

    private <T> void background(final AbstractGoogleClientRequest<T> request, final ServiceCallback<T> callback) {
        new Thread() {
			@Override public void run() {
				try {
					T result = request.execute();
					if (callback != null)
						callback.onSuccess(result, new GooglePlayServicesResponse());
				} catch (IOException e) {
					if (callback != null)
						callback.onFailure(new GooglePlayServicesResponse(e));
				}
			}
		}.start();
    }
}
