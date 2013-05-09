/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mongodb.impl;

import org.mongodb.MongoClientOptions;
import org.mongodb.MongoConnectionStrategy;
import org.mongodb.MongoCredential;
import org.mongodb.MongoServerBinding;
import org.mongodb.ServerAddress;
import org.mongodb.io.BufferPool;
import org.mongodb.io.PowerOfTwoByteBufferPool;
import org.mongodb.pool.SimplePool;

import java.nio.ByteBuffer;
import java.util.List;

public final class MongoServerBindings {
    private MongoServerBindings() {
    }

    public static MongoServerBinding create(final ServerAddress serverAddress, final MongoClientOptions options) {
        return create(serverAddress, null, options);
    }

    public static MongoServerBinding create(final ServerAddress serverAddress, final List<MongoCredential> credentialList,
                                                  final MongoClientOptions options) {
        final PowerOfTwoByteBufferPool bufferPool = new PowerOfTwoByteBufferPool();
        return new MongoSingleServerBinding(create(serverAddress, credentialList, options, bufferPool), bufferPool);
    }

    public static MongoServerBinding create(final List<ServerAddress> seedList, final MongoClientOptions options) {
        return create(new ReplicaSetConnectionStrategy(seedList, options), null, options);
    }

    public static MongoServerBinding create(final List<ServerAddress> seedList, final List<MongoCredential> credentialList,
                                                  final MongoClientOptions options) {
        return create(new ReplicaSetConnectionStrategy(seedList, options), credentialList, options);
    }

    public static MongoServerBinding create(final MongoConnectionStrategy connectionStrategy, final List<MongoCredential> credentialList,
                                            final MongoClientOptions options) {
        return new MongoMultiServerBinding(connectionStrategy, credentialList, options, new PowerOfTwoByteBufferPool());
    }

    static MongoConnectionManagerImpl create(final ServerAddress serverAddress, final List<MongoCredential> credentialList,
                                             final MongoClientOptions options, final BufferPool<ByteBuffer> bufferPool) {

        SimplePool<MongoSyncConnection> connectionPool = new SimplePool<MongoSyncConnection>(serverAddress.toString(),
                options.getConnectionsPerHost()) {
            @Override
            protected MongoSyncConnection createNew() {
                return new DefaultMongoSyncConnection(serverAddress, credentialList, this, bufferPool, options);
            }

            @Override
            public void close() {
                super.close();
                bufferPool.close();
            }
        };
        SimplePool<MongoAsyncConnection> asyncConnectionPool = null;

        if (options.isAsyncEnabled() && !options.isSSLEnabled() && !System.getProperty("org.mongodb.useSocket", "false").equals("true")) {
            asyncConnectionPool = new SimplePool<MongoAsyncConnection>(serverAddress.toString(), options.getConnectionsPerHost()) {
                @Override
                protected MongoAsyncConnection createNew() {
                    return new DefaultMongoAsyncConnection(serverAddress, credentialList, this, bufferPool);
                }
            };
        }
        return new MongoConnectionManagerImpl(serverAddress, connectionPool, asyncConnectionPool);
    }
}
