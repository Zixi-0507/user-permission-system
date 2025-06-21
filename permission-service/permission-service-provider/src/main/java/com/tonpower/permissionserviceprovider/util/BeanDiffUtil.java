package com.tonpower.permissionserviceprovider.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeanDiffUtil {
    public static Map<String, Object> diff(Object oldObj, Object newObj) {
        Map<String, Object> result = new HashMap<>();
        if (oldObj == null || newObj == null) return result;

        Field[] fields = newObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object oldValue = field.get(oldObj);
                Object newValue = field.get(newObj);
                if (!Objects.equals(oldValue, newValue)) {
                    Map<String, Object> diffMap = new HashMap<>();
                    diffMap.put("oldValue", oldValue);
                    diffMap.put("newValue", newValue);
                    result.put(field.getName(), diffMap);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}
