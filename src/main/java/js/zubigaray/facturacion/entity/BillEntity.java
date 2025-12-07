package js.zubigaray.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "bills")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    private LocalDateTime dateTime = LocalDateTime.now();

    // Soft delete
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(
            mappedBy = "bill",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<BillUser> details = new ArrayList<>();

    // Valor total calculado dinámicamente
    public double getTotal() {
        return details.stream()
                .mapToDouble(d -> d.getProduct().getPrice() * d.getQuantity())
                .sum();
    }
}