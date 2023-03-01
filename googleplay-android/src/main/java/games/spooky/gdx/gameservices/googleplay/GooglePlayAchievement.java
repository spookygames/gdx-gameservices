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
import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementState;

@SuppressLint("VisibleForTests")
public class GooglePlayAchievement implements Achievement {

    private final String id;
    private final String name;
    private final AchievementState state;

    GooglePlayAchievement(@NonNull com.google.android.gms.games.achievement.Achievement achievement) {
        super();
        this.id = achievement.getAchievementId();
        this.name = achievement.getName();
        this.state = readGooglePlayAchievementState(achievement.getState());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AchievementState getState() {
        return state;
    }

    private static AchievementState readGooglePlayAchievementState(int state) {
        switch (state) {
            case com.google.android.gms.games.achievement.Achievement.STATE_UNLOCKED:
                return AchievementState.Unlocked;
            case com.google.android.gms.games.achievement.Achievement.STATE_REVEALED:
                return AchievementState.Locked;
            case com.google.android.gms.games.achievement.Achievement.STATE_HIDDEN:
                return AchievementState.Hidden;
        }
        return null;
    }
}
