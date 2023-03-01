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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.spooky.gdx.gameservices.GameServicesHandler;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

public abstract class GdxGameServicesDemo implements ApplicationListener, GdxGameServicesDemoLogger {

	final String serviceName;
	final NativeFileChooser fileChooser;

	SpriteBatch batch;
	
	Stage stage;
	Skin skin;

	Preferences prefs;
	
	GameServicesHandler handler;

	Cell<GdxGameservicesDemoTable> contentCell;

	Table logTable;

	ScrollPane scrollingLog;
	
	public GdxGameServicesDemo(String serviceName, NativeFileChooser fileChooser) {
		super();
		this.serviceName = serviceName;
		this.fileChooser = fileChooser;
	}

	@Override
	public void create() {

		/* ************** */
		/* Initialization */
		/* ************** */
		
		prefs = Gdx.app.getPreferences("GdxGameservicesDemo");

		batch = new SpriteBatch();

		Camera camera = new OrthographicCamera();

		skin = new Skin(Gdx.files.internal("uiskin.json"));
		
		handler = buildHandler(prefs);

		Gdx.graphics.setTitle("gdx-gameservices demo -- " + serviceName);

		/* ***** */
		/* Title */
		/* ***** */
		
		Label nameLabel = new Label(serviceName, skin);

		/* ******* */
		/* Content */
		/* ******* */
		
		Table tabsTable = new Table(skin);
		ButtonGroup<TextButton> tabsGroup = new ButtonGroup<>();
		tabsGroup.setMinCheckCount(0);

		TextButton authenticationButton = new TextButton("Authentication", skin, "toggle");
		authenticationButton.addListener(new ChangeListener() {
			AuthenticationDemoTable table;
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (((Button) actor).isChecked()) {
					if (table == null) {
						table = buildAuthenticationTable(skin);
						table.initialize(handler);
						table.setLogger(GdxGameServicesDemo.this);
					}
					contentCell.setActor(table);
				}
			}
		});
		tabsGroup.add(authenticationButton);

		if (handler.handlesAchievements()) {
			TextButton achievementsButton = new TextButton("Achievements", skin, "toggle");
			achievementsButton.addListener(new ChangeListener() {
				AchievementsDemoTable table;
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (((Button) actor).isChecked()) {
						if (table == null) {
							table = buildAchievementsTable(skin);
							table.initialize(handler, prefs);
							table.setLogger(GdxGameServicesDemo.this);
						}
						contentCell.setActor(table);
					}
				}
			});
			tabsGroup.add(achievementsButton);
		}

		if (handler.handlesLeaderboards()) {
			TextButton leaderboardsButton = new TextButton("Leaderboards", skin, "toggle");
			leaderboardsButton.addListener(new ChangeListener() {
				LeaderboardsDemoTable table;
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (((Button) actor).isChecked()) {
						if (table == null) {
							table = buildLeaderboardsTable(skin);
							table.initialize(handler, prefs);
							table.setLogger(GdxGameServicesDemo.this);
						}
						contentCell.setActor(table);
					}
				}
			});
			tabsGroup.add(leaderboardsButton);
		}
		
		if (handler.handlesSavedGames()) {
			TextButton savedGamesButton = new TextButton("Saved games", skin, "toggle");
			savedGamesButton.addListener(new ChangeListener() {
				SavedGamesDemoTable table;
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (((Button) actor).isChecked()) {
						if (table == null) {
							table = buildSavedGamesTable(skin);
							table.initialize(handler, prefs, fileChooser);
							table.setLogger(GdxGameServicesDemo.this);
						}
						contentCell.setActor(table);
					}
				}
			});
			tabsGroup.add(savedGamesButton);
		}

		tabsTable.row();
		for (Actor actor : tabsGroup.getButtons())
			tabsTable.add(actor).expandX();

		/* ******* */
		/* Logging */
		/* ******* */
		
		logTable = new Table(skin);
		
		/* *********** */
		/* Stage setup */
		/* *********** */

		Table titleTable = new Table(skin);
		titleTable.row();
		titleTable.add(nameLabel).expandX().center().padTop(8f);

		Table contentTable = new Table(skin);
		contentTable.row();
		contentCell = contentTable.add((GdxGameservicesDemoTable) null).grow();
		
		Table logTableContainer = new Table(skin);
		logTableContainer.add(logTable).expand().top().left();
		scrollingLog = new ScrollPane(logTableContainer, skin);
		scrollingLog.setScrollingDisabled(true, false);
		
		SplitPane splitScrolls = new SplitPane(contentTable, scrollingLog, true, skin);
		splitScrolls.setSplitAmount(0.75f);
		splitScrolls.setMinSplitAmount(0.05f);
		splitScrolls.setMaxSplitAmount(0.95f);

		Table rootTable = new Table(skin);
		rootTable.setFillParent(true);
		rootTable.row();
		rootTable.add(titleTable).colspan(2).growX();
		rootTable.row();
		rootTable.add(tabsTable).growX().padTop(25f);
		rootTable.row();
		rootTable.add(splitScrolls).grow().padTop(25f);
		
		stage = new Stage(new ScreenViewport(camera), batch);
		stage.addActor(rootTable);

		/* ***** */
		/* Input */
		/* ***** */

		Gdx.input.setInputProcessor(stage);

		/* ******* */
		/* Startup */
		/* ******* */
		
		tabsGroup.getButtons().first().setChecked(true);
		tabsGroup.setMinCheckCount(1);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		skin.dispose();
	}

	@Override
	public void logInfo(final String message) {
		System.out.println(message);
		writeToLog("Info: " + message);
	}

	@Override
	public void logError(String message) {
		System.err.println(message);
		writeToLog("Error! " + message);
	}

	protected abstract GameServicesHandler buildHandler(Preferences preferences);

	protected AuthenticationDemoTable buildAuthenticationTable(Skin skin) {
		return new AuthenticationDemoTable(skin);
	}

	protected AchievementsDemoTable buildAchievementsTable(Skin skin) {
		return new AchievementsDemoTable(skin);
	}

	protected LeaderboardsDemoTable buildLeaderboardsTable(Skin skin) {
		return new LeaderboardsDemoTable(skin);
	}

	protected SavedGamesDemoTable buildSavedGamesTable(Skin skin) {
		return new SavedGamesDemoTable(skin);
	}
	
	private void writeToLog(final String message) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Label logLabel = new Label(message, skin);
				logLabel.setWrap(true);

				logTable.row();
				logTable.add(logLabel).growX();

				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						scrollingLog.setScrollPercentY(1f);
					}
				});
			}
		});
	}
}
