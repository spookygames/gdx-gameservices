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
package games.spooky.gdx.gameservices.gamecenter;

import org.robovm.apple.gamekit.GKSavedGame;

import games.spooky.gdx.gameservices.savedgame.SavedGame;

public class GameCenterSavedGameWrapper implements SavedGame {

	private final GKSavedGame wrapped;

	public GameCenterSavedGameWrapper(GKSavedGame wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public String getId() {
		return wrapped.getName();
	}

	@Override
	public String getTitle() {
		return wrapped.getName();
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public long getTimestamp() {
		return wrapped.getModificationDate().toDate().getTime();
	}

	@Override
	public long getPlayedTime() {
		return 0;
	}

	@Override
	public String getDeviceName() {
		return wrapped.getDeviceName();
	}
	
	GKSavedGame getWrapped() {
		return wrapped;
	}
}
