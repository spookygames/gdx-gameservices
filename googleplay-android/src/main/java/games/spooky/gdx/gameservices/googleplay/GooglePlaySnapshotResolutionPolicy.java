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

import static com.google.android.gms.games.SnapshotsClient.*;

public enum GooglePlaySnapshotResolutionPolicy {
    /**
     * In the case of a conflict, the snapshot with the longest played time will be used.
     * In the case of a tie, the last known good snapshot will be chosen instead.
     */
    LONGEST_PLAYTIME(RESOLUTION_POLICY_LONGEST_PLAYTIME),
    /**
     * In the case of a conflict, the last known good version of the snapshot will be used.
     */
    LAST_KNOWN_GOOD(RESOLUTION_POLICY_LAST_KNOWN_GOOD),
    /**
     * In the case of a conflict, the most recently modified version of the snapshot will be used.
     */
    MOST_RECENTLY_MODIFIED(RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED),
    /**
     * In the case of a conflict, the snapshot with the highest progress value will be used.
     * In the case of a tie, the last known good snapshot will be chosen instead.
     */
    HIGHEST_PROGRESS(RESOLUTION_POLICY_HIGHEST_PROGRESS),
    ;

    public final int rawValue;

    GooglePlaySnapshotResolutionPolicy(int rawValue) {
        this.rawValue = rawValue;
    }

    static GooglePlaySnapshotResolutionPolicy fromRawValue(int rawValue) {
        for (GooglePlaySnapshotResolutionPolicy policy : values()) {
            if (policy.rawValue == rawValue)
                return policy;
        }
        return null;
    }
}
