package com.ssafy.virtudy.group.repository;

import com.ssafy.virtudy.group.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
}
