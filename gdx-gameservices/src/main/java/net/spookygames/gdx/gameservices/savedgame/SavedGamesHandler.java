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
package net.spookygames.gdx.gameservices.savedgame;

import net.spookygames.gdx.gameservices.ServiceCallback;

public interface SavedGamesHandler {

	/**
	 * Get saved games from game service for current player. SavedGames contain
	 * metadata only. In order to get save content, call
	 * {@link #loadSavedGameData(SavedGame, ServiceCallback)}.
	 * 
	 * @param callback
	 *            a ServiceCallback to handle the Iterable result
	 */
	void getSavedGames(ServiceCallback<Iterable<SavedGame>> callback);

	/**
	 * Get saved game data from game service for given saved game. A SavedGame
	 * contains metadata only and calling this method is necessary to retrieve
	 * actual game data from the service. Depending on the size of the data
	 * stored this call may take quite some time!
	 * 
	 * @param metadata
	 *            the metadata of the saved game to retrieve content from
	 * @param callback
	 *            a ServiceCallback to handle the byte[] result
	 */
	void loadSavedGameData(SavedGame metadata, ServiceCallback<byte[]> callback);

	/**
	 * Submit a game save to game service. Both metadata and raw data are sent
	 * with this call. Any previous version will be erased or at least
	 * unreachable from this library, depending on underlying game service.
	 * 
	 * @param savedGame
	 *            the metadata of the saved game to submit
	 * @param data
	 *            the content of the saved game to submit
	 * @param callback
	 *            a ServiceCallback to handle success/failure
	 */
	void submitSavedGame(SavedGame savedGame, byte[] data, ServiceCallback<Void> callback);

	/**
	 * Delete a game save from game service. Only metadata needed to retrieve
	 * the game but both metadata and raw content are to be deleted.
	 * 
	 * @param savedGame
	 *            the metadata of the saved game to delete
	 * @param callback
	 *            a ServiceCallback to handle success/failure
	 */
	void deleteSavedGame(SavedGame savedGame, ServiceCallback<Void> callback);

}
