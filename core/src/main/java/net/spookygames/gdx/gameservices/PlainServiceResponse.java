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
package net.spookygames.gdx.gameservices;

import net.spookygames.gdx.gameservices.ServiceResponse;

public class PlainServiceResponse implements ServiceResponse {

	private boolean successful;
	private int errorCode;
	private String errorMessage;

	public PlainServiceResponse() {
		super();
	}

	public PlainServiceResponse(boolean successful, int errorCode, String errorMessage) {
		super();
		this.successful = successful;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	@Override
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public static ServiceResponse success() {
		return new PlainServiceResponse(true, 0, null);
	}
	
	public static ServiceResponse error(int errorCode) {
		return error(errorCode, null);
	}
	
	public static ServiceResponse error(String errorMessage) {
		return error(-1, errorMessage);
	}
	
	public static ServiceResponse error(int errorCode, String errorMessage) {
		return new PlainServiceResponse(false, errorCode, errorMessage);
	}
}
