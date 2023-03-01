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
package games.spooky.gdx.gameservices;

import com.badlogic.gdx.utils.Array;

public abstract class CallbackAsyncServiceResult<TCallback, TResult> implements AsyncServiceResult<TResult> {

    public interface Callback<T> {
        void onSuccess(T value);
        void onError(Throwable error);
    }

    private final Array<ServiceSuccessCallback<TResult>> successCallbacks = new Array<>();
    private final Array<ServiceErrorCallback> errorCallbacks = new Array<>();

    private boolean completed = false;

    private TCallback result;
    private Throwable error;

    public CallbackAsyncServiceResult() {
        callAsync(new Callback<TCallback>() {
            @Override
            public void onSuccess(TCallback result) {
                synchronized (CallbackAsyncServiceResult.this) {
                    completed = true;
                    CallbackAsyncServiceResult.this.result = result;
                    checkExistingResponse();
                }
            }

            @Override
            public void onError(Throwable error) {
                synchronized (CallbackAsyncServiceResult.this) {
                    completed = true;
                    CallbackAsyncServiceResult.this.error = error;
                    checkExistingResponse();
                }
            }
        });
    }

    @Override
    public void onSuccess(ServiceSuccessCallback<TResult> callback) {
        synchronized (this) {
            successCallbacks.add(callback);
            checkExistingResponse();
        }
    }

    @Override
    public void onError(ServiceErrorCallback callback) {
        synchronized (this) {
            errorCallbacks.add(callback);
            checkExistingResponse();
        }
    }

    @Override
    public void onCompletion(ServiceCompletionCallback<TResult> callback) {
        synchronized (this) {
            successCallbacks.add(callback);
            errorCallbacks.add(callback);
            checkExistingResponse();
        }
    }

    void checkExistingResponse() {
        if (completed) {
            if (error != null) {
                ServiceError serviceError = new ExceptionServiceError(error);
                for (ServiceErrorCallback callback : errorCallbacks)
                    callback.onError(serviceError);
            } else {
                try {
                    TResult transformed = transformResult(result);
                    for (ServiceSuccessCallback<TResult> callback : successCallbacks)
                        callback.onSuccess(transformed);
                } catch (Throwable e) {
                    ServiceError serviceError = new ExceptionServiceError(e);
                    for (ServiceErrorCallback callback : errorCallbacks)
                        callback.onError(serviceError);
                }
            }
        }
    }

    protected abstract void callAsync(Callback<TCallback> callback);

    protected abstract TResult transformResult(TCallback result);
}
