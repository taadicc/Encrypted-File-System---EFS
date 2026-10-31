package etf.unibl.org.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OpensslCMD {

    public static String izvrsi(String komanda) {
        String rezultat = "";
        Process proces = null;

        try {
            proces = Runtime.getRuntime().exec("cmd /c " + komanda);

            BufferedReader izlaz = new BufferedReader(
                    new InputStreamReader(proces.getInputStream()));

            String linija;
            StringBuilder rezultatBuilder = new StringBuilder();
            while ((linija = izlaz.readLine()) != null) {
                rezultatBuilder.append(linija).append("\n");
            }

            izlaz.close();
            rezultat = rezultatBuilder.toString().trim();

            //int kodIzlaza = proces.waitFor();
            //if (kodIzlaza != 0) {
            //    System.err.println("Greška prilikom izvršavanja komande: " + komanda);
            //}

        } catch (Exception e) {
            System.err.println("Izuzetak pri izvršavanju komande: " + e.getMessage());
        }

        return rezultat;
    }
}
