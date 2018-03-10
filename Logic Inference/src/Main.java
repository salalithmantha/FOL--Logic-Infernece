import java.io.*;
import java.util.*;

class Fol {

    int n;
    int m;
    ArrayList<Sentence> query; //input query
    ArrayList<Sentence> sent;  //input KB

    Fol() {
        input();
    }

    /**
     * reads input from the file
     */
    void input() {
        try {

            File f = new File("input.txt");
            FileInputStream fin = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));

            n = Integer.parseInt(br.readLine());
            System.out.println(n);

            query = new ArrayList<>();
            sent = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                Sentence q = new Sentence();
                q.setSentence(br.readLine());
                query.add(q);
            }
            m = Integer.parseInt(br.readLine());
            System.out.println(m);
            for (int i = 0; i < m; i++) {
                Sentence s = new Sentence();
                s.setSentence(br.readLine());
                sent.add(s);
            }

            fin.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}

/**
 * Stores Sentences in the KnowledgeBase
 */
class Sentence {

    static int k = 0;
    ArrayList<String> sentence = new ArrayList<>();
    HashMap<String, String> senVar = new HashMap();
    ArrayList<String> pos = new ArrayList<>();
    ArrayList<String> neg = new ArrayList<>();
    HashMap<String, String> predicate = new HashMap<>();
    HashMap<String, String> var = new HashMap<>();
    HashMap<String, String> cons = new HashMap<>();


    public ArrayList<String> getSentence() {
        return sentence;
    }

    public String standard(String s) {
        String s1 = "" + s;
        String set[] = s1.split("[\\\\(\\\\)]");
        String str[] = set[1].split(",");
        String g = "";
        for (int i = 0; i < str.length; i++) {
            if (str[i].charAt(0) >= 97 && str[i].charAt(0) <= 122) {
                str[i] = str[i] + k;
            }
            if (i != str.length - 1)
                g += str[i] + ",";
            else
                g += str[i];
        }
        return set[0] + "(" + g + ")";
    }


    public void setSentence(String s) {

        k++;
        String se[] = s.split("\\|");
        int i = 0;
        while (i < se.length) {
            if (se[i] != "") {

                if (k <= 1000) {

                    se[i] = standard(se[i]);
                }
                sentence.add(se[i].trim());
            }
            se[i] = se[i].trim();
            if (se[i] != "")
                if (se[i].charAt(0) == '~') {
                    int len = se[i].length();
                    neg.add(se[i].trim());
                } else {

                    pos.add(se[i].trim());
                }

            i++;
        }

        for (i = 0; i < sentence.size(); i++) {

            String pre = sentence.get(i);
            String paren[] = pre.split("[\\\\(\\\\)]");


            senVar.put(pre, paren[1].replaceAll("\\s+", ""));

            if (paren[1].charAt(0) >= 65 && paren[1].charAt(0) <= 90) {
                cons.put(pre, paren[1]);
            } else {
                var.put(pre, paren[1].replaceAll("\\s+", ""));
            }

            predicate.put(pre, paren[0]);


        }


    }


    public int senSize() {
        return sentence.size();
    }


}

/**
 * performs  FOL Resolution
 */
class Resolution {
    HashMap<String, Integer> hmap = new HashMap<>();
    ArrayList<String> asr = new ArrayList<>();
    Stack<Sentence> qrtm = new Stack<>();
    HashMap<String, String> var = new HashMap<>();

    Resolution(ArrayList<Sentence> sent, int m, ArrayList<Sentence> q, int n) {


        for (int i = 0; i < n; i++) {
            long time = System.currentTimeMillis();
            Sentence s = q.get(i);
            String ss = s.sentence.get(0);
            Sentence sq = new Sentence();
            if (ss.charAt(0) == '~') {
                int len = ss.length();
                ss = ss.substring(1, len);
            } else {
                ss = "~" + ss;
            }
            hmap.clear();
            sq.setSentence(ss);
            if (sent.size() == m) {
                sent.add(sq);
            }
            if (sent.size() == (m + 1)) {
                sent.remove(sent.size() - 1);
                sent.add(sq);
            }
            if (asr.size() == (i - 1)) {
                asr.add("FALSE");
            }
            qrtm.add(sq);


            while (!qrtm.empty()) {
                if ((System.currentTimeMillis() - time) / 1000 > 30) {
                    asr.add("FALSE");
                    break;
                }

                int t = call(sent, m, qrtm.get(0), n);

                if (t == 1) {
                    asr.add("TRUE");
                    break;
                }
                qrtm.remove(0);


            }

            if (qrtm.empty()) {
                asr.add("FALSE");
            }
            qrtm.clear();


        }

        System.out.println(asr);
    }


    static boolean var(String x) {
        if (x.charAt(0) >= 97 && x.charAt(0) <= 122) {
            return true;
        } else {
            return false;
        }
    }


    static Map<String, String> unify(Object x, Object y, Map<String, String> sub) {
        if (sub == null) {
            return null;
        } else if (isString(x) && isString(y)) {
            String xStr = (String) x;


            String yStr = (String) y;
            if (xStr.equals(yStr)) {
                return sub;

            } else if (var(xStr)) {
                return unifyVar(xStr, yStr, sub);


            } else if (var(yStr)) {
                return unifyVar(yStr, xStr, sub);
            } else {

                return null;
            }
        } else {
            List<String> xArg = (List) x;
            List<String> yArg = (List) y;
            if (xArg.size() == 1) {


                return unify(xArg.get(0), yArg.get(0), sub);


            }


            return unify(xArg.subList(1, xArg.size()), yArg.subList(1, yArg.size()), unify(xArg.get(0), yArg.get(0), sub));
        }
    }

    static Map<String, String> unifyVar(String var, String x, Map<String, String> sub) {
        if (sub.containsKey(var)) {

            return unify(sub.get(var), x, sub);
        } else if (sub.containsKey(x)) {

            return unify(var, sub.get(x), sub);
        } else {

            sub.put(var, x);
            return sub;
        }
    }

    static boolean isString(Object a) {
        return a instanceof String;
    }


    String sub(String z, Map<String, String> var1) {

        if (!var1.containsKey(z)) {
            return z;
        } else {
            return sub(var1.get(z), var1);
        }

    }


    int call(ArrayList<Sentence> sent1, int m, Sentence q, int n) {
        ArrayList<String> q1 = new ArrayList<>();


        String hmapkey = "";
        Sentence s = q;
        for (String e : s.sentence) {
            hmapkey += e;
        }

        hmap.put(hmapkey, 1);

        Sentence sq;
        if (n == 0) {
            System.out.println("TRUE");

            return 1;
        } else {

        }


        while (true) {
            ArrayList<String> sqt = s.getSentence();
            q1 = new ArrayList<>(sqt);

            for (int i = 0; i < s.senSize(); i++) {       // one query sennteces

                String s1 = q1.get(i);

                ArrayList<Sentence> sent = new ArrayList<>(sent1);

                for (int j = 0; j < sent.size(); j++) {    //one sentecnce in KB
                    Sentence sen1 = sent.get(j);


                    for (int k = 0; k < sen1.senSize(); k++) {//    parts of sentences
                        int set = 1;
                        String s2;
                        if (abs(s1)) {
                            s2 = "~" + s1;
                        } else {
                            int len = s1.length();
                            s2 = s1.substring(1, len);
                        }

                        String s5 = s2.split("[\\\\(\\\\)]")[0];
                        String s4 = sen1.sentence.get(k);
                        if (s5.equals(sen1.predicate.get(s4))) {
                            String s3 = sen1.sentence.get(k);

                            String vars3[] = sen1.senVar.get(s3).split(",");
                            String vars2[] = s.senVar.get(s1).split(",");
                            ArrayList<String> s33 = new ArrayList<String>();
                            ArrayList<String> s22 = new ArrayList<String>();
                            for (String s77 : vars3) {
                                s33.add(s77);
                            }
                            for (String s77 : vars2) {
                                s22.add(s77);
                            }
                            var.clear();


                            Map<String, String> map = unify(s33, s22, new HashMap<>());


                            if (map == null) {
                                continue;
                            }
                            ArrayList<String> remSens = sen1.getSentence();
                            ArrayList<String> remSen = new ArrayList<>(remSens);
                            remSen.remove(k);
                            String remsen1 = "";
                            for (int a = 0; a < remSen.size(); a++) {
                                String x = remSen.get(a);
                                String senter = sen1.predicate.get(x) + "(";
                                String vars4[] = sen1.senVar.get(x).split(",");
                                for (int b = 0; b < vars4.length; b++) {

                                    if (!map.containsKey(vars4[b])) {
                                        if (b != vars4.length - 1)
                                            senter += vars4[b] + ",";
                                        else
                                            senter += vars4[b] + ")|";
                                    } else {
                                        if (b != vars4.length - 1)
                                            senter += sub(vars4[b], map) + ",";
                                        else
                                            senter += sub(vars4[b], map) + ")|";

                                    }


                                }
                                remsen1 += senter;
                            }

                            ArrayList<String> qq = s.getSentence();
                            ArrayList<String> q2 = new ArrayList<>(qq);
                            q2.remove(s1);
                            if (!q2.isEmpty())
                                for (int a = 0; a < q2.size(); a++) {
                                    String x = q2.get(a);
                                    String senter1 = s.predicate.get(x) + "(";
                                    String vars5[] = s.senVar.get(x).split(",");
                                    for (int b = 0; b < vars5.length; b++) {

                                        if (!map.containsKey(vars5[b])) {
                                            if (b != vars5.length - 1)
                                                senter1 += vars5[b] + ",";
                                            else
                                                senter1 += vars5[b] + ")|";
                                        } else {
                                            if (b != vars5.length - 1)
                                                senter1 += sub(vars5[b], map) + ",";
                                            else
                                                senter1 += sub(vars5[b], map) + ")|";

                                        }


                                    }
                                    remsen1 += senter1;
                                }
                            if (remsen1.equals("")) {
                                return 1;
                            }


                            Sentence sss = new Sentence();
                            Sentence sss1 = new Sentence();
                            sss.setSentence(remsen1);

                            var.clear();


                            Set<String> s1e = new HashSet<>(sss.sentence);


                            String sfm1 = "";
                            for (String sfm : s1e) {
                                sfm1 += sfm + "|";
                            }

                            sss1.setSentence(sfm1);
                            if (s1e.size() != sss.sentence.size()) {
                                return 4;

                            }

                            String hmapkey1 = "";
                            for (String e : sss.sentence) {
                                hmapkey1 += e;
                            }

                            if (!hmap.containsKey(hmapkey1)) {
                                qrtm.add(sss);


                            } else {

                                continue;
                            }


                        }

                    }

                }

            }
            break;
        }

        return 0;
    }


    boolean abs(String s) {
        if (s.charAt(0) == '~') {
            return false;
        } else
            return true;
    }

}


public class Main {
    public static void main(String[] args) {
        Fol f1 = new Fol();
        Resolution r = new Resolution(f1.sent, f1.m, f1.query, f1.n);
        int i = 0;
        while (i < f1.sent.size()) {
            Sentence s = f1.sent.get(i);
            try {
                PrintWriter pw = new PrintWriter("output.txt", "UTF-8");
                for (String s12 : r.asr) {
                    pw.println(s12);
                }

                pw.close();
            } catch (Exception e) {

            }


            i++;
        }
    }
}


