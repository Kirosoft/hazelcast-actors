package com.hazelcast.actors.impl;

import java.io.IOException;

import com.hazelcast.actors.api.ActorRef;
import com.hazelcast.actors.utils.Util;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.AbstractOperation;

class SendOperation extends AbstractOperation {
    private String name;
    private String destinationId;
    private Object message;
    private ActorRef sender;
    private transient Object response;

    SendOperation() {
    }

    SendOperation(ActorRef sender, String name, String destinationId, Object message) {
        this.destinationId = destinationId;
        this.message = message;
        this.name = name;
        this.sender = sender;
    }

    @Override
    public void writeInternal(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(destinationId);
        byte[] messageBytes = Util.toBytes(message);
        out.writeInt(messageBytes.length);
        out.write(messageBytes);

        if (sender == null) {
            out.writeInt(0);
        } else {
            byte[] senderBytes = Util.toBytes(sender);
            out.writeInt(senderBytes.length);
            out.write(senderBytes);
        }
    }

    @Override
    public void readInternal(ObjectDataInput in) throws IOException {
        name = in.readUTF();
        destinationId = in.readUTF();

        int messageBytesLength = in.readInt();
        byte[] recipeBytes = new byte[messageBytesLength];
        in.readFully(recipeBytes);
        this.message = Util.toObject(recipeBytes);

        int senderBytesLength = in.readInt();
        if (senderBytesLength > 0) {
            byte[] senderBytes = new byte[senderBytesLength];
            in.readFully(senderBytes);
            this.sender = (ActorRef) Util.toObject(senderBytes);
        }
    }

    @Override
    public void run() {
        ActorService actorService = (ActorService) getService();
        ActorPartitionContainer container = actorService.partitionContainers[getPartitionId()];
        ActorPartition actorPartition = container.getPartition(name);
        try {
            actorPartition.send(sender, destinationId, message);
        } catch (Exception e) {
            response = e;
        }
    }

    @Override
    public Object getResponse() {
        return response;
    }

    @Override
    public boolean returnsResponse() {
        return true;
    }
}
