package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findAllByRole(Role role);

    @Query("""
            select u
            from User u
            where lower(u.username) like lower(concat('%', :query, '%'))
               or lower(u.fullName) like lower(concat('%', :query, '%'))
            order by u.username asc
            """)
    List<User> searchByUsername(@Param("query") String query);

    @Query("""
            select u
            from User u
            where u.active = true
            order by u.username asc
            """)
    List<User> findAllActiveUsersOrdered();

    void deleteByUserId(String userId);
}
