package libs.errs;

public final class GeneralErrors {

    public static final String RECORD_NOT_FOUND = "record.not.found";
    public static final String VALUE_IS_INVALID = "value.is.invalid";
    public static final String VALUE_IS_REQUIRED = "value.is.required";
    public static final String INVALID_STRING_LENGTH = "invalid.string.length";
    public static final String COLLECTION_IS_TOO_SMALL = "collection.is.too.small";
    public static final String COLLECTION_IS_TOO_LARGE = "collection.is.too.large";
    public static final String VALUE_IS_OUT_OF_RANGE = "value.is.out.of.range";
    public static final String VALUE_MUST_BE_GREATER_THAN = "value.must.be.greater.than";
    public static final String VALUE_MUST_BE_GREATER_OR_EQUAL = "value.must.be.greater.or.equal";
    public static final String VALUE_MUST_BE_LESS_THAN = "value.must.be.less.than";
    public static final String VALUE_MUST_BE_LESS_OR_EQUAL = "value.must.be.less.or.equal";

    private GeneralErrors() {
        // Utility class, no instantiation
    }

    public static <T> Error notFound(String name, T id) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(RECORD_NOT_FOUND, String.format("Record not found. Name: %s, id: %s", name, id));
    }

    public static <T> Error valueIsInvalid(String name, T value) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(VALUE_IS_INVALID, String.format("Value '%s' is invalid for %s", value, name));
    }

    public static Error valueIsRequired(String name) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        return Error.of(VALUE_IS_REQUIRED, "Value is required for " + name);
    }

    public static Error invalidLength(String name) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        return Error.of(INVALID_STRING_LENGTH, "Invalid " + name + " length");
    }

    public static Error collectionIsTooSmall(int min, int current) {
        return Error.of(COLLECTION_IS_TOO_SMALL,
                "The collection must contain " + min + " items or more. It contains " + current + " items.");
    }

    public static Error collectionIsTooLarge(int max, int current) {
        return Error.of(COLLECTION_IS_TOO_LARGE,
                "The collection must contain " + max + " items or fewer. It contains " + current + " items.");
    }

    public static <T extends Comparable<T>> Error valueIsOutOfRange(String name, T value, T min, T max) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        String message = String.format("Value %s for %s is out of range. Min value is %s, max value is %s.", value,
                name, min, max);

        return Error.of(VALUE_IS_OUT_OF_RANGE, message);
    }

    public static <T extends Comparable<T>> Error valueMustBeGreaterThan(String name, T value, T min) {

        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(VALUE_MUST_BE_GREATER_THAN,
                String.format("The value of %s (%s) must be greater than %s.", name, value, min));
    }

    public static <T extends Comparable<T>> Error valueMustBeGreaterOrEqual(String name, T value, T min) {

        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(VALUE_MUST_BE_GREATER_OR_EQUAL,
                String.format("The value of %s (%s) must be greater than or equal to %s.", name, value, min));
    }

    public static <T extends Comparable<T>> Error valueMustBeLessThan(String name, T value, T max) {

        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(VALUE_MUST_BE_LESS_THAN,
                String.format("The value of %s (%s) must be less than %s.", name, value, max));
    }

    public static <T extends Comparable<T>> Error valueMustBeLessOrEqual(String name, T value, T max) {

        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        return Error.of(VALUE_MUST_BE_LESS_OR_EQUAL,
                String.format("The value of %s (%s) must be less than or equal to %s.", name, value, max));
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
