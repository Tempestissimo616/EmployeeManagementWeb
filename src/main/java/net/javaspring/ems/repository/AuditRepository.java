package net.javaspring.ems.repository;

import net.javaspring.ems.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit,Long> {
}
