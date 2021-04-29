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
package games.spooky.gdx.gameservices;

public interface ServiceCallback<T> {

	/**
	 * Called when service request ended up successfully. A call to method
	 * {@link ServiceResponse#isSuccessful()} from the response should
	 * invariably return true.
	 * 
	 * @param result
	 *            the result from service call
	 * @param response
	 *            the response from remote service,
	 *            {@link ServiceResponse#isSuccessful()} should return true
	 */
	void onSuccess(T result, ServiceResponse response);

	/**
	 * Called when service request ended on error. A call to method
	 * {@link ServiceResponse#isSuccessful()} from the response should
	 * invariably return false.
	 * 
	 * @param response
	 *            the response from remote service,
	 *            {@link ServiceResponse#isSuccessful()} should return false
	 */
	void onFailure(ServiceResponse response);

}
