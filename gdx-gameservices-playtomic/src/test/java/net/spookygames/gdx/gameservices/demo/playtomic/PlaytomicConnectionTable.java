/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Spooky Games
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
package net.spookygames.gdx.gameservices.demo.playtomic;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import net.spookygames.gdx.gameservices.ConnectionHandler;
import net.spookygames.gdx.gameservices.demo.ConnectionDemoTable;
import net.spookygames.gdx.gameservices.playtomic.PlaytomicServicesHandler;

public class PlaytomicConnectionTable extends ConnectionDemoTable {

	public PlaytomicConnectionTable(Skin skin) {
		super(skin);
	}
	
	@Override
	public void initialize(ConnectionHandler handler, final Preferences prefs) {
		
		final PlaytomicServicesHandler playtomic = (PlaytomicServicesHandler) handler;
		Skin skin = getSkin();
		
		// Server URL
		final TextField serverTextField = new TextField(prefs.getString("playtomic_server", ""), skin);
		serverTextField.setMessageText("Playtomic server URL");
		
		// Public key
		final TextField publicKeyTextField = new TextField(prefs.getString("playtomic_publickey", ""), skin);
		publicKeyTextField.setMessageText("Public key");
		
		// Server URL
		final TextField privateKeyTextField = new TextField(prefs.getString("playtomic_privatekey", ""), skin);
		privateKeyTextField.setMessageText("Private key");
		
		// Player id
		final TextField playerIdTextField = new TextField(prefs.getString("playtomic_playerid", ""), skin);
		playerIdTextField.setMessageText("Player ID");
		
		// Player name
		final TextField playerNameTextField = new TextField(prefs.getString("playtomic_playername", ""), skin);
		playerNameTextField.setMessageText("Player name");
		
		// Player source
		final TextField playerSourceTextField = new TextField(prefs.getString("playtomic_playersource", ""), skin);
		playerSourceTextField.setMessageText("Player source (optional)");
		
		row();
		add(serverTextField);
		
		row();
		add(publicKeyTextField);
		
		row();
		add(privateKeyTextField);
		
		row();
		add(playerIdTextField);
		
		row();
		add(playerNameTextField);
		
		row();
		add(playerSourceTextField);
		
		super.initialize(handler, prefs);
		
		getChildren().peek().getListeners().insert(0, new ChangeListener() {
			@Override public void changed(ChangeEvent event, Actor actor) {
				String text = serverTextField.getText();
				playtomic.setServer(text);
				prefs.putString("playtomic_server", text);

				text = publicKeyTextField.getText();
				playtomic.setPublicKey(text);
				prefs.putString("playtomic_publickey", text);

				text = privateKeyTextField.getText();
				playtomic.setPrivateKey(text);
				prefs.putString("playtomic_privatekey", text);

				text = playerIdTextField.getText();
				playtomic.setPlayerId(text);
				prefs.putString("playtomic_playerid", text);

				text = playerNameTextField.getText();
				playtomic.setPlayerName(text);
				prefs.putString("playtomic_playername", text);

				text = playerSourceTextField.getText();
				playtomic.setPlayerSource(text);
				prefs.putString("playtomic_playersource", text);
				
				prefs.flush();
			}
		});
	}

}
