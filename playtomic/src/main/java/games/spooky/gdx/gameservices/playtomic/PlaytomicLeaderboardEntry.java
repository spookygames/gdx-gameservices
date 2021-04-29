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
package games.spooky.gdx.gameservices.playtomic;

import java.util.Date;

import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;

public class PlaytomicLeaderboardEntry implements LeaderboardEntry {

	private String table;
	private String playername;
	private String playerid;
	private String source;
	private String publickey;
	private long points;
	private String fields;
	private long lastupdated;
	private long date;
	private long rank;
	private String scoreid;
	private String rdate;
	private boolean submitted;

	public PlaytomicLeaderboardEntry() {
	}

	@Override
	public String getPlayerId() {
		return playerid;
	}

	@Override
	public String getPlayerName() {
		return playername;
	}

	@Override
	public long getScore() {
		return points;
	}

	@Override
	public long getRank() {
		return rank;
	}

	public String getTable() {
		return table;
	}

	public String getPlayerid() {
		return playerid;
	}

	@Override
	public String getSource() {
		return source;
	}

	public String getPublickey() {
		return publickey;
	}

	public String getFields() {
		return fields;
	}

	public long getLastupdated() {
		return lastupdated;
	}

	public long getLongDate() {
		return date;
	}

	@Override
	public Date getDate() {
		return new Date(date * 1000);
}

	public String getScoreid() {
		return scoreid;
	}

	public String getRdate() {
		return rdate;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	@Override
	public String toString() {
		return "PlaytomicLeaderboardEntry [rank=" + getRank() + ", playerId=" + getPlayerId() + ", playerName="
				+ getPlayerName() + ", score=" + getScore() + ", date=" + getDate() + ", source=" + getSource() + "]";
	}
}