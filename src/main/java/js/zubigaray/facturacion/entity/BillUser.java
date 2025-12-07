package js.zubigaray.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bill_user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BillUser {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private BillUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("billId")
    @JoinColumn(name = "bill_id")
    @JsonIgnore
    private BillEntity bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @NotNull
    @Min(1)
    private Long quantity;

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}