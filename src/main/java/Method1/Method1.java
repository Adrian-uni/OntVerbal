package Method1;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.script.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class Method1 {


    //Modelo inferido
    static private OntModel infModel = ModelFactory.createOntologyModel();

    //vector de elementos
    static private Map<String,Elemento> elementos = new HashMap<String, Elemento>();

    static private String baseModel = "";
    static private String prefixModel = "";

    static private FileWriter myWriter;

    static private Map<String, String> template = new HashMap<String, String>();

    private static void anyadirClases() throws FileNotFoundException {

        baseModel = infModel.getNsPrefixURI("").split("#")[0];
        //prefixModel = (baseModel.split("//")[1] + "#").toLowerCase();
        prefixModel = "";
        ExtendedIterator classes = infModel.listClasses();
        while (classes.hasNext()) {

            OntClass thisClass = (OntClass) classes.next();
            if( thisClass.isURIResource() && !thisClass.getLocalName().equals("Thing") && !thisClass.getLocalName().equals("Nothing") ){


                //Añadimos el label
                Entidad entidad = new Entidad( thisClass.getURI() , prefixModel + thisClass.getLocalName() );
                if( thisClass.getLabel("") != null){
                    entidad.setName( prefixModel + thisClass.getLabel("") );
                }

                //Añadimos el comentariof
                if(thisClass.getComment("") != null){
                    entidad.addCaracteristica("Comentario","T[" + thisClass.getComment("") + "]");
                }

                entidad.addCaracteristica("DescriptionClass", "T[" + prefixModel + entidad.getName()+ "]" );

                //Añadimos individuos
                ExtendedIterator instances = thisClass.listInstances(true);

                while (instances.hasNext()) {

                    Individual thisInstance = (Individual) instances.next();
                    if ( elementos.get(thisInstance.getLocalName()) == null  ){
                        Individuo i = new Individuo( thisInstance.getURI(), prefixModel + thisInstance.getLocalName() );

                        i.addCaracteristica("ClassAssertionJ", "T[" + prefixModel + thisInstance.getOntClass(true).getLocalName() + "]");

                        if( thisInstance.getLabel("") != null){
                            entidad.setName( prefixModel + thisInstance.getLabel("") );
                        }
                        elementos.put( prefixModel + thisInstance.getLocalName(), i );
                    }

                }

                ExtendedIterator subClasses = thisClass.listSubClasses(true);
                while (subClasses.hasNext()) {
                    OntClass subClass = (OntClass) subClasses.next();
                    if( !subClass.getLocalName().equals("Nothing")){
                        if( subClass.getLabel("") != null){
                            entidad.addCaracteristica("Disjoint","T[" + prefixModel + subClass.getLabel("") + "]" );
                        }
                        else{
                            entidad.addCaracteristica("Disjoint","T[" + prefixModel + subClass.getLocalName()+ "]" );
                        }

                    }
                }

                ExtendedIterator superClasses = thisClass.listSuperClasses(true);
                while (superClasses.hasNext()) {
                    OntClass superClass = (OntClass) superClasses.next();

                    if ( superClass.getLocalName() != null && !superClass.getLocalName().equals("Nothing") && thisClass.getLocalName() != null && !superClass.getLocalName().equals("Thing") ){

                        if( superClass.getLabel("") != null){
                            entidad.addCaracteristica("DescripcionE","T[" + prefixModel + superClass.getLabel("") + "]" );
                        }
                        else{
                            entidad.addCaracteristica("DescripcionE","T[" + prefixModel + superClass.getLocalName() + "]" );
                        }

                    }
                }

                //Añadimos entidades
                elementos.put( prefixModel + thisClass.getLocalName(), entidad );

            }
        }

        ExtendedIterator properties = infModel.listOntProperties();
        while (properties.hasNext()) {
            OntProperty p = (OntProperty) properties.next();
            Propiedad property = new Propiedad(p.getURI(), prefixModel + p.getLocalName());
            if( p.getURI().contains(baseModel)){
                if( p.getDomain() != null){
                    if( p.getDomain().getLabel("") != null){
                        property.addCaracteristica("Domain", "T[" + prefixModel + p.getDomain().getLabel("") + "]" );
                    }
                    else if(p.getDomain().getLocalName() != null){
                        property.addCaracteristica("Domain","T[" + prefixModel + p.getDomain().getLocalName() + "]" );
                    }
                }
                if( p.getRange() != null){

                    if( p.getRange().getLabel("") != null){
                        property.addCaracteristica("Range","T[" + prefixModel + p.getRange().getLabel("") + "]" );
                    }
                    else if( p.getRange().getLocalName() != null ){
                        property.addCaracteristica("Range","T[" + prefixModel + p.getRange().getLocalName() + "]" );
                    }
                }

                if( p.getLabel("") != null){
                    property.setName( prefixModel + p.getLabel("") );
                }

                elementos.put(prefixModel + p.getLocalName() ,property);


            }

        }

    }

    private static void anyadirInformacion() throws IOException {


        // Guardamos las propiedades de los individuos
        Iterator it = elementos.keySet().iterator();
        while(it.hasNext()){

            String indName =  (String)it.next();
            Elemento ind = elementos.get(indName);
            if( ind instanceof Individuo){

                String indURI = elementos.get(indName).getURI();

                String propiedad = "";
                String valor = "";

                StmtIterator propsIterator = infModel.getIndividual(indURI).listProperties();
                while ( propsIterator.hasNext()) {

                    Statement s = (Statement) propsIterator.next();
                    if( s.getPredicate().toString().contains(baseModel) ){


                        if (s.getObject().isLiteral()) {
                            valor = s.getLiteral().getValue().toString();
                        }
                        else{
                            valor = prefixModel + s.getObject().toString().split("#")[1];
                        }
                        propiedad = s.getPredicate().getLocalName();
                        elementos.get(indName).addCaracteristica("ObjectProperty", "T[" + prefixModel + propiedad + "]" + " " + "T[" + valor + "]");

                        OntProperty p = infModel.getOntProperty(baseModel + "#" + propiedad);
                        if ( p.hasInverse() ){
                            elementos.get(valor).addCaracteristica("ObjectProperty", "T[" + prefixModel + p.getInverse().getLocalName() + "]" + " " + "T[" + indName + "]");
                        }
                    }


                }
            }

        }
    }


    private static void leerPlantilla(String file) throws IOException{

        //Leemos la plantilla
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

            NodeList list = doc.getElementsByTagName("Restriccion");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                Element element = (Element) node;

                String id = element.getAttribute("id");

                NodeList listAttributes = element.getChildNodes();
                for (int attribute = 0; attribute < listAttributes.getLength(); attribute++) {
                    Node attributeNode = listAttributes.item(attribute);
                    if( !attributeNode.getNodeName().contains("#")){
                        String text = element.getElementsByTagName(attributeNode.getNodeName()).item(0).getTextContent();
                        template.put(id+"-"+attributeNode.getNodeName(),text);
                    }
                    if( attributeNode.hasChildNodes() ){
                        NodeList childNodes = attributeNode.getChildNodes();
                        int att = 1;
                        for (int childAttribute = 0; childAttribute < childNodes.getLength(); childAttribute++) {
                            Node childAttributeNode = childNodes.item(childAttribute);
                            if( !childAttributeNode.getNodeName().contains("#")){
                                String text = element.getElementsByTagName(attributeNode.getNodeName()).item(0).getTextContent().replaceAll("\\s+"," ").split(" ")[att];
                                template.put(id+"-"+attributeNode.getNodeName()+"-"+childAttributeNode.getNodeName(),text);
                                att++;
                            }
                        }
                    }
                }

            }
            template.put("Language", doc.getElementsByTagName("Language").item(0).getTextContent());
            template.put("Regular", "");

            list = doc.getElementsByTagName("Regular");
            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);
                NodeList childs = node.getChildNodes();
                for (int child = 0; child < childs.getLength(); child++) {
                    Node childNode = childs.item(child);
                    if ( childNode.getNodeName().equals("Antes") || childNode.getNodeName().equals("Despues") ){
                        template.put( "Regular" , template.get("Regular") + " " + childNode.getTextContent());
                    }
                }

            }


        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String salida;

    private static String XML_recursive(Node node, boolean primero) {


        String nombre = "";
        String lastRestriccion = "";
        String lastProperty = "";
        String loopValue = "";
        if (node.getNodeType() == Node.ELEMENT_NODE){
            Element element = (Element) node;
            String nodeName = element.getNodeName();
            salida = salida + template.get(nodeName + "-Text");

            if( salida != null){
                NodeList nodeList = element.getChildNodes();
                for(int i = 0; i < nodeList.getLength() ; i++ ){

                    Node chidlNode = nodeList.item(i);
                    String childName = chidlNode.getNodeName();

                    if( !childName.contains("#") ) {
                        if (lastRestriccion.equals(childName)) {
                            salida = salida + template.get(nodeName + "-Loop");
                        }

                        if (element.hasAttributes()) { //Atributos del propio nodo
                            String replaceString = template.get(element.getNodeName() + "-Extraccion-Content");
                            String valueString = element.getAttributes().item(0).getTextContent().replaceAll(".*[#:]", "");

                            if (replaceString != null && valueString != null) {

                                replaceString = replaceString.replaceAll("\\s+", "");
                                if( elementos.get(valueString) != null){
                                    salida = salida.replace(replaceString, "[" + elementos.get(valueString).getName() + "]" );
                                }
                                else{
                                    salida = salida.replace(replaceString, "[" + valueString + "]" );
                                }

                            }
                        }

                        //<abab IRI="Contenido>Value</abab>
                        if (chidlNode.hasAttributes()) { //Contenido de los hijos
                            String replaceString = template.get(element.getNodeName() + "-Extraccion-" + childName);
                            String valueString = chidlNode.getAttributes().item(0).getTextContent().replaceAll(".*[#:]", "");

                            if (replaceString != null && !replaceString.equals("none") && valueString != null) {

                                if ( primero && replaceString.equals("[?name]") ) {

                                    primero = false;
                                    nombre = prefixModel + valueString;

                                }
                                else if( replaceString.equals("[?name]") ){
                                    loopValue = prefixModel + valueString;
                                }

                                if( replaceString.contains("?property")){
                                    lastProperty = valueString;
                                    if(lastProperty.equals("comment")){
                                        salida = salida.replace("T[?value]","");
                                    }
                                }

                                replaceString = replaceString.replaceAll("\\s+", "");
                                if( elementos.get(valueString) != null){
                                    salida = salida.replace(replaceString, "[" + elementos.get(valueString).getName() + "]" );
                                }
                                else{
                                    salida = salida.replace(replaceString, "[" + valueString + "]" );
                                }
                            }
                        }

                        //<abab IRI="Contenido>Value</abab>
                        if( chidlNode.getTextContent() != null){ //Value de los hijos
                            String replaceString = template.get(childName + "-Extraccion-Value");
                            String valueString = chidlNode.getTextContent().replace("#","");

                            if (replaceString != null && valueString != null) {

                                replaceString = replaceString.replaceAll("\\s+", "").replace("#","");
                                valueString = valueString.replace("#","");
                                if( elementos.get(valueString) != null && !salida.contains("label")){
                                    salida = salida.replace(replaceString, "[" + elementos.get(valueString).getName() + "]" );
                                }
                                else{
                                    salida = salida.replace(replaceString, "[" + valueString + "]");
                                }

                                if( elementos.get(valueString) != null && nombre.equals("") ){
                                    nombre = prefixModel + valueString;
                                }
                            }
                        }

                        lastRestriccion = childName;

                        if ( nodeList.item(i).hasChildNodes() ) {
                            XML_recursive(nodeList.item(i), false);
                        }
                    }
                }

                if( template.get(nodeName + "-Inverse") != null){
                    String info = template.get(nodeName + "-Inverse");
                    OntProperty p = infModel.getOntProperty(baseModel + "#" + lastProperty);
                    if( !nombre.equals("") ){
                        if ( p != null && p.getInverse() != null ){ //inversa de propiedad
                            Individual ind1 = infModel.getIndividual(baseModel + "#" + nombre);
                            Individual ind2 = infModel.getIndividual(baseModel + "#" + ind1.getPropertyValue(p).toString().split("#")[1] );

                            info = info.replace("[?0]", "[" + prefixModel + ind2.getLocalName() + "]" );
                            info = info.replace("[?1]", "[" + prefixModel + p.getInverse().getLocalName() + "]"  );
                            info = info.replace("[?2]", "[" + nombre + "]" );
                            elementos.get(prefixModel + ind2.getLocalName()).setDescripcion(info);
                        }
                        else if( !loopValue.equals("") && lastProperty.equals("")){ //inversa de caracteristica

                            info = info.replace("[?0]", "[" + loopValue + "]" );
                            info = info.replace("[?2]", "[" + nombre + "]" );
                            elementos.get(loopValue).setDescripcion(info);
                        }


                    }


                }


            }



            if( !nombre.equals("") ) return nombre;
        }





        return "";


    }


    private static void anyadirInformacionXML(String file) throws IOException{

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

            NodeList list = doc.getElementsByTagName("Ontology").item(0).getChildNodes();
            //añadimos propiedades de las entidades
            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);
                salida = "";
                String ent = XML_recursive(node, true );
                if( salida != null && !ent.equals("") && elementos.get(ent) != null ) {
                    salida = salida.replaceAll("[A-Z]\\[\\?[a-zA-Z0-9]*\\]","");
                    elementos.get(ent).setDescripcion(salida.replace("#", ""));
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }





    private static void verbalizarModelo()  throws IOException{
        //Entidades
        Iterator it = elementos.keySet().iterator();
        while(it.hasNext()){
            String entName =  (String)it.next();
            Elemento elemento = elementos.get(entName);
            elemento.describir(template,myWriter);
            myWriter.write("\n");
        }
    }




    public static void main (String [ ] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, ScriptException, InterruptedException {

        //Parametros:
        //     ontology_file    final_lenguage_template       boolInferencia       boolTraducir
        // si boolInferencia = 0 no inferimos si es 1 inferimos
        // si boolTraducir = 0 no traducimos si es 1 traducimos los nombres

        if (args.length != 4){
            System.out.println("\n\nNumero de parametros incorrecto, usage:\n         ./program  ontology_file  final_lenguage_template  boolInferencia  boolTraducir\n");
            System.exit(1);
        }
        // inferimos mediante un reasoner, que es el mismo de protege(en teoria)
        if( args[2].equals("1")){
            OntDocumentManager mgr = new OntDocumentManager();
            mgr.setProcessImports(true);
            OntModelSpec s = new OntModelSpec(PelletReasonerFactory.THE_SPEC);

            s.setDocumentManager( mgr );
            infModel = ModelFactory.createOntologyModel( s );
        }
        else{
            infModel = ModelFactory.createOntologyModel();
        }



        String fileRDF = args[0];
        String fileXML = "src/files/finalFiles/auxFileXML.owl";
        String infFile = "src/files/finalFiles/infModel.owl";
        String templateFile = args[1];

        System.out.println("\t Reading...");
        infModel.read(fileRDF);
        System.out.println("\t [ok]");

        // load the model to the reasoner
        System.out.println("\t Preparing...");
        infModel.prepare();
        System.out.println("\t [ok]");

        //Escribimos el modelo inferido
        FileWriter out = new FileWriter( infFile );
        infModel.write(out);


        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(infFile));
        //Create a file for the new format
        File fileformated = new File(fileXML);
        //Save the ontology in a different format
        OWLDocumentFormat format = manager.getOntologyFormat(ontology);
        OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
        if (format.isPrefixOWLOntologyFormat()) {
            owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(ontology, owlxmlFormat, IRI.create(fileformated.toURI()));

        leerPlantilla(templateFile);

        myWriter = new FileWriter("salida.txt");

        anyadirClases(); //añadimos entidades, individuos y propiedades

        String fileName = "infModel_turtle.rdf";
        out = new FileWriter( fileName );
        try {
            infModel.write( out, "TURTLE" );
        }
        finally {
            try {
                out.close();
            }
            catch (IOException closeException) {
                // ignore
            }
        }

        anyadirInformacion();

        anyadirInformacionXML(fileXML);

        verbalizarModelo();

        myWriter.close();

        //Ejecutamos el script python

        System.out.println("Se verbalizará la ontología " + fileRDF + " al idioma " + template.get("Language"));
        String output = null;
        prefixModel = (baseModel.split("//")[1] + "#").toLowerCase();
        ProcessBuilder processBuilder = new ProcessBuilder("python", "pythonCode/fileParser.py",template.get("Language"),  args[3], prefixModel, template.get("Regular") );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(process.getInputStream()));

        // read the output from the command
        while ((output = stdInput.readLine()) != null) {
            System.out.println(output);
        }

        int exitCode = process.waitFor();

        //Partes del algoritmo:
            //Extraer la informacion de la ontologia
                //partiendo del formato turtle buscar tripletas que coincidan con alguna estructura - pdf2
                    //Estas estructuras las podemos sacar de la tabla del pdf2

            //Permitir entrada de usuario: cosas que no quiere que se digan - pdf2

            //buscar forma de evitar redundancia, aproximacion sencilla a refinamiento semántico o emplear un map e ir añadiendo

            //Añadir frases hechas escritas en lenguaje natural para facilitar el entendimiento: - pdf2
                // "Esto es" , "fue creado" , "ademas esta compuesto por" , ....
                // extraerlas de la platilla de lenguaje español - pdf5

            //seria posible hacer una aproximacion al ingles empleando una platilla a ingles - pdf5

    }

    private static OntModel borrarRecursosRDFS(Model inf) {
        //hacemos una copia del modelo ya que el modelo inferido es inmutable
        OntModel model2 = ModelFactory.createOntologyModel();
        model2.add(inf);

        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#List"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#Class"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#label"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#Resource"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#seeAlso"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#Container"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#Datatype"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#comment"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#range"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#subClassOf"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#Literal"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#domain"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2000/01/rdf-schema#nil"), null, (RDFNode)null);
        return model2;
    }

}
