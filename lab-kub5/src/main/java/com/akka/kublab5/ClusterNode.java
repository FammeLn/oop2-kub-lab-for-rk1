package com.akka.kublab5;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ClusterEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;

/**
 * Узел кластера, который подписывается на события кластера
 * и логирует информацию о membership.
 * 
 * ЭТО ДЕМОНСТРАЦИОННЫЙ АКТОР для наблюдения за кластером.
 * Он показывает, как узлы реагируют на изменения состава кластера.
 */
public class ClusterNode extends AbstractBehavior<ClusterNode.Command> {

    // ============ ИНТЕРФЕЙС КОМАНД ============
    
    /**
     * Базовый интерфейс для всех команд узла
     */
    public interface Command {}

    /**
     * Команда для получения информации о текущем состоянии кластера
     */
    public static final class GetClusterInfo implements Command {
        public final ActorRef<String> replyTo;
        
        public GetClusterInfo(ActorRef<String> replyTo) {
            this.replyTo = replyTo;
        }
    }

    /**
     * Внутренняя команда - оборачивает события кластера
     */
    private static final class ClusterEventWrapper implements Command {
        public final ClusterEvent.ClusterDomainEvent event;
        
        public ClusterEventWrapper(ClusterEvent.ClusterDomainEvent event) {
            this.event = event;
        }
    }

    // ============ СОЗДАНИЕ АКТОРА ============

    /**
     * Создание поведения узла кластера
     */
    public static Behavior<Command> create() {
        return Behaviors.setup(ClusterNode::new);
    }

    // ============ КОНСТРУКТОР ============

    private final Cluster cluster;
    private final String selfAddress;

    private ClusterNode(ActorContext<Command> context) {
        super(context);
        
        // Получаем доступ к Cluster extension
        this.cluster = Cluster.get(context.getSystem());
        this.selfAddress = cluster.selfMember().address().toString();
        
        getContext().getLog().info("🚀 ClusterNode запущен на адресе: {}", selfAddress);
        
        // Подписываемся на события кластера
        subscribeToClusterEvents();
        
        // Логируем начальную информацию
        logClusterState();
    }

    /**
     * Подписка на события кластера
     */
    private void subscribeToClusterEvents() {
        // Адаптер для преобразования ClusterEvent -> Command
        ActorRef<ClusterEvent.ClusterDomainEvent> adapter = 
            getContext().messageAdapter(ClusterEvent.ClusterDomainEvent.class, ClusterEventWrapper::new);
        
        // Подписываемся на базовое событие - все события будут обработаны
        cluster.subscriptions().tell(new Subscribe(adapter, ClusterEvent.ClusterDomainEvent.class));
        
        getContext().getLog().info("✅ Подписка на события кластера активирована");
    }

    /**
     * Логирование текущего состояния кластера
     */
    private void logClusterState() {
        int memberCount = 0;
        for (@SuppressWarnings("unused") akka.cluster.Member m : cluster.state().getMembers()) {
            memberCount++;
        }
        
        getContext().getLog().info("""
            
            ═══════════════════════════════════════════════════════════════
            📊 СОСТОЯНИЕ КЛАСТЕРА
            ═══════════════════════════════════════════════════════════════
            🏠 Мой адрес:      {}
            👥 Членов кластера: {}
            👑 Лидер:          {}
            🌐 Роли:           {}
            ═══════════════════════════════════════════════════════════════
            """,
            selfAddress,
            memberCount,
            cluster.state().getLeader() != null ? cluster.state().getLeader() : "нет",
            cluster.selfMember().getRoles()
        );
    }

    // ============ ОБРАБОТКА СООБЩЕНИЙ ============

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ClusterEventWrapper.class, this::onClusterEvent)
            .onMessage(GetClusterInfo.class, this::onGetClusterInfo)
            .build();
    }

    /**
     * Обработка событий кластера
     */
    private Behavior<Command> onClusterEvent(ClusterEventWrapper wrapper) {
        ClusterEvent.ClusterDomainEvent event = wrapper.event;
        
        if (event instanceof ClusterEvent.MemberUp) {
            ClusterEvent.MemberUp memberUp = (ClusterEvent.MemberUp) event;
            getContext().getLog().info("✅ Узел присоединился к кластеру: {}", memberUp.member().address());
            
        } else if (event instanceof ClusterEvent.MemberRemoved) {
            ClusterEvent.MemberRemoved memberRemoved = (ClusterEvent.MemberRemoved) event;
            getContext().getLog().warn("❌ Узел покинул кластер: {}", memberRemoved.member().address());
            
        } else if (event instanceof ClusterEvent.UnreachableMember) {
            ClusterEvent.UnreachableMember unreachable = (ClusterEvent.UnreachableMember) event;
            getContext().getLog().warn("⚠️  Узел недоступен: {}", unreachable.member().address());
            
        } else if (event instanceof ClusterEvent.ReachableMember) {
            ClusterEvent.ReachableMember reachable = (ClusterEvent.ReachableMember) event;
            getContext().getLog().info("✅ Узел снова доступен: {}", reachable.member().address());
            
        } else if (event instanceof ClusterEvent.LeaderChanged) {
            ClusterEvent.LeaderChanged leaderChanged = (ClusterEvent.LeaderChanged) event;
            String newLeader = leaderChanged.getLeader() != null ? 
                leaderChanged.getLeader().toString() : "нет";
            getContext().getLog().info("👑 Новый лидер кластера: {}", newLeader);
        }
        
        return this;
    }

    /**
     * Обработка запроса информации о кластере
     */
    private Behavior<Command> onGetClusterInfo(GetClusterInfo msg) {
        int memberCount = 0;
        for (@SuppressWarnings("unused") akka.cluster.Member m : cluster.state().getMembers()) {
            memberCount++;
        }
        
        String info = String.format(
            "Кластер: %d членов, Лидер: %s",
            memberCount,
            cluster.state().getLeader()
        );
        msg.replyTo.tell(info);
        return this;
    }
}
