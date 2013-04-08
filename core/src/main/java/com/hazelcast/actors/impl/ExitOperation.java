package com.hazelcast.actors.impl;

import java.io.IOException;

import com.hazelcast.actors.api.ActorRef;
import com.hazelcast.actors.utils.Util;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.AbstractOperation;

class ExitOperation extends AbstractOperation {
    private String name;
    private ActorRef target;

    ExitOperation() {
    }

    ExitOperation(String name, ActorRef target) {
        this.name = name;
        this.target = target;
    }

    @Override
    public void writeInternal(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
        byte[] targetBytes = Util.toBytes(target);
        out.writeInt(targetBytes.length);
        out.write(targetBytes);
    }

    @Override
    public void readInternal(ObjectDataInput in) throws IOException {
        name = in.readUTF();
        int targetBytesLength = in.readInt();
        byte[] targetBytes = new byte[targetBytesLength];
        in.readFully(targetBytes);
        this.target = (ActorRef) Util.toObject(targetBytes);
    }

    @Override
    public void run() {
        ActorService actorService = (ActorService) getService();
        ActorPartitionContainer actorPartitionContainer = actorService.partitionContainers[getPartitionId()];
        ActorPartition actorPartition = actorPartitionContainer.getPartition(name);
        try {
            actorPartition.exit(target);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean returnsResponse() {
        return false;
    }
}
