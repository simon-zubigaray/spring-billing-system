package js.zubigaray.facturacion.repository;

import js.zubigaray.facturacion.entity.BillUser;
import js.zubigaray.facturacion.entity.BillUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillUserRepository extends JpaRepository<BillUser, BillUserId> {

    List<BillUser> findByBill_Id(Long billId);

    List<BillUser> findByProduct_Id(Long productId);
}