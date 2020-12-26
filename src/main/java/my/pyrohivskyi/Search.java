package my.pyrohivskyi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Path2D;

import my.pyrohivskyi.osmand.Region;

import static my.pyrohivskyi.Generate.ANSI_RED;
import static my.pyrohivskyi.Generate.ANSI_RESET;

public class Search {

    String pbfFile;
    List<IndexStructure> indexStructureList;

    public Search(String pbfFile) {
        this.pbfFile = pbfFile;
        indexStructureList = new ArrayList<>();
        loadHeader();
    }

    public Region findRegionByName(String countryName) {

        if(indexStructureList.size() == 0) {
            System.out.println(ANSI_RED + "SEARCH INDEX WASN'T LOADED" + ANSI_RESET);
            return Region.newBuilder().build();
        }

        for(IndexStructure index : indexStructureList) {
            if(index.name.equals(countryName)) {
                try (FileInputStream input = new FileInputStream(pbfFile)) {
                    input.getChannel().position(index.bytes);
                    Region r = Region.parseDelimitedFrom(input);
                    System.out.println("Read " + r.getSerializedSize() + " bytes of country data");
                    if(index.withoutPoly)
                        System.out.println(ANSI_RED + "COUNTRY WITHOUT POLYLINE" + ANSI_RESET);
                    input.close();
                    return r;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(ANSI_RED + "COUNTRY NOT FOUND" + ANSI_RESET);
        return Region.newBuilder().build();
    }

    public List<Region> findRegionByPoint(double lat, double lng) {

        List<Region> result = new ArrayList<>();
        List<IndexStructure> candidates = new ArrayList<>();

        for(IndexStructure index : indexStructureList) {
            if(lat >= index.minLat && lat <= index.maxLat
                && lng >= index.minLng && lng <= index.maxLng )
                candidates.add(index);
        }

        if(candidates.size() == 0) {
            System.out.println(ANSI_RED + "COUNTRY NOT FOUND" + ANSI_RESET);
            return result;
        }

        int read = 0;
        for(IndexStructure index : candidates) {
            try (FileInputStream input = new FileInputStream(pbfFile)) {
                input.getChannel().position(index.bytes);
                Region r = Region.parseDelimitedFrom(input);
                read += r.getSerializedSize();

                if(containsInRegionPolygon(r, lat, lng)) {
                    result.add(r);
                }
                input.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Read " + read + " bytes of country data");
        if(result.size() == 0)
            System.out.println(ANSI_RED + "COUNTRY NOT FOUND" + ANSI_RESET);
        return result;
    }

    private void loadHeader() {

        try {
            FileInputStream input = new FileInputStream(pbfFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(input));

            String line = null;


            long startingPoint = 0;
            while ((line = br.readLine()) != null) {
                startingPoint += line.getBytes().length;

                if(line.getBytes().length == 0) {
                    System.out.println("Read " + startingPoint + " bytes of header");
                    break;
                }

                String[] arguments= line.split(" ");
                if(arguments.length < 2)
                    continue;
                indexStructureList.add(new IndexStructure(arguments));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    class IndexStructure {
        String name;
        long bytes;
        double minLat, minLng, maxLat, maxLng;
        boolean withoutPoly;

        IndexStructure(String[] arguments) {
            if(arguments.length >= 2) {
                name = arguments[0];
                bytes = Long.parseLong(arguments[1]);

                if(arguments.length == 6) {
                    minLat = Double.parseDouble(arguments[2]);
                    minLng = Double.parseDouble(arguments[3]);
                    maxLat = Double.parseDouble(arguments[4]);
                    maxLng = Double.parseDouble(arguments[5]);
                    withoutPoly = false;
                } else {
                    withoutPoly = true;
                }
            }
        }
    }

    private boolean containsInRegionPolygon(Region region, double lat, double lng) {

        List<Region.Polygons> regionPoly = region.getPolygonsList();

        if(regionPoly.size() == 0)
            return false;

        for(Region.Polygons poly : regionPoly) {
            Path2D path = new Path2D.Double();
            path.moveTo(poly.getPoly(0), poly.getPoly(1));
            for(int i = 2; i < poly.getPolyCount(); i = i+2) {
                path.lineTo(poly.getPoly(i), poly.getPoly(i+1));
            }
            path.closePath();
            if(path.contains(lng, lat))// lng--lat
                return true;
        }
        return false;
    }
}
