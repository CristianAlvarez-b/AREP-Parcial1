package co.edu.escuelaing.arep.parcial;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflexCalculator {

    public static Double calculate(String operation, double[] numbers) {
        try{
            Method method = getMathMethod(operation, numbers.length);
            return (Double) method.invoke(null, toObjectArray(numbers));
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static Object[] toObjectArray(double[] numbers) {
        return Arrays.stream(numbers).boxed().toArray(Double[]::new);
    }

    private static Method getMathMethod(String operation, int length) throws NoSuchMethodException {
        Class<?>[] paramTypes = length == 1 ? new Class<?>[]{double.class} : new Class<?>[]{double.class, double.class};
        return Math.class.getMethod(operation, paramTypes);
    }

}
