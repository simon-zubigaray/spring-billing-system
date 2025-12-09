package js.zubigaray.facturacion.exception.custom_exceptions;


// Se usa esta excepción cuando un usuario intente registrarse con un username o email ya en uso.
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}