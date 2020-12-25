package my.pyrohivskyi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.pyrohivskyi.osmand.Region;


public class PolyFinder {

    String pathToRootDir;
    List<File> polygonFiles;

    PolyFinder(String pathToRootDir) {
        this.pathToRootDir = pathToRootDir;
        polygonFiles = new ArrayList<File>();

        File dir = new File(pathToRootDir);
        if(dir.exists() && dir.isDirectory())
            listOfFiles(dir);

        /*for(File file : polygonFiles) {
            System.out.println("---------------" + file.getPath());
        }*/

    }

    void fillPoly(Region.Builder regionBuilder, XmlParser.RegionXmlStruct regionXmlStruct) {

        List<List<Double>> polygons = getPolyFromFile(regionXmlStruct.poly_name);


        if(polygons.size() == 0)
            return;

        int i = 0;
        for(List<Double> polygon : polygons) {
            Region.Polygons.Builder pb = Region.Polygons.newBuilder();
            pb.addAllPoly(polygon);
            regionBuilder.addPolygons(pb.build());
            i++;
        }
    }

    private List<List<Double>> getPolyFromFile(String filePath) {
        List<List<Double>> list = new ArrayList<>();

        String pathToFile = getPathToFile(filePath);
        if(pathToFile.isEmpty())
            return list;

        try {
            FileInputStream input = new FileInputStream(pathToFile);

            BufferedReader br = new BufferedReader(new InputStreamReader(input));

            String line = null;
            List<Double> polygon = new ArrayList<Double>();
            while ((line = br.readLine()) != null) {
                Pattern p = Pattern.compile("(-|\\+)?\\d+\\.[^ .]+\\s+(-|\\+)?\\d+\\.[^ .]+");
                Matcher matcher = p.matcher(line);
                if(matcher.find()) {
                    p = Pattern.compile("(-|\\+)?\\d+\\.[^ .]+");
                    MatchResult matchResult = matcher.toMatchResult();

                    Matcher m = Pattern.compile("(-|\\+)?\\d+\\.[^ .\\t\\r]+").matcher(line);
                    while (m.find()) {
                        //System.out.println(line);
                        polygon.add(Double.parseDouble(m.group()));
                    }
                } else {
                    if(polygon.size() > 2)
                        list.add(polygon);
                    polygon = new ArrayList<Double>();
                    //System.out.println("---------------" + line);
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;

    }

    private String getPathToFile(String fileName) {
        for(File polygon : polygonFiles) {
            if(polygon.getName().equals(fileName))
                return polygon.getPath();
        }
        return "";
    }

    private void listOfFiles(File dirPath){
        File filesList[] = dirPath.listFiles();
        for(File file : filesList) {
            if(file.isFile()) {
                polygonFiles.add(file);
            } else
                listOfFiles(file);
        }
    }
}
