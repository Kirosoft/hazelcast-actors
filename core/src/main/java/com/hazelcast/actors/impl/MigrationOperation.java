package com.hazelcast.actors.impl;

import com.hazelcast.actors.utils.Util;
import com.hazelcast.spi.AbstractOperation;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;

class MigrationOperation extends AbstractOperation {

    private ActorPartitionContent[] changes;

    public MigrationOperation() {
    }

    public MigrationOperation(int partitionId, ActorPartitionContent[] changes) {
        setPartitionId(partitionId);
        this.changes = changes;
    }

    public String getServiceName() {
        return ActorService.SERVICE_NAME;
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        changes = (ActorPartitionContent[]) Util.toObject(bytes);
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        byte[] bytes = Util.toBytes(changes);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    @Override
    public void run() {
        ActorService actorService = (ActorService) getService();
        ActorPartitionContainer container = actorService.partitionContainers[getPartitionId()];
        container.applyChanges(changes);
    }
}
