package com.sinostock.erp.repository;
import com.sinostock.erp.domain.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    List<InventoryLog> findTop100ByOrderByCreatedAtDesc();
}
