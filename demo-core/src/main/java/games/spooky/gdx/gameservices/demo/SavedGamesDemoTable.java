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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import games.spooky.gdx.gameservices.ServiceCompletionCallback;
import games.spooky.gdx.gameservices.ServiceError;
import games.spooky.gdx.gameservices.savedgame.SavedGame;
import games.spooky.gdx.gameservices.savedgame.SavedGamesHandler;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

public class SavedGamesDemoTable extends GdxGameservicesDemoTable {
	
	TextField savedGameIdTextField;
	String defaultDirectory;

	public SavedGamesDemoTable(Skin skin) {
		super(skin);
	}
	
	public void initialize(final SavedGamesHandler savedGames, final Preferences prefs, final NativeFileChooser fileChooser) {

		defaultDirectory = Gdx.files.isExternalStorageAvailable() ? 
				Gdx.files.getExternalStoragePath()
				: (Gdx.files.isLocalStorageAvailable() ?
						Gdx.files.getLocalStoragePath()
						: System.getProperty("user.home"));
		
		Skin skin = getSkin();
		
		// Get games
		TextButton gamesButton = new TextButton("Get games", skin);
		gamesButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {

				log("Get saved games...");
				try {
					savedGames.getSavedGames().onCompletion(new ServiceCompletionCallback<Iterable<SavedGame>>() {
						@Override
						public void onSuccess(Iterable<SavedGame> result) {
							log("Received saved games");
							for (SavedGame entry : result)
								log(entry.toString());
						}

						@Override
						public void onError(ServiceError error) {
							error(error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Score value
		savedGameIdTextField = new TextField(prefs.getString("savedgames_savedgameid", ""), skin);
		savedGameIdTextField.setMessageText("Saved game ID");
		
		// Load saved game
		TextButton loadSavedGameButton = new TextButton("Load saved game", skin);
		loadSavedGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				String id = savedGameIdTextField.getText();

				prefs.putString("savedgames_savedgameid", id);
				prefs.flush();

				try {
					log("Load saved game of id " + id + "...");

					savedGames.loadSavedGameData(new SimpleMetadata(id)).onCompletion(new ServiceCompletionCallback<byte[]>() {
						@Override
						public void onSuccess(byte[] result) {
							log("Received data of length " + result.length);
						}

						@Override
						public void onError(ServiceError error) {
							error(error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Submit saved game
		TextButton submitSavedGameButton = new TextButton("Submit saved game...", skin);
		submitSavedGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				NativeFileChooserConfiguration configuration = new NativeFileChooserConfiguration();
				configuration.directory = Gdx.files.absolute(prefs.getString("savedgames_lastdirectory", defaultDirectory));
				configuration.title = "Choose saved game file to submit";

				try {
					fileChooser.chooseFile(configuration, new NativeFileChooserCallback() {
						@Override public void onFileChosen(FileHandle file) {
							try {
								final String id = savedGameIdTextField.getText();

								prefs.putString("savedgames_savedgameid", id);
								prefs.flush();

								log("Submit saved game of id " + id + "...");

								savedGames.submitSavedGame(new SimpleMetadata(id), file.readBytes()).onCompletion(new ServiceCompletionCallback<Void>() {
									@Override
									public void onSuccess(Void result) {
										log("Submitted saved game of id " + id);
									}

									@Override
									public void onError(ServiceError error) {
										error(error.getErrorMessage());
									}
								});
							} catch (Exception e) {
								error(e.getLocalizedMessage());
							}
						}
						
						@Override
						public void onError(Exception exception) {
							error(exception.getLocalizedMessage());
						}
						
						@Override
						public void onCancellation() {
							error("File choosing cancellation");
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		// Delete saved game
		TextButton deleteSavedGameButton = new TextButton("Delete saved game", skin);
		deleteSavedGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final String id = savedGameIdTextField.getText();

				prefs.putString("savedgames_savedgameid", id);
				prefs.flush();

				try {
					log("Delete saved game of id " + id + "...");

					savedGames.deleteSavedGame(new SimpleMetadata(id)).onCompletion(new ServiceCompletionCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							log("Deleted saved game of id " + id);
						}

						@Override
						public void onError(ServiceError error) {
							error(error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});
		
		row();
		add(gamesButton);
		
		row();
		add(savedGameIdTextField);
		
		row();
		add(loadSavedGameButton);
		
		row();
		add(submitSavedGameButton);
		
		row();
		add(deleteSavedGameButton);
	}
	
	private static class SimpleMetadata implements SavedGame {
		
		String id;
		
		public SimpleMetadata(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return id;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public long getTimestamp() {
			return 0;
		}

		@Override
		public long getPlayedTime() {
			return 0;
		}

		@Override
		public String getDeviceName() {
			return null;
		}
		
	}
	
}
