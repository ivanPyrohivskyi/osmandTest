package my.pyrohivskyi;
import java.util.List;

import my.pyrohivskyi.osmand.Region;

import static my.pyrohivskyi.Generate.ANSI_GREEN;
import static my.pyrohivskyi.Generate.ANSI_RED;
import static my.pyrohivskyi.Generate.ANSI_RESET;

public class Main {

    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] ars) {

        System.out.println("Hello from Ivan");

        Generate generate = new Generate("regions.xml", "polygons", "outfile.pbf");
        if(generate.run()) {
            System.out.println(ANSI_GREEN + "GENERATION SUCCESSFUL" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "GENERATION FAILED" + ANSI_RESET);
            return;
        }

        System.out.println();
        System.out.println(ANSI_YELLOW + "SEARCH INITIALIZATION" + ANSI_RESET);
        Search search = new Search("outfile.pbf");

        System.out.println();
        System.out.println(ANSI_YELLOW + "SEARCH BY NAME" + ANSI_RESET);
        Region r = search.findRegionByName("denmark");
        System.out.println("Region: " + r.toString());

        System.out.println(ANSI_YELLOW + "SEARCH BY COORDINATES" + ANSI_RESET);
        List<Region> listRegions = search.findRegionByPoint(50, 30);
        for(Region region : listRegions) {
            System.out.println("Found: " + region.getName());
        }
    }
}
