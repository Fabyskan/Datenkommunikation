package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DistanceVectorRouting {

    public static final int INFINITY = 1337; // Groß genug aber kein Überläufchen

    // ZIEL | DISTANZ | NÄCHSTER ROUTER -> Ein Entry = Eine Zeile der Tabelle
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

    // Nodes bzw die router
    static class Router {
        String name;
        List<String> neighbours = new ArrayList<>();
        Map<String, RoutingEntry> table = new HashMap<>(); //Routingtabelle

        public Router(String name) {
            this.name = name;
        }

        public void findNeighbours() {
            for (var entry : table.values()) {
                if (!entry.destination.equals(name) // Nicht mein eigener Nachbar
                        && entry.distance != INFINITY //Nicht unendlich weit entfernt
                        && !entry.nextHop.equals("-")) { // Es gibt einen gültigen SChritt
                    neighbours.add(entry.destination);
                }
            }
        }
    }

    // Bellman-Ford bzw der Lernalgo
    public static boolean runRoutingRound(Map<String, Router> network) {
        boolean anyChange = false;

        /*
        * Snapshot, damit die runden einmal sauber abgespielt werden und nicht nahc jeder Zeile
        * mit den geupdateten Werten weiter gearbeitet wird
         */
        Map<String, Map<String, RoutingEntry>> snapshot = new HashMap<>();
        for (String routerName : network.keySet()) {
            Map<String, RoutingEntry> clonedTable = new HashMap<>();
            for (var entry : network.get(routerName).table.entrySet()) {
                clonedTable.put(entry.getKey(), entry.getValue().cloneEntry());
            }
            snapshot.put(routerName, clonedTable);
        }

        // Jeder Router lernt von seinen Nachbarn
        for (Router router : network.values()) { // aktueller Router
            for (String neighbourName : router.neighbours) { // Nachbar Router (FindNeighbours

                if (!snapshot.containsKey(neighbourName)) continue;

                /*
                 * Kostenablgeich mit dem Snapshot der Vorrunde
                 */
                int costToNeighbour = snapshot.get(router.name).get(neighbourName).distance;
                if (costToNeighbour == INFINITY) continue;


                /*
                * Ein Router geht seine Nachbarn durch - holt sich die Infos aus dem snapshot
                 */
                Map<String, RoutingEntry> neighbourTable = snapshot.get(neighbourName);

                for (String dest : neighbourTable.keySet()) {
                    if (neighbourTable.get(dest) == null) continue;

                    int distanceFromNeighbourToDest = neighbourTable.get(dest).distance;


                    /*
                    * Berechnung der Kosten aus Distanz zum nachbar und der distanz zum ziel
                     */
                    if (distanceFromNeighbourToDest != INFINITY) {
                        int newPotentialCost = costToNeighbour + distanceFromNeighbourToDest;

                        /*
                        * Check, ob es einen besseren weg gibt, wenn ja neue Runde anfordern
                         */
                        RoutingEntry currentEntry = router.table.get(dest);
                        if (currentEntry != null) {
                            if (currentEntry.distance == INFINITY || newPotentialCost < currentEntry.distance) {
                                currentEntry.distance = newPotentialCost;
                                currentEntry.nextHop = neighbourName;
                                anyChange = true;
                            }
                        }
                    }
                }
            }
        }
        return anyChange;
    }


    public static void printTables(Map<String, Router> network) {
        for (Router router : network.values()) {
            System.out.println("Tabelle für Router " + router.name + ":");
            System.out.println("Ziel | Distanz | Nächster Router");
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


    public static void main(String[] args) {

        /*
        * Auslesen der CSV mit wirklich gottlosen Kopfschmerzen warum ist das so
        * */
        Map<String, Router> network = new TreeMap<>();
        String csvPath = "routingTables.csv";

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

        /*
        * Nachbarsuche starten
        * */
        for (Router r : network.values()) {
            r.findNeighbours();
        }

        /*
        *  Initialprint
         */
        System.out.println("\n=== INITIALE TABELLEN ===");
        printTables(network);

        int round = 1;
        boolean changed = true;


        /*
        *  Hauptsteuerung für BelloBello
         */
        while (changed) {
            changed = runRoutingRound(network);

            System.out.println("=========================");
            System.out.println("=== ENDE RUNDE " + round + " ===");
            System.out.println("=========================");
            printTables(network);

            if (changed) {
                round++;
            }
        }

        System.out.println("==================================================");
        System.out.println("Das System ist nach " + round + " Runden stabil.");
        System.out.println("==================================================");


    }
}