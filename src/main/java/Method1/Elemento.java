package Method1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Elemento {
    protected  String elementoURI;
    protected  String elementoName;

    protected Map<String, ArrayList<String>> caracteristicas = new HashMap<String, ArrayList<String> >();
    protected String descripcion;

    Elemento(String URI, String name){
        elementoURI = URI;
        elementoName = name;
        caracteristicas.clear();
        descripcion = "";
    }

    public  void addCaracteristica( String nameRestriccion, String newRestriccion){
        if( !verbalizedProperty(newRestriccion)){
            if(caracteristicas.get(nameRestriccion) == null){
                caracteristicas.put(nameRestriccion, new ArrayList<String>());
            }
            caracteristicas.get(nameRestriccion).add(newRestriccion);
        }

    }


    //entidadURI
    public  void setURI(String newURI){
        elementoURI = newURI;
    }
    public  String getURI(){
        return elementoURI;
    }

    public  void setName(String newName){
        elementoName = newName;
    }
    public  String getName(){
        return elementoName;
    }

    public boolean verbalizedProperty(String description){
        String newDescription = description.replace("T[" + elementoName + "]","");
        newDescription = newDescription.replace("G[" + elementoName + "]","");

        for( String caracteristica : caracteristicas.keySet() ){
            if( caracteristicas.get(caracteristica) != null){
                for (String propiedad : caracteristicas.get(caracteristica)) {
                    boolean aparece = true;
                    for (int i = 0; i < newDescription.split(" ").length; i++) {
                        String palabra = newDescription.split(" ")[i];
                        if( palabra.contains("[") && !propiedad.contains(palabra)){
                            aparece = false;
                        }
                    }
                    if ( aparece ){
                        return true;
                    }
                }
            }
        }
        /*
        System.out.println("Para la nueva descripcion " + newDescription);
        if( caracteristicas.get("ObjectProperty") != null){
            for (String propiedad : caracteristicas.get("ObjectProperty")) {
                System.out.println("En la propiedad1: " + propiedad);
                boolean aparece = true;
                for (int i = 0; i < newDescription.split(" ").length; i++) {
                    String palabra = newDescription.split(" ")[i];
                    System.out.println("Comprobamos palabra " + palabra);
                    if( palabra.contains("[") && !propiedad.contains(palabra)){
                        System.out.println("Esa palabra no aparecia");
                        aparece = false;
                    }
                }
                if ( aparece ){
                    System.out.println("Dejamos de añadir1 : " + elementoName + " "+ newDescription);
                    return true;
                }
            }
        }

        if( caracteristicas.get("ClassAssertion") != null) {
            for (String propiedad : caracteristicas.get("ClassAssertion")) {
                boolean aparece = true;
                System.out.println("En la propiedad2: " + propiedad);
                for (int i = 0; i < newDescription.split(" ").length; i++) {
                    String palabra = newDescription.split(" ")[i];
                    System.out.println("Comprobamos palabra " + palabra);
                    if( palabra.contains("[") && !propiedad.contains(palabra)){
                        System.out.println("Esa palabra no aparecia");
                        aparece = false;
                    }
                }
                if ( aparece ){
                    System.out.println("Dejamos de añadir2 : " + elementoName + " "+ newDescription);
                    return true;
                }
            }
        }
        if( caracteristicas.get("ClassAssertionJ") != null) {
            for (String propiedad : caracteristicas.get("ClassAssertionJ")) {
                boolean aparece = true;
                System.out.println("En la propiedad2: " + propiedad);
                for (int i = 0; i < newDescription.split(" ").length; i++) {
                    String palabra = newDescription.split(" ")[i];
                    System.out.println("Comprobamos palabra " + palabra);
                    if( palabra.contains("[") && !propiedad.contains(palabra)){
                        System.out.println("Esa palabra no aparecia");
                        aparece = false;
                    }
                }
                if ( aparece ){
                    System.out.println("Dejamos de añadir2 : " + elementoName + " "+ newDescription);
                    return true;
                }
            }
        }*/

        return false;
    }
    public void setDescripcion(String newDescription){


        /*
        if ( !verbalizedProperty(newDescription) ) {

            boolean anyadir = false;
            //Evitamos informacion repetido
            for (String palabra : newDescription.split(" ")) {
                if ((!descripcion.contains("[")) || (palabra.contains("[") && !descripcion.contains(palabra))) {
                    anyadir = true;
                    break;
                }
            }

            if (anyadir) {
                if (descripcion.equals("") || descripcion.equals(" ")) {
                    descripcion = newDescription;
                } else if (!descripcion.contains(newDescription)) {
                    descripcion = newDescription + " . " + descripcion;
                }
            }
        }*/
        if ( !verbalizedProperty(newDescription) ) {
            if (descripcion.equals("") || descripcion.equals(" ")) {
                descripcion = newDescription;
            } else if (!descripcion.contains(newDescription)) {
                descripcion = newDescription + " . " + descripcion;
            }
        }

    }

    protected String completarFrase( String template, String[] text){

        String frase = template;
        frase = frase.replace("[?name]", "[" +  elementoName + "]");

        for ( int index = 0; template.contains( "[?" + index + "]") ; index++ ){
            if(text != null)
                frase = frase.replace( "[?" + index + "]", "[" + text[index].replaceAll(".*:", "") + "]" );
        }

        if ( frase.contains("[?") ) {
            return "";
        }
        return frase.replace("T[T[","T[").replace("]]","]");
    }

    /*
    private String separarPorVocales( String s){

        String newString = s.replaceAll("xsd:","represented by a ");
        newString = newString.replaceAll("[a-z]+:","");
        String returnString = "";

        for (int i=0 ; i<newString.length() ; i++ ) {
            if ( Character.isUpperCase(newString.charAt(i) ) && i == 0 ) {
                returnString = returnString + Character.toLowerCase( newString.charAt(i) );
            }
            else if ( Character.isUpperCase(newString.charAt(i) ) ){
                returnString = returnString + " " + Character.toLowerCase( newString.charAt(i) ) ;
            }
            else {
                returnString = returnString + newString.charAt(i);
            }
        }

        returnString = returnString.replaceAll("  ", " ");


        return returnString;
    }
     */
    public void describir(Map<String, String> template , FileWriter myWriter) throws IOException {

    }
}
