package com.akka.kublab7;

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
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "PersistenceSystem");

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

            sharding.init(Entity.of(AccountEntity.ENTITY_KEY, entityContext ->
                AccountEntity.create(entityContext.getEntityId())));

            EntityRef<AccountEntity.Command> account42 =
                sharding.entityRefFor(AccountEntity.ENTITY_KEY, "account-42");

            account42.tell(new AccountEntity.Deposit("tx-1", 100));
            account42.tell(new AccountEntity.Withdraw("tx-2", 30));
            account42.tell(new AccountEntity.Deposit("tx-3", 20));

            context.getLog().info(
                "Lab 7 started: persistent sharding initialized, demo commands sent for account-42");
            return Behaviors.empty();
        });
    }
}
