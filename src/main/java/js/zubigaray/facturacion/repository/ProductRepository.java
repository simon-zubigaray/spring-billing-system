package js.zubigaray.facturacion.repository;

import js.zubigaray.facturacion.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT p
        FROM ProductEntity p
        WHERE p.price BETWEEN :minValue AND :maxValue
        AND p.active = true
    """)
    List<ProductEntity> findByPriceRange(
            @Param("minValue") double minValue,
            @Param("maxValue") double maxValue
    );

    List<ProductEntity> findByStock(Long stock);

    @Query("SELECT p FROM ProductEntity p WHERE p.active = true")
    List<ProductEntity> findAllActive();
}
