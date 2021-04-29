/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2021 Spooky Games
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

import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementState;

public class GooglePlayAchievementWrapper implements Achievement {

    private final com.google.android.gms.games.achievement.Achievement wrapped;

    GooglePlayAchievementWrapper(@NonNull com.google.android.gms.games.achievement.Achievement wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getAchievementId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public AchievementState getState() {
        int innerState = wrapped.getState();
        switch (innerState) {
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
