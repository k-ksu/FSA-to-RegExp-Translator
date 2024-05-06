import java.io.File;
import java.io.IOException;
import java.util.*;


public class FsaTranslator {
    public static void main(String[] args) {
        try {
            new FsaTranslator().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        File file = new File("input.txt");
        Scanner scanner = new Scanner(file);

        try {
            Fsa fsa = fsaInput(scanner);
            String regExp = KleeneAlgorithm(fsa);
            System.out.println(regExp);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println("E1: Input file is malformed");
        } finally {
            scanner.close();
        }
    }

    private String KleeneAlgorithm(Fsa fsa) {
        String[][] prevStage = new String[fsa.size][fsa.size];
        for (int i = 0; i < fsa.size; i++) {
            for (int j = 0; j < fsa.size; j++) {
                if (fsa.trans[i][j].isEmpty() && i != j)
                    prevStage[i][j] = "{}";
                else if (fsa.trans[i][j].isEmpty())
                    prevStage[i][j] = "eps";
                else {
                    prevStage[i][j] = "";
                    for (String s : fsa.trans[i][j]) {
                        prevStage[i][j] += s + "|";
                    }
                    if (i == j)
                        prevStage[i][j] += "eps";
                    else {
                        prevStage[i][j] = prevStage[i][j].substring(0, prevStage[i][j].length() - 1);
                    }
                }
            }
        }

        for (int i = 0; i < fsa.size; i++) {
            for (int j = 0; j < fsa.size; j++) {
                prevStage[i][j] = "(" + prevStage[i][j] + ")";
            }
        }

        for (int k = 0; k < fsa.size; k++) {
            String[][] curStage = new String[fsa.size][fsa.size];
            for (int i = 0; i < fsa.size; i++) {
                for (int j = 0; j < fsa.size; j++) {
                    curStage[i][j] = "(" + prevStage[i][k] + prevStage[k][k] + "*"
                            + prevStage[k][j] + "|" + prevStage[i][j] + ")";
                }
            }
            prevStage = curStage;
        }

        String regExp = "";
        int counter = 0;
        for (int i = 0; i < fsa.size; i++) {
            if (fsa.finiteStates.contains(i)) {
                if (counter != 0)
                    regExp += "|";
                regExp += prevStage[fsa.initState][i];
                counter++;
            }
        }
        if (counter == 0)
            regExp = "{}";
        return regExp;
    }

    private Fsa fsaInput(Scanner scanner) throws RuntimeException, IOException {
        Fsa fsa = new Fsa();

        String line0 = scanner.nextLine();

        if (line0.split("=").length != 2 || !line0.split("=")[0].equals("type")) {
            throw new RuntimeException("E1: Input file is malformed");
        }


        String str = line0.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (!str.equals("deterministic") && !str.equals("non-deterministic"))
            throw new RuntimeException("E1: Input file is malformed");
        fsa.type = str;

        String line1 = scanner.nextLine();
        if (line1.split("=").length != 2 || !line1.split("=")[0].equals("states")) {
            throw new RuntimeException("E1: Input file is malformed");
        }
        str = line1.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (str.length() != 0 && str.charAt(str.length() - 1) == ',') {
            throw new RuntimeException("E1: Input file is malformed");
        }
        int i = 0;
        for (String s : str.split(",")) {
            if (!s.equals("")) {
                if (!fsa.states.containsKey(s))
                    fsa.states.put(s, i++);
            }
            else
                throw new RuntimeException("E1: Input file is malformed");
        }
        fsa.size = fsa.states.size();

        String line2 = scanner.nextLine();
        if (line2.split("=").length != 2 || !line2.split("=")[0].equals("alphabet")) {
            throw new RuntimeException("E1: Input file is malformed");
        }
        str = line2.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (str.length() != 0 && str.charAt(str.length() - 1) == ',') {
            throw new RuntimeException("E1: Input file is malformed");
        }
        for (String s : str.split(",")) {
            if (!s.equals(""))
                fsa.alphabet.add(s);
            else
                throw new RuntimeException("E1: Input file is malformed");
        }

        String line3 = scanner.nextLine();
        if (line3.split("=").length != 2 || !line3.split("=")[0].equals("initial")) {
            throw new RuntimeException("E1: Input file is malformed");
        }
        str = line3.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (str.equals(""))
            throw new RuntimeException("E2: Initial state is not defined");
        if (!str.equals("") && !fsa.states.containsKey(str))
            throw new RuntimeException("E4: A state '" + str + "' is not in the set of states");
        fsa.initState = fsa.states.get(str);

        String line4 = scanner.nextLine();
        if (line4.split("=").length != 2 || !line4.split("=")[0].equals("accepting")) {
            throw new RuntimeException("E1: Input file is malformed");
        }
        str = line4.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (str.length() == 0) {
            throw new RuntimeException("E3: Set of accepting states is empty");
        }
        if (str.length() != 0 && str.charAt(str.length() - 1) == ',') {
            throw new RuntimeException("E1: Input file is malformed");
        }
        String[] ss = str.split(",");
        if (ss.length == 1 && ss[0].equals("")) {
            throw new RuntimeException("E3: Set of accepting states is empty");
        }

        for (String s : ss) {
            if (!s.equals("") && !fsa.states.containsKey(s))
                throw new RuntimeException("E4: A state '" + s + "' is not in the set of states");
            if (!s.equals("")) {
                if (!fsa.finiteStates.contains(fsa.states.get(s)))
                    fsa.finiteStates.add(fsa.states.get(s));
            }
            else
                throw new RuntimeException("E1: Input file is malformed");
        }

        fsa.outTrans = new ArrayList[fsa.size];
        fsa.trans = new ArrayList[fsa.size][fsa.size];
        for (i = 0; i < fsa.size; i++) {
            fsa.outTrans[i] = new ArrayList<>();
            for (int j = 0; j < fsa.size; j++)
                fsa.trans[i][j] = new ArrayList<>();
        }
        fsa.links = new boolean[fsa.size][fsa.size];

        String line5 = scanner.nextLine();
        if (line5.split("=").length != 2 || !line5.split("=")[0].equals("transitions")) {
            throw new RuntimeException("E1: Input file is malformed");
        }
        str = line5.split("=")[1];
        if (str.charAt(0) != '[' || str.charAt(str.length() - 1) != ']')
            throw new RuntimeException("E1: Input file is malformed");
        str = str.substring(1, str.length() - 1);
        if (str.length() != 0 && str.charAt(str.length() - 1) == ',') {
            throw new RuntimeException("E1: Input file is malformed");
        }
        ss = str.split(",");
        HashSet<String> tt = new HashSet<String>(ss.length);
        for (String s : ss) {
            if (tt.contains(s)) {
                throw new RuntimeException("E1: Input file is malformed");
            }
            tt.add(s);
            String state1 = s.split(">")[0];
            String state2 = s.split(">")[2];
            String tran = s.split(">")[1];

            if (state1.equals("") || state2.equals("") || tran.equals("")) {
                throw new RuntimeException("E1: Input file is malformed");
            }

            if (!fsa.states.containsKey(state1))
                throw new RuntimeException("E4: A state '" + state1 + "' is not in the set of states");
            else if (!fsa.states.containsKey(state2))
                throw new RuntimeException("E4: A state '" + state2 + "' is not in the set of states");
            else if (!fsa.alphabet.contains(tran))
                throw new RuntimeException("E5: A transition '" + tran + "' is not represented in the alphabet");
            else if (fsa.outTrans[fsa.states.get(state1)].contains(tran) && fsa.type.equals("deterministic"))
                throw new RuntimeException("E7: FSA is non-deterministic");
            fsa.outTrans[fsa.states.get(state1)].add(tran);
            fsa.trans[fsa.states.get(state1)][fsa.states.get(state2)].add(tran);
            fsa.links[fsa.states.get(state1)][fsa.states.get(state2)] = true;
            if (fsa.type == "deterministic")
                fsa.links[fsa.states.get(state2)][fsa.states.get(state1)] = true;
        }

        if (isDisjoint(fsa))
            throw new RuntimeException("E6: Some states are disjoint");

        return fsa;
    }

    private boolean isDisjoint(Fsa fsa) {
        ArrayDeque<Integer> next = new ArrayDeque<>();
        boolean[] visited = new boolean[fsa.states.size()];
        visited[0] = true;
        next.addLast(0);
        while (!next.isEmpty()) {
            int curState = next.removeFirst();
            for (int i = 0; i < fsa.states.size(); i++) {
                if (fsa.links[curState][i] && !visited[i]) {
                    visited[i] = true;
                    next.addLast(i);
                }
            }
        }
        for (boolean v : visited) {
            if (!v) return true;
        }
        return false;
    }

    private class Fsa {
        String type;
        HashMap<String, Integer> states;
        ArrayList<String> alphabet;
        ArrayList<Integer> finiteStates;
        Integer initState;
        ArrayList<String>[][] trans;
        boolean[][] links;
        ArrayList<String>[] outTrans;
        Integer size;

        public Fsa() {
            states = new HashMap<>();
            alphabet = new ArrayList<>();
            finiteStates = new ArrayList<>();
            size = 0;
        }
    }
}
