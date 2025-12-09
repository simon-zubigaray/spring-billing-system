package js.zubigaray.facturacion.service;

import js.zubigaray.facturacion.dto.BillDetailRequest;
import js.zubigaray.facturacion.entity.*;
import js.zubigaray.facturacion.repository.BillRepository;
import js.zubigaray.facturacion.repository.ProductRepository;
import js.zubigaray.facturacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Obtener todas las facturas activas con el usuario asociado cargado (JOIN FETCH)
    @Transactional(readOnly = true)
    public List<BillEntity> findAllActive() {
        return billRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public Optional<BillEntity> findById(Long id) {
        return billRepository.findById(id);
    }

    /**
     * Crea una nueva factura, actualiza el stock de los productos y gestiona los detalles.
     * Es crucial que sea @Transactional para asegurar que todas las operaciones se completen
     * correctamente o se reviertan (rollback) si algo falla (ej. falta de stock).
     */
    @Transactional
    public BillEntity createBill(Long userId, List<BillDetailRequest> details) {
        // 1. Obtener el usuario
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 2. Crear la entidad de la factura
        BillEntity bill = new BillEntity();
        bill.setUser(user);

        // 3. Procesar los detalles de la factura y actualizar el stock
        List<BillUser> billDetails = new ArrayList<>();

        for (BillDetailRequest detailRequest : details) {
            ProductEntity product = productRepository.findById(detailRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + detailRequest.getProductId()));

            Long quantity = detailRequest.getQuantity();

            // Verificar si el producto está activo
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is inactive: " + product.getName());
            }

            // Verificar y restar stock (la lógica de la entidad lanza la excepción)
            product.subtractStock(quantity);

            // Crear el detalle de la factura (BillUser)
            BillUserId billUserId = new BillUserId(null, product.getId()); // billId se seteará al guardar la factura
            BillUser billUser = BillUser.builder()
                    .id(billUserId)
                    .bill(bill)
                    .product(product)
                    .quantity(quantity)
                    .build();

            billDetails.add(billUser);
        }

        // 4. Establecer los detalles en la factura
        bill.setDetails(billDetails);

        // 5. Guardar la factura (Persistirá los detalles y actualizará los productos por el efecto de 'subtractStock')
        // En este punto, JPA insertará la BillEntity, obtendrá el ID generado, y lo usará
        // para insertar correctamente cada BillUser gracias a @MapsId y CascadeType.ALL.
        return billRepository.save(bill);
    }

    // Soft delete de la factura
    @Transactional
    public void softDelete(Long billId) {
        billRepository.softDelete(billId);
    }
}