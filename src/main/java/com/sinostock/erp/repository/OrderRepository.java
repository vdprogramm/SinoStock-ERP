package com.sinostock.erp.repository;
import com.sinostock.erp.domain.SaleOrder;
import com.sinostock.erp.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<SaleOrder, Long> {
    List<SaleOrder> findAllByOrderByCreatedAtDesc();
    List<SaleOrder> findAllByStatus(OrderStatus status);
    List<SaleOrder> findAllByStatusAndConfirmedAtGreaterThanEqualAndConfirmedAtLessThan(OrderStatus status, Instant from, Instant to);
    long countByStatus(OrderStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from SaleOrder o where o.id = :id")
    Optional<SaleOrder> findByIdForUpdate(@Param("id") Long id);
}
