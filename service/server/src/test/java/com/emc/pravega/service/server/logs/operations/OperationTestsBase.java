/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emc.pravega.service.server.logs.operations;

import org.junit.Test;

import com.emc.pravega.testcommon.AssertExtensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletionException;

/**
 * Base class for all Log Operation test.
 */
public abstract class OperationTestsBase<T extends Operation> {
    protected static final int MAX_CONFIG_ITERATIONS = 10;
    private static final OperationFactory OPERATION_FACTORY = new OperationFactory();

    @Test
    public void testSerialization() throws Exception {
        Random random = new Random();
        T baseOp = createOperation(random);

        // Verify we cannot serialize without a valid Sequence Number.
        trySerialize(baseOp, "Serialization was possible without a valid Sequence Number.");
        baseOp.setSequenceNumber(Math.abs(random.nextLong()));

        // Verify that whatever Pre-Serialization requirements are needed will actually prevent serialization.
        int configIter = 0;
        while (configIter < MAX_CONFIG_ITERATIONS && isPreSerializationConfigRequired(baseOp)) {
            configIter++;
            trySerialize(baseOp, "Serialization was possible without completing all necessary pre-serialization steps.");
            configurePreSerialization(baseOp, random);
        }

        // Serialize.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        baseOp.serialize(outputStream);

        //Deserialize.
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Operation newOp = OPERATION_FACTORY.deserialize(inputStream);

        // Verify operations are the same.
        OperationHelpers.assertEquals(baseOp, newOp);
    }

    /**
     * Creates a new operation of a given type.
     *
     * @return
     */
    protected abstract T createOperation(Random random);

    /**
     * Gets a value indicating whether we need to do anything special (i.e., assign offsets) before serializing.
     *
     * @param operation
     * @return
     */
    protected boolean isPreSerializationConfigRequired(T operation) {
        return false;
    }

    /**
     * Performs any necessary pre-serialization configuration (one step at a time - as long as isPreSerializationConfigRequired returns true).
     *
     * @param operation
     * @param random
     */
    protected void configurePreSerialization(T operation, Random random) {
        // Base method intentionally left blank.
    }

    protected String getStreamSegmentName(long id) {
        return "StreamSegment_" + id;
    }

    private void trySerialize(T op, String message) {
        AssertExtensions.assertThrows(message,
                () -> {
                    try {
                        op.serialize(new ByteArrayOutputStream());
                    } catch (IOException ex) {
                        throw new CompletionException(ex);
                    }
                },
                ex -> ex instanceof IllegalStateException);
    }
}
