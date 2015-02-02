package com.hazelcast.actors.api;

import com.hazelcast.actors.utils.Util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * The ActorRecipe is a recipe to instantiate a new actor. The same recipe can be used to create multiple actors.
 *
 * @param <A>
 */
public class ActorRecipe<A extends Actor> implements Serializable {
    private final String actorClass;
    private final String partitionKey;
    private final Map<String, Object> properties;

    public ActorRecipe(Class<A> actorClass){
        this(actorClass, null);
    }

    public ActorRecipe(Class<A> actorClass, String partitionKey) {
        this(actorClass, partitionKey, null);
    }

    public ActorRecipe(Class<A> actorClass, String partitionKey, Map<String, Object> properties) {
        this.actorClass = Util.notNull(actorClass, "actorClass").getName();
        this.partitionKey = partitionKey;
        this.properties = properties;
    }

    public Class<A> getActorClass() {
        try {
            return (Class<A>) ActorRecipe.class.getClassLoader().loadClass(actorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            return Collections.EMPTY_MAP;
        } else {
            return properties;
        }
    }

    @Override
    public String toString() {
        return "ActorRecipe{" +
                "actorClass=" + actorClass +
                ", partitionKey=" + partitionKey +
                ", properties=" + properties +
                '}';
    }
}
