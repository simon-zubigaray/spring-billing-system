package js.zubigaray.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Table(name = "products")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private double price;

    @NotNull
    @Min(0)
    private Long stock;

    // Soft delete
    private boolean active = true;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BillUser> billUsers = new ArrayList<>();

    public boolean hasStock(long amount) {
        return this.stock >= amount;
    }

    public void subtractStock(long amount) {
        if (!hasStock(amount)) {
            throw new IllegalArgumentException("Not enough stock.");
        }
        this.stock -= amount;
    }
}