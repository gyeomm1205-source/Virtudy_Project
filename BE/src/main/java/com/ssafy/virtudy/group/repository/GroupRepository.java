package com.ssafy.virtudy.group.repository;

import com.ssafy.virtudy.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
