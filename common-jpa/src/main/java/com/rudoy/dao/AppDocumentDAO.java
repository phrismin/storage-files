package com.rudoy.dao;

import com.rudoy.entity.enums.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentDAO extends JpaRepository<BinaryContent, Long> {
}
