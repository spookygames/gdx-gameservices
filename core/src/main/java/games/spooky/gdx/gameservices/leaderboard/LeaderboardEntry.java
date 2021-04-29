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
package games.spooky.gdx.gameservices.leaderboard;

import java.util.Date;

public interface LeaderboardEntry {

	/**
	 * Unique id of the player of this entry.
	 * 
	 * @return the unique id of the player
	 */
	String getPlayerId();

	/**
	 * Display name, or "alias", of the player of this entry.
	 * 
	 * @return the display name of the player
	 */
	String getPlayerName();

	/**
	 * Score of this entry.
	 * 
	 * @return the score of this entry
	 */
	long getScore();

	/**
	 * Rank of this entry in the leaderboard.
	 * 
	 * @return the rank of this entry
	 */
	long getRank();

	/**
	 * Source of this entry. Can be a way to distinguish between several
	 * different platforms competing on the same leaderboard.
	 * 
	 * @return the source of this entry
	 */
	String getSource();

	/**
	 * Date this entry was submitted.
	 * 
	 * @return the date this entry was submitted
	 */
	Date getDate();
}