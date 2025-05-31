import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DMSGBuilder {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java DMSGBuilder <path_to_csv>");
            System.exit(1);
        }

        String csvPath = args[0];

        // Map mutant ID to the set of test IDs that kill it
        Map<Integer, Set<Integer>> mutantKills = new HashMap<>();

        // Read CSV
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                int testId = Integer.parseInt(parts[0].trim());
                int mutantId = Integer.parseInt(parts[1].trim());

                mutantKills.computeIfAbsent(mutantId, k -> new HashSet<>()).add(testId);
            }
        }

        // Group mutants by their killing test sets
        Map<Set<Integer>, Set<Integer>> groupToMutants = new HashMap<>(new HashMap<>() {
            public Set<Integer> get(Object key) {
                for (Set<Integer> k : this.keySet()) {
                    if (k.equals(key)) return super.get(k);
                }
                return null;
            }
        });

        for (Map.Entry<Integer, Set<Integer>> entry : mutantKills.entrySet()) {
            Integer mutant = entry.getKey();
            Set<Integer> tests = entry.getValue();

            boolean found = false;
            for (Set<Integer> key : groupToMutants.keySet()) {
                if (key.equals(tests)) {
                    groupToMutants.get(key).add(mutant);
                    found = true;
                    break;
                }
            }
            if (!found) {
                groupToMutants.put(new HashSet<>(tests), new HashSet<>(List.of(mutant)));
            }
        }

        // Map each group to a unique ID
        List<Set<Integer>> groups = new ArrayList<>(groupToMutants.keySet());
        Map<Set<Integer>, String> groupIds = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            Set<Integer> group = groups.get(i);
            Set<Integer> mutants = groupToMutants.get(group);
            String nodeLabel = mutants.toString();
            groupIds.put(group, nodeLabel);
        }

        // Build DMSG
        Map<String, Set<String>> graph = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0; j < groups.size(); j++) {
                if (i == j) continue;
                Set<Integer> groupA = groups.get(i);
                Set<Integer> groupB = groups.get(j);

                if (groupB.containsAll(groupA)) {
                    graph.computeIfAbsent(groupIds.get(groupA), k -> new HashSet<>()).add(groupIds.get(groupB));
                }
            }
        }

        // Write to DOT file
        try (PrintWriter out = new PrintWriter("dmsg.dot")) {
            out.println("digraph DMSG {");
            for (String from : graph.keySet()) {
                for (String to : graph.get(from)) {
                    out.printf("  \"%s\" -> \"%s\";%n", from, to);
                }
            }
            out.println("}");
        }

        // Identify dominators (nodes with no incoming edges)
        Set<String> allNodes = new HashSet<>(groupIds.values());
        Set<String> subsumedNodes = new HashSet<>();
        for (Set<String> subsumed : graph.values()) {
            subsumedNodes.addAll(subsumed);
        }

        Set<String> dominatorNodes = new HashSet<>(allNodes);
        dominatorNodes.removeAll(subsumedNodes);

        System.out.println("Dominator mutants:");
        for (String label : dominatorNodes) {
            System.out.println(label);
        }
    }
}
