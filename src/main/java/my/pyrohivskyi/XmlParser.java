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

    List<Struct> list;

    public List<Region> parseXml(String pathToXmlFile) {

        List<Region> regions = new ArrayList<>();

        try {
            File inputFile = new File(pathToXmlFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("region");

            Element eElement = doc.getDocumentElement();

            parseChildNodes(eElement, "");

            for(Struct s : list) {
                System.out.println(s.name + " " + s.poly_extract);
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

    private List<Struct> parseChildNodes(Element eElement, String parentPolyEctract) {

        if(list == null)
            list = new ArrayList<Struct>();

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
                Struct s = new Struct(name, poly + ".poly");
                s.addOptional(currentElement);
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
    }

    private class Struct {

        Struct(String name, String poly_extract) {
            this.poly_extract = poly_extract;
            this.name = name;
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

        String name, poly_extract;

        @Nullable
        String lang, type, roads, srtm, hillshade, wiki, translate;
    }
}
