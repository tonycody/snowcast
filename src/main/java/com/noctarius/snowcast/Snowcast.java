/*
 * Copyright (c) 2015-2017, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.snowcast;

import com.hazelcast.core.HazelcastInstance;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * <p>The Snowcast instance is a {@link com.noctarius.snowcast.SnowcastSequencer} factory bound
 * to {@link com.hazelcast.core.HazelcastInstance} instance. A Snowcast instance is built using
 * the {@link com.noctarius.snowcast.SnowcastSystem} entry point.</p>
 * <pre>
 *     HazelcastInstance hazelcastInstance = getHazelcastInstance();
 *     Snowcast snowcast = SnowcastSystem.snowcast( hazelcastInstance );
 * </pre>
 * <p>Depending on the given {@link com.hazelcast.core.HazelcastInstance} (Hazelcast client or
 * embedded node) the returned Snowcast implementation is different. Anyways the implementation
 * is fully thread-safe.</p>
 */
@ThreadSafe
public interface Snowcast {

    /**
     * Creates a {@link com.noctarius.snowcast.SnowcastSequencer} or returns an already existing
     * instance based on the given sequencerName and custom epoch. This method is fully thread-safe
     * and safe to be called concurrently.<br>
     * This operation is a cluster wide operation and blocks until the sequencer's configuration
     * is registered or checked and the {@link com.noctarius.snowcast.SnowcastSequencer} instance is
     * created or retrieved.
     *
     * @param sequencerName the reference name of the distributed sequencer
     * @param epoch         the custom epoch for this sequencer
     * @return a new SnowcastSequencer instance or an existing one matching the sequencerName and epoch
     */
    @Nonnull
    SnowcastSequencer createSequencer(@Nonnull String sequencerName, @Nonnull SnowcastEpoch epoch);

    /**
     * Creates a {@link com.noctarius.snowcast.SnowcastSequencer} or returns an already existing
     * instance based on the given sequencerName and custom epoch. This method is fully thread-safe
     * and safe to be called concurrently.<br>
     * This operation is a cluster wide operation and blocks until the sequencer's configuration
     * is registered or checked and the {@link com.noctarius.snowcast.SnowcastSequencer} instance is
     * created or retrieved.
     *
     * @param sequencerName       the reference name of the distributed sequencer
     * @param epoch               the custom epoch for this sequencer
     * @param maxLogicalNodeCount the maximal logical node ID, must be between 128 and 8192
     * @return a new SnowcastSequencer instance or an existing one matching the sequencerName and epoch
     */
    @Nonnull
    SnowcastSequencer createSequencer(@Nonnull String sequencerName, @Nonnull SnowcastEpoch epoch,
                                      @Min(128) @Max(8192) int maxLogicalNodeCount);

    /**
     * Destroys the given {@link com.noctarius.snowcast.SnowcastSequencer} instance. A sequencer should
     * be destroyed by the same Snowcast instance it was created with otherwise unexpected behavior
     * might occur.<br>
     * This operation is a cluster wide operation and destroys all instances of the referenced
     * {@link com.noctarius.snowcast.SnowcastSequencer} on all nodes! Existing references to this sequencer
     * will transition to state {@link com.noctarius.snowcast.SnowcastSequenceState#Destroyed} and cannot
     * be used to generate further snowcast sequence IDs.
     *
     * @param sequencer the sequencer to destroy
     */
    void destroySequencer(@Nonnull SnowcastSequencer sequencer);

    /**
     * Creates or retrieves (a cached) {@link com.noctarius.snowcast.Snowcast} instance assigned to the passed in
     * {@link com.hazelcast.core.HazelcastInstance}. An implicit value of 1 (ONE) will be configured as the backup count
     * for all {@link com.noctarius.snowcast.SnowcastSequencer} instances created by the returned
     * {@link com.noctarius.snowcast.Snowcast} instance.
     *
     * @param hazelcastInstance the HazelcastInstance with link the Snowcast instance to
     * @return a created or cached Snowcast instance, linked to the given HazelcastInstance
     * @throws com.noctarius.snowcast.SnowcastException if creation fails for various reasons
     */
    @Nonnull
    static Snowcast snowcast(@Nonnull HazelcastInstance hazelcastInstance) {
        return snowcast(hazelcastInstance, 1);
    }

    /**
     * Creates or retrieves (a cached) {@link com.noctarius.snowcast.Snowcast} instance assigned to the passed in
     * {@link com.hazelcast.core.HazelcastInstance}. The defined <tt>backupCount</tt> will be set as the backup count
     * for all {@link com.noctarius.snowcast.SnowcastSequencer} instances created by the returned
     * {@link com.noctarius.snowcast.Snowcast} instance.
     *
     * @param hazelcastInstance the HazelcastInstance with link the Snowcast instance to
     * @param backupCount       the amount of backups to be stored on other nodes, default is one backup
     * @return a created or cached Snowcast instance, linked to the given HazelcastInstance
     * @throws com.noctarius.snowcast.SnowcastException if creation fails for various reasons
     */
    @Nonnull
    public static Snowcast snowcast(@Nonnull HazelcastInstance hazelcastInstance,
                                    @Nonnegative @Max(Short.MAX_VALUE) int backupCount) {

        return SnowcastSystem.snowcast(hazelcastInstance, backupCount);
    }
}
