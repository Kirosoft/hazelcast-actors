package com.hazelcast.actors;

import com.hazelcast.actors.api.ActorRef;
import com.hazelcast.actors.impl.ActorRuntimeProxyImpl;
import com.hazelcast.actors.impl.ActorService;
import com.hazelcast.actors.impl.ActorServiceConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.ServicesConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Set;

public abstract class AbstractTest {

    protected static ActorRuntimeProxyImpl actorRuntime;
    protected static HazelcastInstance hzInstance;
    protected static IMap<ActorRef, Set<ActorRef>> linksMap;

    @BeforeClass
    public static void setUp() {
        Config config = new Config();
        ServicesConfig servicesConfig = config.getServicesConfig();

        ActorServiceConfig actorServiceConfig = new ActorServiceConfig();
        actorServiceConfig.setEnabled(true);
        servicesConfig.addServiceConfig(actorServiceConfig);

        hzInstance = Hazelcast.newHazelcastInstance(config);
        actorRuntime = (ActorRuntimeProxyImpl) hzInstance.getDistributedObject(ActorService.SERVICE_NAME, "foo");
        linksMap = hzInstance.getMap("linksMap");
    }

    @AfterClass
    public static void tearDown() {
        actorRuntime.destroy();
        Hazelcast.shutdownAll();
    }
}
