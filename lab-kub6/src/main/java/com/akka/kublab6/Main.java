package com.akka.kublab6;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;

public class Main {

    public static void main(String[] args) {
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "ShardingSystem");

        AkkaManagement.get(system).start();
        ClusterBootstrap.get(system).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AkkaManagement.get(system).stop();
            system.terminate();
        }));
    }

    private static Behavior<Void> rootBehavior() {
        return Behaviors.setup(context -> {
            ClusterSharding sharding = ClusterSharding.get(context.getSystem());

            sharding.init(
                Entity.of(AccountActor.ENTITY_KEY, entityContext ->
                    AccountActor.create(entityContext.getEntityId()))
            );

            EntityRef<AccountActor.Command> user42 = sharding.entityRefFor(AccountActor.ENTITY_KEY, "user-42");
            EntityRef<AccountActor.Command> user77 = sharding.entityRefFor(AccountActor.ENTITY_KEY, "user-77");

            user42.tell(new AccountActor.UpdateBalance(100));
            user42.tell(new AccountActor.UpdateBalance(-40));
            user77.tell(new AccountActor.UpdateBalance(250));

            context.getLog().info("Cluster Sharding initialized, test messages sent");
            return Behaviors.empty();
        });
    }
}
