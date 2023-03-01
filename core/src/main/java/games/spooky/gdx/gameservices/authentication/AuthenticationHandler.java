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
package games.spooky.gdx.gameservices.authentication;

import games.spooky.gdx.gameservices.AsyncServiceResult;

public interface AuthenticationHandler {

	/**
	 * Whether current player is currently logged in or not.
	 *
	 * @return an AsyncServiceResult indicating whether current player is currently logged in or not
	 */
	AsyncServiceResult<Boolean> isLoggedIn();

	/**
	 * Connect player to remote game service.
	 * 
	 * @return an AsyncServiceResult to handle successful/failed login
	 */
	AsyncServiceResult<Void> login();

	/**
	 * The unique id of currently connected player.
	 *
	 * @return an AsyncServiceResult to get the unique id of current player
	 */
	AsyncServiceResult<String> getPlayerId();

	/**
	 * The display name, or alias, of currently connected player.
	 *
	 * @return an AsyncServiceResult to get the display name of current player
	 */
	AsyncServiceResult<String> getPlayerName();

	/**
	 * Download bytes of the avatar of currently connected player (optional).
	 * PNG if possible. Using Pixmap is a nice way to integrate these bytes into your game.
	 *
	 * @return an AsyncServiceResult to handle bytes of player avatar
	 */
	AsyncServiceResult<byte[]> getPlayerAvatar();

}
