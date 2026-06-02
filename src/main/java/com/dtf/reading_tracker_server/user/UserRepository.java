package com.dtf.reading_tracker_server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthId(String auth0Id);

    boolean existsByAuthId(String auth0Id);

    void deleteByAuthId(String auth0id);
}