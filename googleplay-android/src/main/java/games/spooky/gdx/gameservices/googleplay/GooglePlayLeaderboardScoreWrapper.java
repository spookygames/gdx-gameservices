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
package games.spooky.gdx.gameservices.googleplay;

import android.support.annotation.NonNull;

import com.google.android.gms.games.leaderboard.LeaderboardScore;

import games.spooky.gdx.gameservices.leaderboard.LeaderboardEntry;

import java.util.Date;

public class GooglePlayLeaderboardScoreWrapper implements LeaderboardEntry {

    private final LeaderboardScore wrapped;

    GooglePlayLeaderboardScoreWrapper(@NonNull LeaderboardScore wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public String getPlayerId() {
        return wrapped.getScoreHolder().getPlayerId();
    }

    @Override
    public String getPlayerName() {
        return wrapped.getScoreHolderDisplayName();
    }

    @Override
    public long getScore() {
        return wrapped.getRawScore();
    }

    @Override
    public long getRank() {
        return wrapped.getRank();
    }

    @Override
    public String getSource() {
        return "Google Play Games";
    }

    @Override
    public Date getDate() {
        return new Date(wrapped.getTimestampMillis());
    }
}
