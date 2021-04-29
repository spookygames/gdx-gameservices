/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2018 Spooky Games
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

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import games.spooky.gdx.gameservices.ConnectionHandler;
import games.spooky.gdx.gameservices.ServiceCallback;
import games.spooky.gdx.gameservices.ServiceResponse;

public class ConnectionDemoTable extends GdxGameservicesDemoTable {
	
	public ConnectionDemoTable(Skin skin) {
		super(skin);
	}
	
	public void initialize(final ConnectionHandler handler, Preferences prefs) {
		TextButton loginButton = new TextButton("Login", getSkin(), "toggle");
		loginButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final TextButton button = (TextButton) actor;
				if (button.isChecked()) {
					log("Connecting...");
					try {
						handler.login(new ServiceCallback<Void>() {
							@Override
							public void onSuccess(Void result, ServiceResponse response) {
								button.setText("Logout");
								log(response.getErrorMessage());
							}

							@Override
							public void onFailure(ServiceResponse response) {
								button.setChecked(false);
								error(response.getErrorMessage());
							}
						});
					} catch (Exception e) {
						error(e.getLocalizedMessage());
					}
				} else {
					try {
						handler.logout();
						button.setText("Login");
						log("Disconnected");
					} catch (Exception e) {
						error(e.getLocalizedMessage());
					}
				}
			}
		});
		
		row();
		add(loginButton);
	}
	
}
