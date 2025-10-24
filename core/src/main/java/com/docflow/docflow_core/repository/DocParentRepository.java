package com.docflow.docflow_core.repository;

import com.docflow.docflow_core.entity.DocParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocParentRepository extends JpaRepository<DocParent, Long> {
}
