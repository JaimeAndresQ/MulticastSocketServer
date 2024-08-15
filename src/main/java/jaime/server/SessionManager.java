package jaime.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionManager {
        private static final ConcurrentMap<String, Session> listenSessions = new ConcurrentHashMap<>();
        private static final ConcurrentMap<String, Session> broadcastSessions = new ConcurrentHashMap<>();

        public static void addListenSession(Session session) {
            listenSessions.put(session.getClientId(), session);
        }

        public static void addBroadcastSession(Session session) {
            broadcastSessions.put(session.getClientId(), session);
        }

        public static void removeSession(Session session) {
            listenSessions.remove(session.getClientId());
            broadcastSessions.remove(session.getClientId());
        }

        public static Session getListenSession(String clientId) {
            return listenSessions.get(clientId);
        }

        public static Session getBroadcastSession(String clientId) {
            return broadcastSessions.get(clientId);
        }

        public static Collection<Session> getAllListenSessions() {
            return listenSessions.values();
        }

        public static Collection<Session> getAllBroadcastSessions() {
            return broadcastSessions.values();
        }

        public static Collection<String> getAllListenId(){
            return listenSessions.keySet();
        }

        public static Collection<String> getAllBroadcastId(){
            return broadcastSessions.keySet();
        }

}

