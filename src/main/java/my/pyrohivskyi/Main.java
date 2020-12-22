package my.pyrohivskyi;

//import com.google.protobuf.CodedInputStream;
//import com.google.protobuf.CodedOutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import my.pyrohivskyi.osmand.Region;

public class Main {
    public static void main(String[] ars) {

        System.out.println("Hello from Ivan");

        XmlParser xmlParser = new XmlParser();
        List<Region> regions =  xmlParser.parseXml("regions.xml");

        /*Person person = Person.newBuilder()
                .setId("1")
                .setName("Ivan")
                .setAge("35")
                .build();

        Person person2 = Person.newBuilder()
                .setId("2")
                .setName("Sergey")
                .setAge("40")
                .build();

        //System.out.println("Hello from " + person.getName());*/

        byte[] header = "HERE WILL BE SOME HEADER. BLABLA".getBytes();

        try {
            FileOutputStream fos = new FileOutputStream("my_test.bbb");

            fos.write(header);

            fos.getChannel().position(1000);

            for(Region region : regions) {
                region.writeDelimitedTo(fos);
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream input = new FileInputStream("my_test.bbb")) {
            input.getChannel().position(1000);
            while (true) {
                Region r = Region.parseDelimitedFrom(input);
                if (r == null) { // parseDelimitedFrom returns null on EOF
                    break;
                }
                System.out.println("Region: " + r.toString());
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*WorkWithFile workWithFile = new WorkWithFile("my_test.txt");
        try {
            workWithFile.write("kjdkdjkdj");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
