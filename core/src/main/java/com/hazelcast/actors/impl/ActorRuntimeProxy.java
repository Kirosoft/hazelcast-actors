package com.hazelcast.actors.impl;

import com.hazelcast.actors.api.ActorRuntime;
import com.hazelcast.core.DistributedObject;

public interface ActorRuntimeProxy extends ActorRuntime, DistributedObject {
}
