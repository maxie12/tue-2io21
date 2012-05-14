import java.io.*;
import java.util.*;

import model.*;
import algorithm.*;

class Main {
    Scanner sc;
    Algorithm algo;

    public Main()
    {
        sc = new Scanner(System.in);

        algo = new Optics();
    }

    /**
     * Read the points
     *
     * @param n Number of points to read
     */
    Field read(int n)
    {
        Field f = new Field();

        for (int i = 0; i < n; i++) {
            f.addPoint(new Point(sc.nextInt(), sc.nextInt(), 0));
        }

        return f;
    }

    void run()
    {
        // read 'find $C_i$ to $C_j$ clusters'
        // read '$n$ points'
        // read '$x$ $y$' for each point
        sc.next(); // find
        int ci = sc.nextInt();
        int cj = ci;
        if (sc.next().equals("to")) { // otherwise, it is clusters
            cj = sc.nextInt();
            sc.next(); // clusters
        }

        int n = sc.nextInt();

        sc.next(); // points

        algo.setField(read(n));

        algo.findParameters(ci, cj, n);

        algo.run();
    }

    public static void main(String args[])
    {
        new Main().run();
    }
}
