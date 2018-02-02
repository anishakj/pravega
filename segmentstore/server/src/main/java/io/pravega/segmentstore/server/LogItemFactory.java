/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.segmentstore.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * Factory for LogItems.
 */
public interface LogItemFactory<T extends LogItem> {
    /**
     * Deserializes a LogItem from the given InputStream.
     *
     * @param input The InputStream to deserialize from.
     * @return The deserialized LogItem.
     * @throws IOException If the LogItem could not be deserialized (SerializationException) or if an IOException occurred.
     */
    T deserialize(InputStream input) throws IOException;
}
