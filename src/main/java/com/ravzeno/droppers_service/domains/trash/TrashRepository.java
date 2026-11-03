package com.ravzeno.droppers_service.domains.trash;

import com.ravzeno.droppers_service.domains.trash.model.Trash;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrashRepository extends JpaRepository<Trash, UUID> {}
