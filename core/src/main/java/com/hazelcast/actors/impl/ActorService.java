package com.hazelcast.actors.impl;

import com.hazelcast.actors.api.*;
import com.hazelcast.actors.impl.actorcontainers.ActorContainerFactoryFactory;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.partition.PartitionInfo;
import com.hazelcast.spi.*;
import com.hazelcast.spi.impl.NodeEngineImpl;

import java.util.*;
import java.util.concurrent.*;

public class ActorService implements ManagedService, MigrationAwareService, RemoteService {

    public static final String SERVICE_NAME = "ActorService";

    private final ConcurrentMap<String, ActorRuntimeProxyImpl> actorSystems = new ConcurrentHashMap<>();

    //TODO: These need to be pulled out; made configurable. For the time being it is good enough.
    protected final ExecutorService offloadExecutor = Executors.newFixedThreadPool(16);
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);

    protected NodeEngineImpl nodeEngine;

    protected ActorPartitionContainer[] partitionContainers;
    private ActorServiceConfig actorConfig;
    protected IMap<ActorRef, Set<ActorRef>> linksMap;
    protected IMap<String, Object> responseMap;
    protected ActorFactory actorFactory;
    protected ActorContainerFactoryFactory containerFactoryFactory;
    protected IMap<ActorRef, Object> actorMap;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = (NodeEngineImpl) nodeEngine;
        nodeEngine.getLogger(ActorService.class.getName());
        this.actorConfig = findActorServiceConfig();
        this.actorFactory = actorConfig.getActorFactory();
        this.containerFactoryFactory = actorConfig.getActorContainerFactoryFactory();

        //initializing the PartitionContainers.
        partitionContainers = new ActorPartitionContainer[nodeEngine.getPartitionService().getPartitionCount()];
        for (int partitionId = 0; partitionId < partitionContainers.length; partitionId++) {
            //PartitionInfo partition = nodeEngine.getPartitionService().getPartitionInfo(partitionId);
            partitionContainers[partitionId] = new ActorPartitionContainer(this, partitionId);
        }        
    }

    private ActorServiceConfig findActorServiceConfig() {
        for (ServiceConfig config : nodeEngine.getConfig().getServicesConfig().getServiceConfigs()) {
            if (config.getName().equals(SERVICE_NAME)) {
                return (ActorServiceConfig) config;
            }
        }
        return null;
    }

    @Override
    public Operation prepareReplicationOperation(PartitionReplicationEvent e) {
        if (e.getReplicaIndex() != 0) return null;

        ActorPartitionContainer partitionContainer = partitionContainers[e.getPartitionId()];
        return partitionContainer.createMigrationOperation();
    }

    @Override
    public void beforeMigration(PartitionMigrationEvent e) {
    }

    @Override
    public void commitMigration(PartitionMigrationEvent e) {
    }

    @Override
    public void rollbackMigration(PartitionMigrationEvent e) {
    }

    @Override
    public void clearPartitionReplica(int i) {

    }

	@Override
	public DistributedObject createDistributedObject(String objectId) {
        String id = objectId;
        ActorRuntimeProxyImpl actorSystem = actorSystems.get(id);
        if (actorSystem == null) {
            actorSystem = new ActorRuntimeProxyImpl(this, id);
            ActorRuntimeProxyImpl found = actorSystems.put(id, actorSystem);
            actorSystem = found != null ? found : actorSystem;
        }
        this.linksMap = this.nodeEngine.getNode().hazelcastInstance.getMap("linksMap");
        this.actorMap = this.nodeEngine.getNode().hazelcastInstance.getMap("actorMap");
        this.responseMap = this.nodeEngine.getNode().hazelcastInstance.getMap("responseMap");

        
        return actorSystem;
	}


	@Override
	public void destroyDistributedObject(String objectId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void shutdown(boolean b) {

    }


}
