package com.akka.kublab6;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;

public class AccountActor {

    public interface Command {
    }

    public static final class UpdateBalance implements Command {
        public final int amount;

        public UpdateBalance(int amount) {
            this.amount = amount;
        }
    }

    public static final class GetBalance implements Command {
        public final ActorRef<BalanceResponse> replyTo;

        public GetBalance(ActorRef<BalanceResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class BalanceResponse {
        public final String accountId;
        public final int balance;

        public BalanceResponse(String accountId, int balance) {
            this.accountId = accountId;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return "BalanceResponse{accountId='" + accountId + "', balance=" + balance + "}";
        }
    }

    public static final EntityTypeKey<Command> ENTITY_KEY =
        EntityTypeKey.create(Command.class, "AccountEntity");

    public static Behavior<Command> create(String entityId) {
        return Behaviors.setup(context -> active(entityId, 0, context));
    }

    private static Behavior<Command> active(String entityId, int balance, ActorContext<Command> context) {
        return Behaviors.receiveMessage(message -> {
            if (message instanceof UpdateBalance updateBalance) {
                int newBalance = balance + updateBalance.amount;
                context.getLog().info("Account {} updated by {}, new balance={}", entityId, updateBalance.amount, newBalance);
                return active(entityId, newBalance, context);
            }

            if (message instanceof GetBalance getBalance) {
                getBalance.replyTo.tell(new BalanceResponse(entityId, balance));
                return Behaviors.same();
            }

            return Behaviors.unhandled();
        });
    }
}
