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

/**
 * Options to query leaderboard entries.
 */
public class LeaderboardOptions {

	public enum Window {
		/**
		 * Only fetch top leaderboard entries.
		 */
		Top,
		/**
		 * Only fetch leaderboard entries surrounding the player's entry.
		 */
		CenteredOnPlayer
	}

	public enum Scope {
		/**
		 * Fetch from all leaderboard entries.
		 */
		Public,
		/**
		 * Fetch from friends' leaderboard entries only.
		 */
		Friends
	}

	private final Window window;
	private final Scope scope;
	private final int maxResults;

	public LeaderboardOptions(Window window, Scope scope, int maxResults) {
		this.window = window;
		this.scope = scope;
		this.maxResults = maxResults;
	}

	public Window getWindow() {
		return window;
	}

	public Scope getScope() {
		return scope;
	}

	public int getMaxResults() {
		return maxResults;
	}
}
