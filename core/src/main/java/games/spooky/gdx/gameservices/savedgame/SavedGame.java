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
package games.spooky.gdx.gameservices.savedgame;

/**
 * A SavedGame contains metadata for a given saved game. Actual save content,
 * the "raw and heavy" part, is not contained per se in this class and has to be
 * retrieved externally.
 */
public interface SavedGame {

	/**
	 * Unique id of this saved game.
	 * 
	 * @return the id of this saved game
	 */
	String getId();

	/**
	 * Title, or display name, of this saved game.
	 * 
	 * @return the title of this saved game
	 */
	String getTitle();

	/**
	 * Description of this saved game.
	 * 
	 * @return the description of this saved game
	 */
	String getDescription();

	/**
	 * Timestamp, or last modification date, of this saved game.
	 * 
	 * @return the timestamp of this saved game
	 */
	long getTimestamp();

	/**
	 * Amount of time played on this saved game.
	 * 
	 * @return the playtime of this saved game
	 */
	long getPlayedTime();

	/**
	 * Device this saved game was submitted from.
	 * 
	 * @return the device name of this saved game
	 */
	String getDeviceName();

}
