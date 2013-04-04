package com.hazelcast.actors;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.actors.actors.DispatchingActor;
import com.hazelcast.actors.api.ActorRecipe;
import com.hazelcast.actors.api.ActorRef;
import com.hazelcast.actors.utils.Util;

public class ExitTest extends AbstractTest{

    @Test
    @Ignore
    public void exitRootActorWithoutChildren() {
        ActorRef ref = actorRuntime.spawn(new ActorRecipe(TerminationActor.class));
        TerminationActor terminationActor = (TerminationActor) actorRuntime.getActor(ref);
        actorRuntime.exit(ref);
        terminationActor.assertTerminatedEventually();
    }

    @Test
    @Ignore
    public void exitRootActorWithChildren() {
        ActorRef ref = actorRuntime.spawn(new ActorRecipe(TerminationActor.class));
        TerminationActor terminationActor = (TerminationActor) actorRuntime.getActor(ref);
        actorRuntime.send(ref,new CreateChild());

        actorRuntime.exit(ref);
        terminationActor.assertTerminatedEventually();

        TerminationActor child = (TerminationActor)actorRuntime.getActor(terminationActor.children.get(0));
        child.assertTerminatedEventually();
    }

    @Test
    @Ignore
    public void exitWhenAlreadyTerminated(){
        ActorRef ref = actorRuntime.spawn(new ActorRecipe(TerminationActor.class));
        TerminationActor terminationActor = (TerminationActor) actorRuntime.getActor(ref);
        actorRuntime.exit(ref);
        terminationActor.assertTerminatedEventually();

        actorRuntime.exit(ref);
    }

    public static class CreateChild implements Serializable {
    }

    public static class TerminationActor extends DispatchingActor {
        private volatile boolean terminated = false;
        private final List<ActorRef> children = new Vector<>();

        public void receive(CreateChild msg) throws Exception {
            ActorRef child = spawnAndLink(TerminationActor.class);
            children.add(child);
        }

        @Override
        public void onExit() throws Exception {
            System.out.println(self()+" Terminated");
            terminated = true;
        }

        public void assertTerminatedEventually() {
            for (int k = 0; k < 60; k++) {
                if (terminated) {
                    return;
                }

                Util.sleep(1000);
            }

            Assert.fail("Failed to exit actor");
        }
    }
}
