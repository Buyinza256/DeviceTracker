package com.bcs.devicemonitor.modules.devices.repositories;

import com.bcs.devicemonitor.modules.devices.entities.Entity_StatusReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StatusReportRepository extends JpaRepository<Entity_StatusReport, Long> {

    /**
     * The most recent reports for a single device, newest first. Combine with a
     * {@link Pageable} of size 20 to satisfy the "20 most recent reports" view.
     */
    List<Entity_StatusReport> findByDeviceIdOrderByReportedAtDescIdDesc(UUID deviceId, Pageable pageable);

    /**
     * The latest report for every device, resolved in a single query so the
     * device list does not trigger an N+1 lookup. The newest report per device
     * is identified by the highest id, which reflects insertion order.
     */
    @Query("""
            select sr from Entity_StatusReport sr
            where sr.id in (
                select max(s.id) from Entity_StatusReport s group by s.device.id
            )
            """)
    List<Entity_StatusReport> findLatestReportPerDevice();
}
