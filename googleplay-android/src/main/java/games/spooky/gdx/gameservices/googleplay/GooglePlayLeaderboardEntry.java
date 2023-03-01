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
package games.spooky.gdx.gameservices.googleplay;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;

import java.util.Date;

@SuppressLint("VisibleForTests")
public class GooglePlayLeaderboardEntry implements LeaderboardEntry {

    private final String playerId;
    private final String playerName;
    private final long score;
    private final long rank;
    private final long timestamp;

    GooglePlayLeaderboardEntry(@NonNull LeaderboardScore leaderboardScore) {
        super();
        Player player = leaderboardScore.getScoreHolder();
        this.playerId = player == null ? null : player.getPlayerId();
        this.playerName = leaderboardScore.getScoreHolderDisplayName();
        this.score = leaderboardScore.getRawScore();
        this.rank = leaderboardScore.getRank();
        this.timestamp = leaderboardScore.getTimestampMillis();
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public long getScore() {
        return score;
    }

    @Override
    public long getRank() {
        return rank;
    }

    @Override
    public String getSource() {
        return "Google Play Games";
    }

    @Override
    public Date getDate() {
        return new Date(timestamp);
    }
}
