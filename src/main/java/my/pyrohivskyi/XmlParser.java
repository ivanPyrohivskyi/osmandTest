package my.pyrohivskyi;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import my.pyrohivskyi.osmand.Region;

public class XmlParser {

    List<RegionXmlStruct> list;
    final static short MIN_LAT = 0;
    final static short MIN_LNG = 1;
    final static short MAX_LAT = 2;
    final static short MAX_LNG = 3;
    String pathToPolyhons;

    XmlParser(String pathToPolygons) {
        this.pathToPolyhons = pathToPolygons;
        list = new ArrayList<RegionXmlStruct>();
    }

    public List<Region> parseXml(String pathToXmlFile) {

        List<Region> regions = new ArrayList<>();

        PolyFinder polyFinder = new PolyFinder(pathToPolyhons);

        try {
            File inputFile = new File(pathToXmlFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            parseByTagName(doc);

            /*Element eElement = doc.getDocumentElement();
            parseChildNodes(eElement, "");*/

            for(RegionXmlStruct s : list) {
                //System.out.println(s.name + " " + s.poly_extract);
                Region.Builder regionBuilder = Region.newBuilder();
                regionBuilder.setName(s.name);
                if(s.type != null)
                    regionBuilder.setType(s.type);
                if(s.lang != null)
                    regionBuilder.setLang(s.lang);
                if(s.srtm != null)
                    regionBuilder.setSrtm(s.srtm);
                if(s.roads != null)
                    regionBuilder.setRoads(s.roads);
                if(s.hillshade != null)
                    regionBuilder.setHillshade(s.hillshade);
                if(s.wiki != null)
                    regionBuilder.setWiki(s.wiki);
                if(s.translate != null)
                    regionBuilder.setTranslate(s.translate);

                polyFinder.fillPoly(regionBuilder, s);

                regions.add(regionBuilder.build());
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return regions;
    }

    public List<Double> getMinMaxRect(Region region) {

        List<Double> result = new ArrayList<Double>();

        List<Region.Polygons> polygonsList = region.getPolygonsList();
        Region.Polygons poly;
        if(polygonsList.size() > 0) {
            poly = polygonsList.get(0);
            List<Double> polyList = poly.getPolyList();
            double minLat = 90, maxLat=-90, minLng=180, maxLng=-180;
            for(int i = 0; i < polyList.size(); i++) {
                if(i %2 == 0) {
                    //lng
                    minLng = minLng > polyList.get(i) ? polyList.get(i) : minLng;//
                    maxLng = maxLng < polyList.get(i) ? polyList.get(i) : maxLng;
                } else {
                    //lat
                    minLat = minLat > polyList.get(i) ? polyList.get(i) : minLat;//
                    maxLat = maxLat < polyList.get(i) ? polyList.get(i) : maxLat;//
                }
            }
            result.add(minLat);
            result.add(minLng);
            result.add(maxLat);
            result.add(maxLng);
        }
        return result;
    }

    private void parseByTagName(Document doc) {
        if(list == null)
            list = new ArrayList<RegionXmlStruct>();

        NodeList nList = doc.getElementsByTagName("region");
        for(int i = 0;  i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if(eElement.hasAttribute("name")) {
                    String name = eElement.getAttribute("name");
                    RegionXmlStruct s = new RegionXmlStruct(name);
                    s.addOptional(eElement);
                    list.add(s);
                }
            }
        }
    }

    /*private List<RegionXmlStruct> parseChildNodes(Element eElement, String parentPolyEctract) {

        if(list == null)
            list = new ArrayList<RegionXmlStruct>();

        NodeList nodes = eElement.getChildNodes();
        for(int levelOne = 0;  levelOne < nodes.getLength(); levelOne++) {
            Node levelOneNode = nodes.item(levelOne);
            if(levelOneNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) levelOneNode;
                boolean continent = currentElement.hasAttribute("type")
                        && currentElement.getAttribute("type").equals("continent");
                //System.out.println("\nLevel One Element :" + currentElement.getAttribute("name"));
                String name = currentElement.getAttribute("name");
                String poly = parentPolyEctract;

                if(currentElement.hasAttribute("poly_extract")) {
                    if(continent)
                        poly = poly + "/" + name;
                    else
                        poly = poly + "/" + currentElement.getAttribute("poly_extract") + "/" + name;
                } else {
                    poly = poly + "/" + name;
                }
                RegionXmlStruct s = new RegionXmlStruct(name, poly + ".poly");
                s.addOptional(currentElement);
                s.poly_name = name + ".poly";
                list.add(s);

                if(levelOneNode.hasChildNodes()) {
                    String parent = parentPolyEctract;
                    if(currentElement.hasAttribute("poly_extract") && !continent)
                        parent += "/" + currentElement.getAttribute("poly_extract");
                    parent += "/" + name;
                    parseChildNodes(currentElement, parent);
                }
            }
        }
        return list;
    }*/

    public class RegionXmlStruct {

        String name, poly_name;

        @Nullable
        String lang, type, roads, srtm, hillshade, wiki, translate;

        RegionXmlStruct(String name) {
            this.name = name;
            this.poly_name = name + ".poly";
        }

        public void addOptional(Element element) {
            lang = getAttr(element, "lang");
            type = getAttr(element, "type");
            roads = getAttr(element, "roads");
            srtm = getAttr(element, "srtm");
            hillshade = getAttr(element, "hillshade");
            wiki = getAttr(element, "wiki");
            translate = getAttr(element, "translate");
        }

        @Nullable
        private String getAttr(Element element, String name) {
            if(element.hasAttribute(name))
                return element.getAttribute(name);
            else
                return null;
        }


    }
}
