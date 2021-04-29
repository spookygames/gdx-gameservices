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
package games.spooky.gdx.gameservices.googleplay.desktop;

import com.google.api.services.games.model.PlayerAchievement;

import games.spooky.gdx.gameservices.achievement.Achievement;
import games.spooky.gdx.gameservices.achievement.AchievementState;

public class GooglePlayAchievementWrapper implements Achievement {

    private final PlayerAchievement wrapped;

    GooglePlayAchievementWrapper(PlayerAchievement wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getName() {
        return wrapped.getId();	// Too bad this needs to get the definition
    }

    @Override
    public AchievementState getState() {
        String innerState = wrapped.getAchievementState();
        if ("HIDDEN".equals(innerState)) {
            return AchievementState.Hidden;
        } else if ("REVEALED".equals(innerState)) {
            return AchievementState.Locked;
        } else if ("UNLOCKED".equals(innerState)) {
            return AchievementState.Unlocked;
        } else {
            return null;
        }
    }
}
