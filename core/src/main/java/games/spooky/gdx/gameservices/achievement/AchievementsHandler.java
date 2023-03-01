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
package games.spooky.gdx.gameservices.achievement;

import games.spooky.gdx.gameservices.AsyncServiceResult;

public interface AchievementsHandler {

	/**
	 * Get achievements from game service. Be aware that some game services will
	 * only return achievements actually unlocked by the player while some
	 * others will return all available achievements. It is YOUR responsibility
	 * to check achievement status and filter out if need be.
	 * 
	 * @return an AsyncServiceResult to handle the Iterable result
	 */
	AsyncServiceResult<Iterable<Achievement>> getAchievements();

	/**
	 * Unlock achievement of given id from game service. Successful unlocking is
	 * identified by a call to method onSuccess() on given callback.
	 * 
	 * @param achievementId
	 *            id of the achievement to unlock
	 * @return an AsyncServiceResult to handle success/error
	 */
	AsyncServiceResult<Void> unlockAchievement(String achievementId);

}
