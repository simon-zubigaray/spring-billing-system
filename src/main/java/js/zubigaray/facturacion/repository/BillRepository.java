package js.zubigaray.facturacion.repository;

import js.zubigaray.facturacion.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {

    @Query("""
        SELECT b
        FROM BillEntity b
        JOIN FETCH b.user u
        WHERE b.active = true
    """)
    List<BillEntity> findAllActive();

    @Modifying
    @Transactional
    @Query("""
        UPDATE BillEntity b
        SET b.active = false
        WHERE b.id = :billId
    """)
    void softDelete(@Param("billId") Long billId);
}