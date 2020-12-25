package my.pyrohivskyi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import my.pyrohivskyi.osmand.Region;

public class Generate {

    private String xmlFilePath;
    private String outputFile;
    private String polygonPath;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    XmlParser xmlParser;

    public Generate(String xmlFilePath, String polygonPath, String outputFile) {
        this.xmlFilePath = xmlFilePath;
        this.outputFile = outputFile;
        this.polygonPath = polygonPath;
    }

    public boolean run() {

        File f = new File(xmlFilePath);
        if(!f.exists() || f.isDirectory()) {
            System.out.println(ANSI_RED + "PATH TO XML FILE IS WRONG" + ANSI_RESET);
            return false;
        }
        f = new File(polygonPath);
        if(!f.exists() || !f.isDirectory()) {
            System.out.println(ANSI_RED + "PATH TO POLYGON FOLDER IS WRONG" + ANSI_RESET);
            return false;
        }

        List<Region> regions = parseXml();
        if(regions.size() == 0)
            return false;
        int headerSize = calculateHeaderSize(regions);
        return wtiteToProtobufFile(regions, headerSize);
    }

    private List<Region> parseXml() {
        if(xmlParser == null)
            xmlParser = new XmlParser(polygonPath);
        return xmlParser.parseXml(xmlFilePath);
    }

    private int calculateHeaderSize(List<Region> regions) {
        int size = 0;
        for(Region region : regions) {
            size += region.getSerializedSize();
        }
        //size *= 1.1;//add empty size for \n in repeated Polygons Region.proto
        return size;
    }

    private boolean wtiteToProtobufFile(List<Region> regions, int headerSize) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);

            fos.getChannel().position(headerSize);

            String header = "";
            long prevPosition = headerSize;
            for(Region region : regions) {
                region.writeDelimitedTo(fos);
                header += region.getName() + " " + prevPosition;
                prevPosition = fos.getChannel().position();
                if(region.getPolygonsList().size() > 0) {
                    List<Double> rect = xmlParser.getMinMaxRect(region);
                    if(rect.size() == 4)
                        header += " " + rect.get(XmlParser.MIN_LAT) + " "
                                + rect.get(XmlParser.MIN_LNG) + " "
                                + rect.get(XmlParser.MAX_LAT) + " "
                                + rect.get(XmlParser.MAX_LNG);
                }
                header += "\n";
            }

            header += "\n" + "\n" + "\n";
            fos.getChannel().position(0);
            fos.write(header.getBytes());
            fos.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
