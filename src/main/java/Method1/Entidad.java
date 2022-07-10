package Method1;

import org.w3c.dom.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Entidad extends Elemento {


    private  Vector<String> entidadesSuperiores = new Vector<String>();


    Entidad(String URI, String name){
        super(URI,name);
        entidadesSuperiores.clear();
    }

    // Eliminamos la redundancia
    private void prepararDatos(){

        //Eliminamos las apariciones de some redundantes
        if ( caracteristicas.get("AllUserDefined") != null){
            for( String s : caracteristicas.get("AllUserDefined") ){
                if( caracteristicas.get("UserDefined") != null){
                    String prop = caracteristicas.get("UserDefined").get(0).split(" ")[0];
                    boolean primero = true;

                    for (int i=caracteristicas.get("UserDefined").size()-1; i>=0;i--) {
                        String busqueda;
                        if(primero){
                            busqueda = s;
                            primero = false;
                        }
                        else{
                            busqueda = prop + " " + s;
                        }

                        if( busqueda.contains(caracteristicas.get("UserDefined").get(i))){
                            ArrayList<String> aux = caracteristicas.get("UserDefined");
                            aux.remove(i);
                            caracteristicas.put("UserDefined",  aux);
                        }

                    }
                }

            }



        }
    }
    public  void describir(Map<String, String> template ,FileWriter myWriter) throws IOException {

        prepararDatos();
        String frase = "";
        String lastFrase = "";
        String escribir = "";

        myWriter.write("T[" + elementoName +"]" + " : ");

        Iterator it = caracteristicas.keySet().iterator();
        while(it.hasNext()){
            String key = (String) it.next();
            String text = template.get(key+"-Text");
            String loop = template.get(key+"-Loop");
            String finish = template.get(key+"-Finish");

            ArrayList<String> lista = caracteristicas.get(key);


            for (int i=0;i<lista.size();i++) {

                if( i >= 1 && i == lista.size()-1 && finish != null && loop != null && caracteristicas.get("Disjoint").size() != 0){
                    frase = completarFrase( loop, lista.get(i).split(" "));
                    frase = frase + completarFrase( finish, null);
                }
                else if( i != 0 && loop != null){
                    frase = completarFrase( loop, lista.get(i).split(" "));
                }
                else if( key == "Comentario" && text != null){
                    String[] v = new String[1];
                    v[0] = lista.get(i);
                    frase = completarFrase( text, v);
                }
                else if( text != null){
                    frase = completarFrase( text, lista.get(i).split(" "));
                }



                if (SimilarityUtils.similarity(lastFrase, frase) < 0.92 ){

                    if( !escribir.contains(frase)){
                        escribir = escribir + frase;
                    }
                }
                lastFrase = frase;

            }
            escribir = escribir + " . ";


        }
        myWriter.write(escribir.replaceAll(" \\[\\?.*\\]",""));

        if( !descripcion.equals("") ){
            myWriter.write(descripcion + " . " );
        }
    }

}
