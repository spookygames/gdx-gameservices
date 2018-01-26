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
package net.spookygames.gdx.gameservices.playtomic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestHeader;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import net.spookygames.gdx.gameservices.ServiceCallback;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SerializationException;

public class PlaytomicNet {

	private String serverUrl;
	private String publicKey;
	private String privateKey;

	private final JsonReader reader;
	private final Json converter;

	public PlaytomicNet() {
		super();
		converter = new Json();
		converter.setIgnoreUnknownFields(true);
		reader = new JsonReader();
	}

	public String getBaseUrl() {
		return serverUrl;
	}

	public void setBaseUrl(String url) {
		if (!url.endsWith("/"))
			url = c(url, "/");

		serverUrl = url;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public <T> void send(String section, String action, JsonValue payload, final Class<T> type, final ServiceCallback<T> callback) {
		final HttpRequest req = Pools.obtain(HttpRequest.class);
		req.setMethod(HttpMethods.POST);
		req.setUrl(c(serverUrl, "v1?publickey=", publicKey));

		if (payload == null)
			payload = new JsonValue(ValueType.object);

		payload.addChild("publickey", new JsonValue(publicKey));
		payload.addChild("section", new JsonValue(section));
		payload.addChild("action", new JsonValue(action));

		String jsonstring = payload.toJson(OutputType.json);
		debug(jsonstring);
		String b64 = Base64Coder.encodeString(jsonstring);
		String hash = md5(c(jsonstring, privateKey));

		payload.remove("publickey");
		payload.remove("section");
		payload.remove("action");
		
		JsonValue json = new JsonValue(ValueType.object);
		json.addChild("data", new JsonValue(b64));
		json.addChild("hash", new JsonValue(hash));

		req.setHeader(HttpRequestHeader.ContentType, "application/json");
		req.setHeader(HttpRequestHeader.Accept, "application/json");
		req.setTimeOut(10000);

		String content = json.toJson(OutputType.json);
		req.setContent(content);

		Gdx.net.sendHttpRequest(req, new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				if (callback == null)
					return;	// Don't bother
				
				// Build response
				final PlaytomicResponse response = new PlaytomicResponse();

				JsonValue json = reader.parse(httpResponse.getResultAsStream());
				debug(json.toString());

				HttpStatus status = httpResponse.getStatus();
				int httpCode = status.getStatusCode();
				if (httpCode >= 200 && httpCode < 300) {
					// Http success!
					boolean success = false;
					int code = 1;
					T result = null;

					for (JsonValue child : json) {
						String name = child.name;
						if ("success".equalsIgnoreCase(name)) {
							success = child.asBoolean();
						} else if ("errorcode".equalsIgnoreCase(name)) {
							code = child.asInt();
						} else if (result == null && type != Void.class) {
							try {
								result = converter.readValue(type, child);
							} catch (SerializationException silenced) {
								// I guess that was not our field!
//								error(silenced);
							}
						}
					}
					response.setSuccess(success);
					response.setErrorCode(code);
					if (success)
						callback.onSuccess(result, response);
					else
						callback.onFailure(response);
				} else {
					// Http error
					response.setSuccess(false);
					response.setErrorCode(1);
					callback.onFailure(response);
				}

				// Free request
				Pools.free(req);
			}

			@Override
			public void failed(Throwable t) {
				
				error(t);
				
				if (callback != null) {
					// Build error response
					PlaytomicResponse response = new PlaytomicResponse(false, 1);
					callback.onFailure(response);
				}

				// Free request
				Pools.free(req);
			}

			@Override
			public void cancelled() {
				// Don't do a thing here

				// Free request
				Pools.free(req);
			}
		});
	}

	protected void debug(String text) {
		// Override me
	}

	protected void error(Throwable error) {
		// Override me
	}

	private static String md5(String value) {

		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		algorithm.reset();
		algorithm.update(value.getBytes());
		byte messageDigest[] = algorithm.digest();

		String hexString = "";

		for (int i = 0; i < messageDigest.length; i++)
			hexString = c(hexString, Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));

		return hexString;
	}
	
	// String utils
	
	private static final StringBuilder builder = new StringBuilder();

	private static String c(Object s1, Object s2) {
		builder.setLength(0);
		builder.append(s1);
		builder.append(s2);
		return builder.toString();
	}

	private static String c(Object s1, Object s2, Object s3) {
		builder.setLength(0);
		builder.append(s1);
		builder.append(s2);
		builder.append(s3);
		return builder.toString();
	}

}