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

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

public class JsonObjectBuilder {

	private JsonValue object;

	/**
	 * Initializes the builder and sets it up to build a new {@link JsonValue} .
	 */
	public JsonObjectBuilder newObject() {
		if (object != null) {
			throw new IllegalStateException(
					"A new creation has already been started. Call JsonObjectBuilder.build() first.");
		}

		object = new JsonValue(ValueType.object);
		return this;
	}

	/**
	 * Initializes the builder and sets it up to build a new {@link JsonValue} .
	 */
	public JsonObjectBuilder newArray() {
		if (object != null) {
			throw new IllegalStateException(
					"A new creation has already been started. Call JsonObjectBuilder.build() first.");
		}

		object = new JsonValue(ValueType.array);
		return this;
	}

	public JsonObjectBuilder add(String name, String value) {
		validate();
		object.addChild(name, new JsonValue(value));
		return this;
	}

	public JsonObjectBuilder add(String name, double value) {
		validate();
		object.addChild(name, new JsonValue(value));
		return this;
	}

	public JsonObjectBuilder add(String name, long value) {
		validate();
		object.addChild(name, new JsonValue(value));
		return this;
	}

	public JsonObjectBuilder add(String name, boolean value) {
		validate();
		object.addChild(name, new JsonValue(value));
		return this;
	}

	/**
	 * Returns the {@link JsonValue} that has been setup by this builder so far.
	 */
	public JsonValue build() {
		validate();
		JsonValue value = object;
		object = null;
		return value;
	}

	private void validate() {
		if (object == null) {
			throw new IllegalStateException(
					"A new creation has not been started yet. Call JsonObjectBuilder.newObject() or JsonObjectBuilder.newArray() first.");
		}
	}

}
