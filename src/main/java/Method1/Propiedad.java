package Method1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Propiedad extends Elemento{


    Propiedad(String URI, String name){
        super(URI,name);
    }


    public  void describir(Map<String, String> template , FileWriter myWriter) throws IOException {

        String frase = "";
        String lastFrase = "";
        String escribir = "";
        myWriter.write("T[" + elementoName +"]" + " : ");

        myWriter.write( completarFrase( template.get("DescriptionProperty-Text") + ". ", null ) );

        Iterator it = caracteristicas.keySet().iterator();
        while(it.hasNext()){
            String key = (String) it.next();
            String text = template.get(key+"-Text");
            String loop = template.get(key+"-Loop");
            String finish = template.get(key+"-Finish");

            ArrayList<String> lista = caracteristicas.get(key);

            for (int i=0;i<lista.size();i++) {

                if( i == lista.size()-1 && finish != null && loop != null ){
                    frase = completarFrase( loop, lista.get(i).split(" "));
                    frase = frase + completarFrase( finish, null);
                }
                else if( i != 0 && loop != null){

                    frase = completarFrase( loop, lista.get(i).split(" "));
                }
                else if( key == "Comentario"){
                    String[] v = new String[1];
                    v[0] = lista.get(i);
                    frase = completarFrase( text, v);
                }
                else{
                    if( lista.get(i) != null)
                        frase = completarFrase( text, lista.get(i).split(" "));
                    else
                        frase = completarFrase( text, null);
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
