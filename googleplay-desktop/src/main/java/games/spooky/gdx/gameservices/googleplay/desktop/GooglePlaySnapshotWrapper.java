/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2021 Spooky Games
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

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import games.spooky.gdx.gameservices.savedgame.SavedGame;

public class GooglePlaySnapshotWrapper implements SavedGame {

    private final File wrapped;

    public GooglePlaySnapshotWrapper(File wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getTitle() {
        return wrapped.getName();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public long getTimestamp() {
    	DateTime time = wrapped.getModifiedTime();
    	return time == null ? -1 : time.getValue();
    }

    @Override
    public long getPlayedTime() {
    	// Last modified time - created time
    	// Of course this is incorrect
    	DateTime createdTime = wrapped.getCreatedTime();
        return getTimestamp() - (createdTime == null ? -1 : createdTime.getValue());
    }

    @Override
    public String getDeviceName() {
        return "";
    }

    File getWrapped() {
        return wrapped;
    }
}
