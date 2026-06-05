package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DistanceVectorRouting {

    // Definition für INFINITY laut PDF-Vorgabe
    public static final int INFINITY = 999;

    // 1. Repräsentiert eine Zeile in der Routing-Tabelle
    static class RoutingEntry {
        String destination;
        int distance;
        String nextHop;

        public RoutingEntry(String destination, int distance, String nextHop) {
            this.destination = destination;
            this.distance = distance;
            this.nextHop = nextHop;
        }

        public RoutingEntry cloneEntry() {
            return new RoutingEntry(this.destination, this.distance, this.nextHop);
        }
    }

    // 2. Repräsentiert einen Router im Netzwerk
    static class Router {
        String name;
        List<String> neighbors = new ArrayList<>();
        Map<String, RoutingEntry> table = new HashMap<>();

        public Router(String name) {
            this.name = name;
        }

        public void findNeighbors() {
            for (var entry : table.values()) {
                if (!entry.destination.equals(name)
                        && entry.distance != INFINITY
                        && !entry.nextHop.equals("-")) {
                    neighbors.add(entry.destination);
                }
            }
        }
    }

    // 3. Der Bellman-Ford Algorithmus für eine einzelne Runde
    public static boolean runRoutingRound(Map<String, Router> network) {
        boolean anyChange = false;

        // Snapshot der Vorrunde erstellen
        Map<String, Map<String, RoutingEntry>> snapshot = new HashMap<>();
        for (String routerName : network.keySet()) {
            Map<String, RoutingEntry> clonedTable = new HashMap<>();
            for (var entry : network.get(routerName).table.entrySet()) {
                clonedTable.put(entry.getKey(), entry.getValue().cloneEntry());
            }
            snapshot.put(routerName, clonedTable);
        }

        // Jeder Router lernt von seinen Nachbarn
        for (Router router : network.values()) {
            for (String neighborName : router.neighbors) {

                if (!snapshot.containsKey(neighborName)) continue;

                // Kosten zum Nachbarn holen wir uns direkt aus der Tabelle der Vorrunde
                int costToNeighbor = snapshot.get(router.name).get(neighborName).distance;
                if (costToNeighbor == INFINITY) continue;

                Map<String, RoutingEntry> neighborTable = snapshot.get(neighborName);

                for (String dest : neighborTable.keySet()) {
                    if (neighborTable.get(dest) == null) continue;

                    int distanceFromNeighborToDest = neighborTable.get(dest).distance;

                    if (distanceFromNeighborToDest != INFINITY) {
                        int newPotentialCost = costToNeighbor + distanceFromNeighborToDest;

                        RoutingEntry currentEntry = router.table.get(dest);
                        if (currentEntry != null) {
                            // ENTSCHEIDENDE KORREKTUR:
                            // Wir updaten, wenn der alte Weg INFINITY war ODER der neue Weg echt kürzer ist!
                            if (currentEntry.distance == INFINITY || newPotentialCost < currentEntry.distance) {
                                currentEntry.distance = newPotentialCost;
                                currentEntry.nextHop = neighborName;
                                anyChange = true;
                            }
                        }
                    }
                }
            }
        }
        return anyChange;
    }

    // 4. Hilfsmethode zur formatierten Ausgabe
    public static void printNetworkTables(Map<String, Router> network) {
        for (Router router : network.values()) {
            System.out.println("Tabelle für Router " + router.name + ":");
            System.out.println("Ziel | Distanz | Next Hop");
            System.out.println("-------------------------");

            List<String> destinations = new ArrayList<>(router.table.keySet());
            Collections.sort(destinations);

            for (String dest : destinations) {
                RoutingEntry entry = router.table.get(dest);
                String distStr = (entry.distance == INFINITY) ? "INF" : String.valueOf(entry.distance);
                System.out.printf("  %s  |   %3s   |    %s\n", entry.destination, distStr, entry.nextHop);
            }
            System.out.println();
        }
    }

    // 5. Hauptmethode
    public static void main(String[] args) {
        Map<String, Router> network = new TreeMap<>();
        String csvPath = "routingTables.csv";

        System.out.println("Lese initiale Routingtabellen aus " + csvPath + " ein...");

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            Router currentRouter = null;

            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                String[] rawParts = line.split(",|\\t");

                List<String> tokens = new ArrayList<>();
                for (String p : rawParts) {
                    String clean = p.replaceAll("\\s+", "");
                    if (!clean.isEmpty()) {
                        tokens.add(clean);
                    }
                }

                if (tokens.isEmpty()) continue;

                String possibleRouterName = tokens.get(0);

                if (possibleRouterName.matches("^[A-M]$") && tokens.size() == 1) {
                    currentRouter = new Router(possibleRouterName);
                    network.put(possibleRouterName, currentRouter);
                } else if (currentRouter != null && tokens.size() >= 3) {
                    String dest = tokens.get(0);
                    String distStr = tokens.get(1).toUpperCase();
                    String hop = tokens.get(2);

                    int dist = distStr.contains("INFINITY") ? INFINITY : Integer.parseInt(distStr);
                    currentRouter.table.put(dest, new RoutingEntry(dest, dist, hop));
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler: CSV-Datei '" + csvPath + "' konnte nicht geöffnet werden.");
            return;
        } catch (NumberFormatException e) {
            System.err.println("Formatierungsfehler beim Parsen einer Zahl: " + e.getMessage());
            return;
        }

        if (network.isEmpty()) {
            System.err.println("Kritischer Fehler: Keine Router aus der CSV geladen. Bitte Struktur prüfen!");
            return;
        }

        for (Router r : network.values()) {
            r.findNeighbors();
        }

        System.out.println("\n=== INITIALE TABELLEN ===");
        printNetworkTables(network);

        int round = 1;
        boolean changed = true;

        while (changed) {
            changed = runRoutingRound(network);

            System.out.println("=========================");
            System.out.println("=== ENDE RUNDE " + round + " ===");
            System.out.println("=========================");
            printNetworkTables(network);

            if (changed) {
                round++;
            }
        }

        System.out.println("==================================================");
        System.out.println("Das System ist nach " + round + " Runden stabil.");
        System.out.println("==================================================");

        if (network.containsKey("A") && network.get("A").table.containsKey("M")) {
            System.out.println("Kürzeste Distanz von A nach M: " + network.get("A").table.get("M").distance + " (Soll: 8)");
        }
        if (network.containsKey("F") && network.get("F").table.containsKey("I")) {
            System.out.println("Kürzeste Distanz von F nach I: " + network.get("F").table.get("I").distance + " (Soll: 10)");
        }
    }
}