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
package games.spooky.gdx.gameservices.playtomic;

import games.spooky.gdx.gameservices.ServiceResponse;

public class PlaytomicResponse implements ServiceResponse {

	private boolean success;
	private int errorcode;
	private String exceptionmessage;

	public PlaytomicResponse() {
	}

	public PlaytomicResponse(boolean success, int error) {
		this();
		this.success = success;
		this.errorcode = error;
	}

	@Override
	public boolean isSuccessful() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public int getErrorCode() {
		return errorcode;
	}

	public void setErrorCode(int error) {
		this.errorcode = error;
	}

	@Override
	public String getErrorMessage() {
		switch (errorcode) {
		case 0:
			return "Nothing went wrong!";
		case 1:
			return "General error, this typically means the player is unable to connect to the server";
		case 2:
			return "Invalid game credentials. Make sure you use the right public and private keys";
		case 3:
			return "Request timed out";
		case 4:
			return "Invalid request";

		case 100:
			return "GeoIP API has been disabled for this game";

		case 200:
			return "Leaderboard API has been disabled for this game";
		case 201:
			return "The player's name wasn't provided";
		case 203:
			return "Player is banned from submitting scores in this game";
		case 204:
			return "Score was not saved because it was not the player's best, you can allow players to have "
					+ "more than one score by specifying allowduplicates=true in your save options";

		case 300:
			return "GameVars API has been disabled for this game";

		case 400:
			return "Level sharing API has been disabled for this game";
		case 401:
			return "Invalid rating (must be 1 - 10)";
		case 402:
			return "Player has already rated that level";
		case 403:
			return "Missing level name";
		case 404:
			return "Missing level id";
		case 405:
			return "Level already exists";

		case 500:
			return "Achievements API has been disabled for this game";
		case 501:
			return "Missing playerid";
		case 502:
			return "Missing player name";
		case 503:
			return "Missing achievementid";
		case 504:
			return "Invalid achievementid or achievement key";
		case 505:
			return "Player already had the achievement, you can overwrite old achievements with overwrite=true or "
					+ "save each time the player is awarded with allowduplicates=true";
		case 506:
			return "Player already had the achievement and it was overwritten or a duplicate was saved successfully";

		case 600:
			return "Newsletter API has been disabled for this game";
		case 601:
			return "MailChimp API Key has not been configured";
		case 602:
			return "MailChimp API returned an error";
		}

		return "Unknown error...";
	}
	
	public String getDeveloperMessage() {
		return exceptionmessage;
	}
}
