package com.hazelcast.actors.impl;

import com.hazelcast.partition.PartitionInfo;
import com.hazelcast.spi.Operation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hazelcast.actors.utils.Util.notNull;

class ActorPartitionContainer {

    private final ConcurrentMap<String, ActorPartition> partitionMap = new ConcurrentHashMap<>();
    //private final PartitionInfo partitionInfo;
    private final ActorService actorService;
    private final int partitionId;

    public ActorPartitionContainer(ActorService actorService, int partitionId) {
        this.actorService = notNull(actorService,"actorService");
        //this.partitionInfo = notNull(partitionInfo,"partitionInfo");
        this.partitionId = notNull(partitionId, "partitionId");
    }

    protected ActorPartition getPartition(String name) {
        notNull(name,"name");

        ActorPartition actorPartition = partitionMap.get(name);
        if (actorPartition == null) {
            actorPartition = new ActorPartition(actorService, name);
            ActorPartition found = partitionMap.putIfAbsent(name, actorPartition);
            actorPartition = found != null ? found : actorPartition;
        }
        return actorPartition;
    }

    protected Operation createMigrationOperation() {
        ActorPartitionContent[] partitionChanges = new ActorPartitionContent[partitionMap.size()];
        int k = 0;
        for (Map.Entry<String, ActorPartition> entry : partitionMap.entrySet()) {
            partitionChanges[k] = entry.getValue().content();
            k++;
        }

        return new MigrationOperation(this.partitionId, partitionChanges);
    }

    public void applyChanges(ActorPartitionContent[] changes) {
        //todo
    }
}
