package com.akka.kublab7;

import akka.actor.typed.Behavior;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.RecoveryCompleted;
import akka.persistence.typed.javadsl.SignalHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AccountEntity extends EventSourcedBehavior<AccountEntity.Command, AccountEntity.Event, AccountEntity.State> {

    private static final Logger LOG = LoggerFactory.getLogger(AccountEntity.class);

    public interface Command extends CborSerializable {
    }

    public interface Event extends CborSerializable {
    }

    public static final class Deposit implements Command {
        public final String transactionId;
        public final int amount;

        public Deposit(String transactionId, int amount) {
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    public static final class Withdraw implements Command {
        public final String transactionId;
        public final int amount;

        public Withdraw(String transactionId, int amount) {
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    public static final class Deposited implements Event {
        public final String transactionId;
        public final int amount;

        public Deposited(String transactionId, int amount) {
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    public static final class Withdrawn implements Event {
        public final String transactionId;
        public final int amount;

        public Withdrawn(String transactionId, int amount) {
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    public static final class State implements CborSerializable {
        public final int balance;
        public final Set<String> appliedTransactions;

        public State(int balance, Set<String> appliedTransactions) {
            this.balance = balance;
            this.appliedTransactions = Collections.unmodifiableSet(appliedTransactions);
        }

        public static State empty() {
            return new State(0, new HashSet<>());
        }

        public boolean containsTransaction(String transactionId) {
            return appliedTransactions.contains(transactionId);
        }

        public State apply(Event event) {
            Set<String> nextTransactions = new HashSet<>(appliedTransactions);

            if (event instanceof Deposited deposited) {
                nextTransactions.add(deposited.transactionId);
                return new State(balance + deposited.amount, nextTransactions);
            }

            if (event instanceof Withdrawn withdrawn) {
                nextTransactions.add(withdrawn.transactionId);
                return new State(balance - withdrawn.amount, nextTransactions);
            }

            return this;
        }
    }

    public static final EntityTypeKey<Command> ENTITY_KEY =
        EntityTypeKey.create(Command.class, "AccountPersistentEntity");

    private final String entityId;

    public static Behavior<Command> create(String entityId) {
        return new AccountEntity(entityId);
    }

    private AccountEntity(String entityId) {
        super(PersistenceId.of(ENTITY_KEY.name(), entityId));
        this.entityId = entityId;
    }

    @Override
    public State emptyState() {
        return State.empty();
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(Deposit.class, this::onDeposit)
            .onCommand(Withdraw.class, this::onWithdraw)
            .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return (state, event) -> state.apply(event);
    }

    @Override
    public SignalHandler<State> signalHandler() {
        return newSignalHandlerBuilder()
            .onSignal(RecoveryCompleted.instance(), state ->
                LOG.info(
                    "Recovery completed for entity={}, balance={}, appliedTransactions={}",
                    entityId,
                    state.balance,
                    state.appliedTransactions.size()))
            .build();
    }

    private Effect<Event, State> onDeposit(State state, Deposit command) {
        if (command.amount <= 0) {
            LOG.warn("Ignore invalid deposit for entity={}, amount={}", entityId, command.amount);
            return Effect().none();
        }

        if (state.containsTransaction(command.transactionId)) {
            LOG.info(
                "Duplicate deposit ignored for entity={}, txId={}",
                entityId,
                command.transactionId);
            return Effect().none();
        }

        return Effect()
            .persist(new Deposited(command.transactionId, command.amount))
            .thenRun(newState ->
                LOG.info(
                    "Deposit applied entity={}, txId={}, amount={}, balance={}",
                    entityId,
                    command.transactionId,
                    command.amount,
                    newState.balance));
    }

    private Effect<Event, State> onWithdraw(State state, Withdraw command) {
        if (command.amount <= 0) {
            LOG.warn("Ignore invalid withdraw for entity={}, amount={}", entityId, command.amount);
            return Effect().none();
        }

        if (state.containsTransaction(command.transactionId)) {
            LOG.info(
                "Duplicate withdraw ignored for entity={}, txId={}",
                entityId,
                command.transactionId);
            return Effect().none();
        }

        if (state.balance < command.amount) {
            LOG.warn(
                "Withdraw rejected entity={}, txId={}, amount={}, balance={}",
                entityId,
                command.transactionId,
                command.amount,
                state.balance);
            return Effect().none();
        }

        return Effect()
            .persist(new Withdrawn(command.transactionId, command.amount))
            .thenRun(newState ->
                LOG.info(
                    "Withdraw applied entity={}, txId={}, amount={}, balance={}",
                    entityId,
                    command.transactionId,
                    command.amount,
                    newState.balance));
    }
}
