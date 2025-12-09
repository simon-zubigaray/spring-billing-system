package js.zubigaray.facturacion.service;

import js.zubigaray.facturacion.entity.ProductEntity;
import js.zubigaray.facturacion.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Obtener todos los productos activos
    @Transactional(readOnly = true)
    public List<ProductEntity> findAllActive() {
        return productRepository.findAllActive();
    }

    // Obtener un producto por ID
    @Transactional(readOnly = true)
    public Optional<ProductEntity> findById(Long id) {
        return productRepository.findById(id);
    }

    // Crear o actualizar un producto
    @Transactional
    public ProductEntity save(ProductEntity product) {
        // En una aplicación real, agregarías validaciones adicionales antes de guardar
        return productRepository.save(product);
    }

    // Búsqueda por nombre (parcial e insensible a mayúsculas)
    @Transactional(readOnly = true)
    public List<ProductEntity> findByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Búsqueda por rango de precio
    @Transactional(readOnly = true)
    public List<ProductEntity> findByPriceRange(double min, double max) {
        return productRepository.findByPriceRange(min, max);
    }

    // Realiza un Soft Delete (cambia 'active' a false)
    @Transactional
    public boolean softDelete(Long id) {
        Optional<ProductEntity> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            ProductEntity product = productOpt.get();
            product.setActive(false);
            productRepository.save(product);
            return true;
        }
        return false;
    }
}