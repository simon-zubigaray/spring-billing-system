package js.zubigaray.facturacion.exception.custom_exceptions;

// Se usa esta excepción en el AdminService si se intenta asignar un rol que no existe.
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}