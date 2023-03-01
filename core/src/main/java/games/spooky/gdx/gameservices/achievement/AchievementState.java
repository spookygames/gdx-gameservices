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

/**
 * The state of a given Achievement, coming in three possible values.
 * <ul>
 * <li>{@code Unlocked}: Achievement was unlocked by player.
 * <li>{@code Locked}: Achievement is visible but was not unlocked by player.
 * <li>{@code Hidden}: Achievement was not unlocked by player and is not
 * visible.
 * </ul>
 */
public enum AchievementState {

	/**
	 * Achievement was unlocked by the player.
	 */
	Unlocked,
	/**
	 * Achievement is visible but was not unlocked by the player.
	 */
	Locked,
	/**
	 * Achievement was not unlocked by the player and is not visible.
	 */
	Hidden

}
