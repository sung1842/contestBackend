package com.project.mingle.Repository;

import com.project.mingle.Entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersDetailRepository extends JpaRepository<UserDetail, Long> {
}
