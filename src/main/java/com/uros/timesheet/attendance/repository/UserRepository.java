package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(UUID id);

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findActiveByUsername(String username);

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdIncludingDeleted(UUID id);

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u")
    List<User> findAllIncludingDeleted();

    @EntityGraph(attributePaths = {"organization", "roles", "roles.permissions", "team"})
    @Query("SELECT u FROM User u WHERE u.organization.id = :organizationId AND u.deletedAt IS NULL")
    List<User> findByOrganizationId(UUID organizationId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countActive();

    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countByStatus();

    @Query("SELECT o.name, COUNT(u) FROM User u JOIN u.organization o WHERE u.deletedAt IS NULL GROUP BY o.name")
    List<Object[]> countByOrganization();

    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r WHERE u.deletedAt IS NULL GROUP BY r.name")
    List<Object[]> countByRole();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    long countByStatusName(String status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.name = :orgName AND u.deletedAt IS NULL")
    long countByOrganizationName(String orgName);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deletedAt IS NULL")
    long countByRoleName(String roleName);
}