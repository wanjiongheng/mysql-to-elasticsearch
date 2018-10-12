import com.netopstec.annotation.es.ESField;
import org.junit.Test;

import java.lang.reflect.Field;

public class SimpleTest {

    @Test
    public void print() throws ClassNotFoundException {
        Class clazz = Class.forName("com.netopstec.entity.Goods");
        Field[] fields = clazz.getDeclaredFields();
        Field field = fields[0];
        ESField annotation = field.getAnnotation(ESField.class);
        System.out.println(annotation);
        System.out.println(field.getName());

    }




}
