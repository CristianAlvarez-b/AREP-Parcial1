package co.edu.escuelaing.arep.parcial;

import java.util.ArrayList;
import java.util.Arrays;

public class BubbleSort {

    public static ArrayList<Double> ordenar(double[] numbers) {
        ArrayList<Double> lista = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++){
            lista.add(numbers[i]);
        }
        boolean flag = true;
        while(flag){
            int cont = 0;
            for (int i =0; i < lista.size() - 1; i++){
                double aux = lista.get(i);
                if(aux > lista.get(i+1)){
                    lista.set(i, lista.get(i+1));
                    lista.set(i+1, aux);
                    cont++;
                }
            }
            if(cont == 0){
                flag = false;
            }
        }
        return lista;
    }
}
