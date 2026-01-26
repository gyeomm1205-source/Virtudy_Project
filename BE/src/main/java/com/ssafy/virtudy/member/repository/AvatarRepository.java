package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
