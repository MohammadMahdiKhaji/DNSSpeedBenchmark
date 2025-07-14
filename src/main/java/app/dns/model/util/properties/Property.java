package app.dns.model.util.properties;

public class Property<T> {
    private String propertyFullName;
    private T propertyValue;
    private Class<T> propertyType;

    public Property(String propertyFullName, Class<T> propertyType) {
        this.propertyFullName = propertyFullName;
        this.propertyType = propertyType;
    }

    public String getPropertyFullName() {
        return propertyFullName;
    }

    public void setPropertyFullName(String propertyFullName) {
        this.propertyFullName = propertyFullName;
    }

    public T getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) throws TypeNotPresentException {
        if (propertyType == Integer.class) {
            this.propertyValue = (T) Integer.valueOf(propertyValue);
        } else if (propertyType == Boolean.class) {
            this.propertyValue = (T) Boolean.valueOf(propertyValue);
        } else if (propertyType == Byte.class) {
            this.propertyValue = (T) Byte.valueOf(propertyValue);
        } else if (propertyType == Short.class) {
            this.propertyValue = (T) Short.valueOf(propertyValue);
        } else if (propertyType == Long.class) {
            this.propertyValue = (T) Long.valueOf(propertyValue);
        } else if (propertyType == Float.class) {
            this.propertyValue = (T) Float.valueOf(propertyValue);
        } else if (propertyType == Double.class) {
            this.propertyValue = (T) Double.valueOf(propertyValue);
        } else if (propertyType == String.class) {
            this.propertyValue = (T) String.valueOf(propertyValue);
        } else {
            throw new IllegalArgumentException ("The generic type doesn't exist: " + propertyType.getName(), null);
        }
    }
}