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
package games.spooky.gdx.gameservices.leaderboard;

import games.spooky.gdx.gameservices.AsyncServiceResult;
import games.spooky.gdx.gameservices.ServiceCompletionCallback;

public interface LeaderboardsHandler {

	/**
	 * Get leaderboard entry for specific leaderboard and current player.
	 * 
	 * @param leaderboardId
	 *            id of the leaderboard to get entry from
	 * @param options
	 *            leaderboard options, only the {@code scope} field is
	 *            relevant here
	 * @return an AsyncServiceResult to handle the leaderboard entry
	 */
	AsyncServiceResult<LeaderboardEntry> getPlayerScore(String leaderboardId, LeaderboardOptions options);

	/**
	 * Get leaderboard entries for leaderboard of given id.
	 * 
	 * @param leaderboardId
	 *            id of the leaderboard to get entries from
	 * @param options
	 *            leaderboard options, all fields relevant
	 * @return an AsyncServiceResult to handle the Iterable result
	 */
	AsyncServiceResult<Iterable<LeaderboardEntry>> getScores(String leaderboardId, LeaderboardOptions options);

	/**
	 * Submit a new entry to the leaderboard of given id.
	 * 
	 * @param leaderboardId
	 *            id of the leaderboard to submit an entry to
	 * @param score
	 *            score of the entry to submit
	 * @return an AsyncServiceResult to handle success/error
	 */
	AsyncServiceResult<Void> submitScore(String leaderboardId, long score);

}
