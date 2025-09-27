package de.codebarista.shopware.appserver.config;


class Result<T> {
    private final T result;
    private final String error;

    private Result(T result, String error) {
        this.result = result;
        this.error = error;
    }

    static <T> Result<T> success(T result) {
        return new Result<>(result, null);
    }

    static <T> Result<T> error(String error) {
        return new Result<>(null, error);
    }

    static <T> Result<T> error(String errorFmt, Object... fmtArgs) {
        return error(String.format(errorFmt, fmtArgs));
    }

    T getResult() {
        if (result == null) {
            throw new RuntimeException(error);
        }
        return result;
    }

    boolean isError() {
        return error != null;
    }

    /**
     * Gets the {@link #error}.
     */
    String getError() {
        return error;
    }
}
