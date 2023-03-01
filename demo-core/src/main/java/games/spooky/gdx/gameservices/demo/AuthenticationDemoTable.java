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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import games.spooky.gdx.gameservices.ServiceCompletionCallback;
import games.spooky.gdx.gameservices.ServiceError;
import games.spooky.gdx.gameservices.ServiceSuccessCallback;
import games.spooky.gdx.gameservices.authentication.AuthenticationHandler;

public class AuthenticationDemoTable extends GdxGameservicesDemoTable {

	private Pixmap avatar = null;
	
	public AuthenticationDemoTable(Skin skin) {
		super(skin);
	}
	
	public void initialize(final AuthenticationHandler handler) {

		final Image avatarThumbnail = new Image();
		avatarThumbnail.setScaling(Scaling.fit);

		final Label playerName = new Label("", getSkin());
		playerName.setAlignment(Align.right);
		final Label playerId = new Label("", getSkin());

		TextButton loginButton = new TextButton("Login", getSkin());
		loginButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final TextButton button = (TextButton) actor;
				log("Authenticating...");
				try {
					handler.login().onCompletion(new ServiceCompletionCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							log("Authenticated");

							handler.getPlayerId().onSuccess(new ServiceSuccessCallback<String>() {
								@Override
								public void onSuccess(String result) {
									playerId.setText(result);
								}
							});

							handler.getPlayerName().onSuccess(new ServiceSuccessCallback<String>() {
								@Override
								public void onSuccess(String result) {
									playerName.setText(result);
								}
							});

							handler.getPlayerAvatar().onSuccess(new ServiceSuccessCallback<byte[]>() {
								@Override
								public void onSuccess(final byte[] result) {
									Gdx.app.postRunnable(new Runnable() {
										public void run () {
											if (avatar != null) {
												avatar.dispose();
												avatar = null;
											}

											if (result == null) {
												avatarThumbnail.setDrawable(null);
											} else {
												avatar = new Pixmap(result, 0, result.length);
												avatarThumbnail.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(avatar))));
											}
										}
									});
								}
							});
						}

						@Override
						public void onError(ServiceError error) {
							button.setChecked(false);
							error(error.getErrorMessage());
						}
					});
				} catch (Exception e) {
					error(e.getLocalizedMessage());
				}
			}
		});

		row().colspan(2);
		add(avatarThumbnail);
		row();
		add(playerName).space(12);
		add(playerId);
		row().colspan(2);
		add(loginButton);
	}

}
