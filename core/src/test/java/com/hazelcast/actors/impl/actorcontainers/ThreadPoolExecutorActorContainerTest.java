package com.hazelcast.actors.impl.actorcontainers;

import com.hazelcast.actors.api.ActorRef;
import com.hazelcast.actors.impl.ActorRuntimeProxyImpl;
import com.hazelcast.actors.impl.ActorService;
import com.hazelcast.actors.impl.ActorServiceConfig;
import com.hazelcast.actors.impl.BasicActorFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.ServicesConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spi.impl.NodeEngineImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExecutorActorContainerTest {

    private static ActorRuntimeProxyImpl actorRuntime;
    private static HazelcastInstance hzInstance;
    private static IMap<ActorRef, Set<ActorRef>> monitorMap;
    private static BasicActorFactory actorFactory;
    private static NodeEngineImpl nodeService;
    private static ExecutorService executor;

    @BeforeClass
    public static void beforeClass() {
        Config config = new Config();
        ServicesConfig services = config.getServicesConfig();

        ActorServiceConfig actorServiceConfig = new ActorServiceConfig();
        actorServiceConfig.setEnabled(true);
        services.addServiceConfig(actorServiceConfig);

        hzInstance = Hazelcast.newHazelcastInstance(config);
        actorRuntime = (ActorRuntimeProxyImpl) hzInstance.getDistributedObject(ActorService.SERVICE_NAME, "foo");
        monitorMap = hzInstance.getMap("linkedMap");
        actorFactory = new BasicActorFactory();
        nodeService = (NodeEngineImpl) actorRuntime.getNodeEngine();
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterClass
    public static void afterClass() {
        Hazelcast.shutdownAll();
    }
}
