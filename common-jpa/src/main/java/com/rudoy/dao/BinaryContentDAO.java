package com.rudoy.dao;

import com.rudoy.entity.enums.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
