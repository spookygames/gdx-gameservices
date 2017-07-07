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
package net.spookygames.gdx.gameservices.googleplay;

import java.util.Date;

import org.robovm.apple.gamekit.GKScore;

import net.spookygames.gdx.gameservices.leaderboard.LeaderboardEntry;

public class GameCenterLeaderboardEntryWrapper implements LeaderboardEntry {

	private final GKScore wrapped;

	GameCenterLeaderboardEntryWrapper(GKScore wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public String getPlayerId() {
		return wrapped.getPlayer().getPlayerID();
	}

	@Override
	public String getPlayerName() {
		return wrapped.getPlayer().getDisplayName();	// Could be wrapped.getPlayer().getAlias()
	}

	@Override
	public long getScore() {
		return wrapped.getValue();
	}

	@Override
	public long getRank() {
		return wrapped.getRank();
	}

	@Override
	public String getSource() {
		return "Game Center";
	}

	@Override
	public Date getDate() {
		return wrapped.getDate().toDate();
	}
}
