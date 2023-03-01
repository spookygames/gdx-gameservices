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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpStatus;
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
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.games.Games;
import com.google.api.services.games.GamesScopes;
import com.google.api.services.games.model.*;
import games.spooky.gdx.gameservices.*;
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardOptions;
import games.spooky.gdx.gameservices.savedgame.SavedGame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static games.spooky.gdx.gameservices.googleplay.GooglePlayAsyncUtils.performAsyncRequest;

public class GooglePlayServicesHandler implements GameServicesHandler {

    private static final String LOCAL_PLAYER = "me";
    private static final String ALL_TIME_TIMESPAN = "ALL_TIME";
    private static final String COLLECTION_PUBLIC = "PUBLIC";
    private static final String COLLECTION_SOCIAL = "SOCIAL";
    private static final String APP_DATA = "appDataFolder";

    protected Games games;
    protected Drive drive;

    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    
    private GoogleClientSecrets clientSecrets;
    private FileDataStoreFactory dataStoreFactory;
    private HttpTransport httpTransport;

    private String applicationName;

    private String playerId;
    private String playerName;
    private String playerAvatarUrl;

    private boolean authenticated;
    private boolean authenticating;

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
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
	}

	// Authentication

	@Override
	public AsyncServiceResult<Boolean> isLoggedIn() {
        return new SyncSuccessServiceResult<>(authenticated);
	}

	@Override
	public AsyncServiceResult<Void> login() {
		boolean startLogin;
		synchronized (this) {
			startLogin = !authenticated && !authenticating;
			if (startLogin) {
				authenticating = true;
				playerName = null;
				playerAvatarUrl = null;
			}
		}
		if (startLogin) {
			return new CallbackAsyncServiceResult<Player, Void>() {
				@Override
				protected void callAsync(CallbackAsyncServiceResult.Callback<Player> callback) {
					try {
						// Set up authorization code flow
						String userID = getSystemUserName();

						ArrayList<String> scopes = new ArrayList<>(2);
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

						AuthorizationCodeInstalledApp authorizer = new AuthorizationCodeInstalledApp(flow, receiver) {
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

						Player player = games.players().get(LOCAL_PLAYER).execute();

						synchronized (this) {
							authenticated = true;

							// Initialize local player
							playerId = player.getPlayerId();
							playerName = player.getDisplayName();
							playerAvatarUrl = player.getAvatarImageUrl();
						}

						callback.onSuccess(player);
					} catch (IOException e) {
						callback.onError(e);
					} finally {
						synchronized (this) {
							authenticating = false;
						}
					}
				}

				@Override
				protected Void transformResult(Player result) {
					return null;
				}
			};
		} else {
			return new SyncSuccessServiceResult<>(null);
		}
	}

	protected String getSystemUserName() {
		return System.getProperty("user.name");
	}

	public void logout() {
		synchronized (this) {
			if (authenticated) {
				authenticated = false;
				playerName = null;
				playerAvatarUrl = null;
				games = null;
				drive = null;
			}
		}
	}

	@Override
	public AsyncServiceResult<String> getPlayerId() {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new SyncSuccessServiceResult<>(playerId);
    }

	@Override
	public AsyncServiceResult<String> getPlayerName() {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
        return new SyncSuccessServiceResult<>(playerName);
	}

	@Override
	public AsyncServiceResult<byte[]> getPlayerAvatar() {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new CallbackAsyncServiceResult<Net.HttpResponse, byte[]>() {
			@Override
			protected void callAsync(final CallbackAsyncServiceResult.Callback<Net.HttpResponse> callback) {
				Net.HttpRequest httpRequest = Pools.obtain(Net.HttpRequest.class);
				httpRequest.setMethod(Net.HttpMethods.GET);
				httpRequest.setUrl(playerAvatarUrl);

				Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
					@Override
					public void handleHttpResponse(Net.HttpResponse httpResponse) {
						final int statusCode = httpResponse.getStatus().getStatusCode();
						if (statusCode == HttpStatus.SC_OK) {
							callback.onSuccess(httpResponse);
						} else {
							callback.onError(new RuntimeException("HTTP Error " + statusCode));
						}
					}

					@Override
					public void failed(Throwable t) {
						callback.onError(t);
					}

					@Override
					public void cancelled() {
						callback.onError(new RuntimeException("Operation cancelled"));
					}
				});
			}

			@Override
			protected byte[] transformResult(Net.HttpResponse result) {
				return result.getResult();
			}
		};
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
	public boolean handlesAchievements() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<Achievement>> getAchievements() {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new GooglePlayAsyncServiceResultFromRequest<PlayerAchievementListResponse, Iterable<Achievement>>() {
			@Override
			protected AbstractGoogleClientRequest<PlayerAchievementListResponse> createRequest() throws IOException {
				return games.achievements().list(LOCAL_PLAYER);
			}

			@Override
			protected Iterable<Achievement> transformResult(PlayerAchievementListResponse result) {
				return new TransformIterable<PlayerAchievement, Achievement>(result.getItems()) {
					@Override
					protected Achievement transform(PlayerAchievement item) {
						return new GooglePlayAchievement(item);
					}
				};
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> unlockAchievement(final String achievementId) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new GooglePlayAsyncServiceResultFromRequest<AchievementUnlockResponse, Void>() {
			@Override
			protected AbstractGoogleClientRequest<AchievementUnlockResponse> createRequest() throws IOException {
				return games.achievements().unlock(achievementId);
			}

			@Override
			protected Void transformResult(AchievementUnlockResponse result) {
				return null;
			}
		};
	}

	// Leaderboards

	@Override
	public boolean handlesLeaderboards() {
		return true;
	}

	@Override
	public AsyncServiceResult<LeaderboardEntry> getPlayerScore(final String leaderboardId, final LeaderboardOptions options) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new GooglePlayAsyncServiceResultFromRequest<PlayerLeaderboardScoreListResponse, LeaderboardEntry>() {
			@Override
			protected AbstractGoogleClientRequest<PlayerLeaderboardScoreListResponse> createRequest() throws IOException {
				return games.scores().get(playerId, leaderboardId, ALL_TIME_TIMESPAN);
			}

			@Override
			protected LeaderboardEntry transformResult(PlayerLeaderboardScoreListResponse result) {
				List<PlayerLeaderboardScore> items = result.getItems();
				int size = items.size();
				if (size < 1) {
					throw new RuntimeException("No score found for player " + playerName);
				} else {
					if (size > 1)
						error("Multiple scores found for player " + playerName + ", taking first one");

					final PlayerLeaderboardScore entry = items.get(0);
					final boolean social = options != null && options.getScope() == LeaderboardOptions.Scope.Friends;
					return new LeaderboardEntry() {
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
					};
				}
			}
		};
	}

	@Override
	public AsyncServiceResult<Iterable<LeaderboardEntry>> getScores(final String leaderboardId, LeaderboardOptions options) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));

		final boolean top;
		final String collection;
		final int items;
		if (options == null) {
			top = true;
			collection = COLLECTION_PUBLIC;
			items = 20;
		} else {
			LeaderboardOptions.Window window = options.getWindow();
			if (window == null) {
				top = true;
			} else {
				switch (window) {
				case CenteredOnPlayer:
					top = false;
					break;
				case Top:
				default:
					top = true;
					break;
				}
			}
			LeaderboardOptions.Scope c = options.getScope();
			if (c == LeaderboardOptions.Scope.Friends) {
				collection = COLLECTION_SOCIAL;
			} else {
				collection = COLLECTION_PUBLIC;
			}

			int perPage = options.getMaxResults();
			if (perPage > 0)
				items = perPage;
			else
				items = 20;
		}

		return new GooglePlayAsyncServiceResultFromRequest<LeaderboardScores, Iterable<LeaderboardEntry>>() {
			@Override
			protected AbstractGoogleClientRequest<LeaderboardScores> createRequest() throws IOException {
				return top ?
					games.scores().listWindow(leaderboardId, collection, ALL_TIME_TIMESPAN).setMaxResults(items) :
					games.scores().list(leaderboardId, collection, ALL_TIME_TIMESPAN).setMaxResults(items);
			}

			@Override
			protected Iterable<LeaderboardEntry> transformResult(LeaderboardScores result) {
				return new TransformIterable<com.google.api.services.games.model.LeaderboardEntry, LeaderboardEntry>(result.getItems()) {
					@Override
					protected LeaderboardEntry transform(com.google.api.services.games.model.LeaderboardEntry item) {
						return new GooglePlayLeaderboardEntry(item);
					}
				};
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitScore(final String leaderboardId, final long score) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new GooglePlayAsyncServiceResultFromRequest<PlayerScoreResponse, Void>() {
			@Override
			protected AbstractGoogleClientRequest<PlayerScoreResponse> createRequest() throws IOException {
				return games.scores().submit(leaderboardId, score);
			}

			@Override
			protected Void transformResult(PlayerScoreResponse result) {
				return null;
			}
		};
	}

	// Saved games

	@Override
	public boolean handlesSavedGames() {
		return true;
	}

	@Override
	public AsyncServiceResult<Iterable<SavedGame>> getSavedGames() {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new GooglePlayAsyncServiceResultFromRequest<FileList, Iterable<SavedGame>>() {
			@Override
			protected AbstractGoogleClientRequest<FileList> createRequest() throws IOException {
				return drive.files()
						.list()
						.setSpaces(APP_DATA)
						.setFields("files(id, name, description, modifiedTime, createdTime)")
						;
			}

			@Override
			protected Iterable<SavedGame> transformResult(FileList result) {
				return new TransformIterable<File, SavedGame>(result.getFiles()) {
					@Override
					protected SavedGame transform(File item) {
						return new GooglePlaySavedGame(item);
					}
				};
			}
		};
	}

	@Override
	public AsyncServiceResult<byte[]> loadSavedGameData(final SavedGame save) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new CallbackAsyncServiceResult<byte[], byte[]>() {
			@Override
			protected void callAsync(final CallbackAsyncServiceResult.Callback<byte[]> callback) {
				resolveSavedGame(save, new CallbackAsyncServiceResult.Callback<File>() {
					@Override
					public void onSuccess(final File remoteFile) {
						if (remoteFile == null) {
							callback.onError(new RuntimeException("File not found: " + save.getTitle()));
						} else {
							performAsyncRequest(new Callable<byte[]>() {
								@Override
								public byte[] call() throws Exception {
									InputStream stream = null;
									try {
										stream = drive.files().get(remoteFile.getId()).executeMediaAsInputStream();
										return StreamUtils.copyStreamToByteArray(stream);
									} finally {
										StreamUtils.closeQuietly(stream);
									}
								}
							}, callback);
						}
					}

					@Override
					public void onError(Throwable error) {
						callback.onError(error);
					}
				});
			}

			@Override
			protected byte[] transformResult(byte[] result) {
				return result;
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> submitSavedGame(final SavedGame save, final byte[] data) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new CallbackAsyncServiceResult<File, Void>() {
			@Override
			protected void callAsync(final CallbackAsyncServiceResult.Callback<File> callback) {
				resolveSavedGame(save, new CallbackAsyncServiceResult.Callback<File>() {
					@Override
					public void onSuccess(final File remoteFile) {
						// No type since it is binary data
						final AbstractInputStreamContent mediaContent = new ByteArrayContent(null, data);

						performAsyncRequest(new Callable<File>() {
							@Override
							public File call() throws Exception {
								if (remoteFile != null) {
									// File exists, update it
									File fileMetadata = new File()
											.setName(save.getTitle())
											.setDescription(save.getDescription())
											.setModifiedTime(new DateTime(save.getTimestamp()));

									return drive.files().update(remoteFile.getId(), fileMetadata, mediaContent).execute();
								} else {

									// File doesn't exist, create it
									File fileMetadata = new File()
											.setName(save.getTitle())
											.setDescription(save.getDescription())
											.setModifiedTime(new DateTime(save.getTimestamp()))
											.setCreatedTime(new DateTime(System.currentTimeMillis()))
											.setParents(Collections.singletonList(APP_DATA));

									return drive.files().create(fileMetadata, mediaContent).execute();
								}
							}
						}, callback);
					}

					@Override
					public void onError(Throwable error) {
						callback.onError(error);
					}
				});
			}

			@Override
			protected Void transformResult(File result) {
				return null;
			}
		};
	}

	@Override
	public AsyncServiceResult<Void> deleteSavedGame(final SavedGame save) {
		if (!authenticated) return new SyncErrorServiceResult<>(SimpleServiceError.error("Not authenticated"));
		return new CallbackAsyncServiceResult<Void, Void>() {
			@Override
			protected void callAsync(final CallbackAsyncServiceResult.Callback<Void> callback) {
				resolveSavedGame(save, new CallbackAsyncServiceResult.Callback<File>() {
					@Override
					public void onSuccess(final File remoteFile) {
						if (remoteFile == null) {
							callback.onSuccess(null);
						} else {
							performAsyncRequest(new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									return drive.files().delete(remoteFile.getId()).execute();
								}
							}, callback);
						}
					}

					@Override
					public void onError(Throwable error) {
						callback.onError(error);
					}
				});
			}

			@Override
			protected Void transformResult(Void result) {
				return result;
			}
		};
	}

	private void resolveSavedGame(final SavedGame savedGame, final CallbackAsyncServiceResult.Callback<File> callback) {
		if (savedGame instanceof GooglePlaySavedGame) {
			if (callback != null)
				callback.onSuccess(((GooglePlaySavedGame) savedGame).getWrapped());
		} else {
			// Open from API in order to get proper metadata (or create if none)

			// Find file by name
			final String name = savedGame.getTitle();
			performAsyncRequest(new Callable<FileList>() {
				@Override
				public FileList call() throws Exception {
					return drive.files().list()
							.setOrderBy("modifiedTime desc")
							.setSpaces(APP_DATA)
							.setQ("name='" + name + "'")
							.execute();
				}
			}, new CallbackAsyncServiceResult.Callback<FileList>() {
				@Override
				public void onSuccess(FileList result) {
					List<File> files = result.getFiles();
					int size = files.size();
					if (size < 1) {
						error("No file found with name " + name);
						callback.onSuccess(null);
					} else {
						if (size > 1)
							error("Multiple files exist with name " + name + ", taking first one");
						callback.onSuccess(files.get(0));
					}
				}

				@Override
				public void onError(Throwable error) {
					callback.onError(error);
				}
			});
		}
	}

	// Utilities

	protected void error(String error) {
		// Override me
	}

}
