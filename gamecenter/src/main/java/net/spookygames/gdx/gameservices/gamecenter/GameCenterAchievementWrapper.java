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
package net.spookygames.gdx.gameservices.gamecenter;

import org.robovm.apple.gamekit.GKAchievement;

import net.spookygames.gdx.gameservices.achievement.Achievement;
import net.spookygames.gdx.gameservices.achievement.AchievementState;

public class GameCenterAchievementWrapper implements Achievement {

	private final GKAchievement wrapped;

	public GameCenterAchievementWrapper(GKAchievement wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public String getId() {
		return wrapped.getIdentifier();
	}

	@Override
	public String getName() {
		return wrapped.description();
	}

	@Override
	public AchievementState getState() {
		// No proper state reading here, AchievementState could and should be
		// properly retrievable from GKAchievement
		return wrapped.isCompleted() ? AchievementState.Unlocked : AchievementState.Locked;
	}
}
