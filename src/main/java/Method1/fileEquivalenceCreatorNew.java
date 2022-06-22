package Method1;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class fileEquivalenceCreatorNew {

    private static List<String> equivalencias;

    private static boolean usePrefix = false;

    private static String directorioAlineameintos = "src/files/alineamientos/alineamientos5";
    private static String directorioVerbalizaciones = "src/files/verbalizaciones/formal";
    private static String ficheroTest = "sigkdd";
    private static boolean test = false;

    private static String splitWord(String word){
        String listWords = "";
        String aux = "";
        boolean mayusculas = false;
        word = word.replace("_", " ");

        for (String simpleWord : word.split(" ") ){
            for ( char letra : simpleWord.toCharArray() ){
                if ( Character.isLowerCase(letra) || Character.isDigit(letra) ){
                    if( aux.length() >= 2 && mayusculas) {
                        listWords = listWords + " " + aux;
                        aux = "";
                    }
                    aux = aux + letra;
                    mayusculas = false;
                }
                else if( Character.isUpperCase(letra) && !mayusculas){
                    if ( !aux.equals("") ){
                        listWords = listWords + " " + aux;
                        aux = "" + letra;
                    }
                    else {
                        aux = aux + letra;

                    }
                    mayusculas = true;
                }
                else if(Character.isUpperCase(letra) && mayusculas){
                    aux = aux + letra;
                }
            }
            listWords = listWords + " " + aux;
            aux = "";
            mayusculas = false;
        }

        return listWords.replaceAll(" $","").replaceAll("^ ","").toLowerCase(Locale.ROOT);
    }


    private static void rellenarEquivalencias(String file, FileWriter myWriter){
        //Leemos el fichero XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(file));

            // optional, but recommended
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("Cell");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                Element element = (Element) node;

                String entity1 = "";
                String entity2 = "";
                String rel = "";

                NodeList listChilds = element.getChildNodes();
                for (int child = 0; child < listChilds.getLength(); child++) {
                    Node childNode = listChilds.item(child);

                    if(childNode.getNodeName() == "entity1"){
                        //entity1 =  childNode.getAttributes().item(0).getTextContent().split("#")[1];
                        entity1 =  childNode.getAttributes().item(0).getTextContent().split("//")[1];
                        if( !usePrefix ) entity1 = entity1.split("#")[1];
                    }

                    if(childNode.getNodeName() == "entity2"){
                        //entity2 =  childNode.getAttributes().item(0).getTextContent().split("#")[1];
                        entity2 =  childNode.getAttributes().item(0).getTextContent().split("//")[1];
                        if( !usePrefix ) entity2 = entity2.split("#")[1];
                    }

                    if(childNode.getNodeName() == "measure"){
                        rel =  childNode.getTextContent();
                    }
                }

                if( !splitWord(entity2).equals(splitWord(entity1)) && !equivalencias.contains(entity1 + "," + entity2) ){
                    equivalencias.add(entity1 + "," + entity2);
                    equivalencias.add(entity2 + "," + entity1);
                    myWriter.write(splitWord(entity1)+ "," + splitWord(entity2) + ",1" + "\n");
                    anyadirNoEquivalente(myWriter);
                }
            }
        }  catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static void anyadirNoEquivalente(FileWriter myWriter) throws IOException {
        Random rand = new Random();
        File folder = new File(directorioVerbalizaciones);
        File[] listOfFiles = folder.listFiles();
        int numVerbalizaciones = listOfFiles.length;
        boolean added = false;

        while(!added){
            int randomVerb1 = rand.nextInt(numVerbalizaciones );
            while( !listOfFiles[randomVerb1].isFile() || ( test && !listOfFiles[randomVerb1].getName().contains(ficheroTest) ) ||
                    ( !test && listOfFiles[randomVerb1].getName().contains(ficheroTest) ) ) randomVerb1 = rand.nextInt(numVerbalizaciones );
            int randomVerb2 = rand.nextInt(numVerbalizaciones );
            while( !listOfFiles[randomVerb2].isFile() || randomVerb1 == randomVerb2  ||
                    ( !test && listOfFiles[randomVerb2].getName().contains(ficheroTest) )) randomVerb2 = rand.nextInt(numVerbalizaciones );

            //Obtenemos el elemento 1
            String path1 = folder.getPath() + "/" + listOfFiles[randomVerb1].getName();
            String prefix1 = "";
            if ( usePrefix ) prefix1 = listOfFiles[randomVerb1].getName().split("[.]")[0].replace("Verbalized","") + "#";
            List<String> lines1 = Files.readAllLines(Paths.get(path1));
            int randomRow = rand.nextInt(lines1.size() );
            while ( lines1.get(randomRow).split("'").length < 2 ) randomRow = rand.nextInt(lines1.size() );
            String element1 = prefix1 + lines1.get(randomRow).split("'")[1];


            //Obtenemos el elemento 2
            String path2 = folder.getPath() + "/" + listOfFiles[randomVerb2].getName();
            String prefix2 = "";
            if ( usePrefix ) prefix2 = listOfFiles[randomVerb2].getName().split("[.]")[0].replace("Verbalized","") + "#";
            List<String> lines2 = Files.readAllLines(Paths.get(path2));
            randomRow = rand.nextInt(lines2.size() );
            while ( lines2.get(randomRow).split("'").length < 2 ) randomRow = rand.nextInt(lines2.size() );
            String element2 = prefix2 + lines2.get(randomRow).split("'")[1];

            while( equivalencias.contains(element1 + "," + element2) ){
                randomRow = rand.nextInt(lines1.size() );
                while ( lines1.get(randomRow).split("'").length < 2 ) randomRow = rand.nextInt(lines1.size() );
                element1 = prefix1 + lines1.get(randomRow).split("'")[1];

                randomRow = rand.nextInt(lines1.size() );
                while ( lines2.get(randomRow).split("'").length < 2 ) randomRow = rand.nextInt(lines2.size() );
                element2 = prefix2 + lines2.get(randomRow).split("'")[1];
            }

            if ( !splitWord(element1).equals(splitWord(element2)) && !element1.equals(" . ") && !element2.equals(" . ")){
                equivalencias.add(element1 + "," + element2);
                equivalencias.add(element2 + "," + element1);
                myWriter.write(splitWord(element1) + "," + splitWord(element2) + ",0" + "\n");
                added = true;
            }
            else{
                added = false;
            }

        }

    }

    public static void main (String [ ] args) throws IOException {

        equivalencias = new ArrayList<>();


        FileWriter myWriterTrain = new FileWriter("train.csv");
        FileWriter myWriterVal = new FileWriter("val.csv");
        FileWriter myWriterTest = new FileWriter("test.csv");

        myWriterTrain.write("source,target,rel\n");
        myWriterVal.write("source,target,rel\n");
        myWriterTest.write("source,target,rel\n");

        //rellenamos ficheros de train y val, 70% para train y 30% para val

        System.out.println("Train_Test");
        File folder = new File(directorioAlineameintos);
        File[] listOfFiles = folder.listFiles();
        int totalFiles = listOfFiles.length;
        int filesTrain = totalFiles * 65 / 100;
        int filesVal = totalFiles - filesTrain;

        if( listOfFiles != null ){
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if( i < filesTrain){
                        System.out.println("Train: " + listOfFiles[i].getName());
                        rellenarEquivalencias( folder.getPath() + "/" + listOfFiles[i].getName() , myWriterTrain);
                    }
                    else{
                        System.out.println("Val: " + listOfFiles[i].getName());
                        rellenarEquivalencias( folder.getPath() + "/" + listOfFiles[i].getName() , myWriterVal);
                    }
                }
            }
        }
        else{
            System.out.println("Error: El directorio no existe");
        }


        //rellenamos fichero de test con los que están dentro de la carpeta test
        test = true;
        folder = new File(folder.getPath() + "/test");
        listOfFiles = folder.listFiles();
        if( listOfFiles != null ){
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("Test: " + listOfFiles[i].getName());
                    rellenarEquivalencias( folder.getPath() + "/" + listOfFiles[i].getName() , myWriterTest);
                }
            }
        }
        else{
            System.out.println("Error: El directorio no existe");
        }

        myWriterTrain.close();
        myWriterVal.close();
        myWriterTest.close();
    }
}
