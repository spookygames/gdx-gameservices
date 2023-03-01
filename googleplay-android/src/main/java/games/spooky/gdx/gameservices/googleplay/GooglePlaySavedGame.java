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

import androidx.annotation.NonNull;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import games.spooky.gdx.gameservices.savedgame.SavedGame;

public class GooglePlaySavedGame implements SavedGame {

    private final String id;
    private final String title;
    private final String description;
    private final long timestamp;
    private final long playedTime;
    private final String deviceName;

    public GooglePlaySavedGame(@NonNull SnapshotMetadata snapshotMetadata) {
        super();
        this.id = snapshotMetadata.getSnapshotId();
        this.title = snapshotMetadata.getUniqueName();
        this.description = snapshotMetadata.getDescription();
        this.timestamp = snapshotMetadata.getLastModifiedTimestamp();
        this.playedTime = snapshotMetadata.getPlayedTime();
        this.deviceName = snapshotMetadata.getDeviceName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getPlayedTime() {
        return playedTime;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

}
