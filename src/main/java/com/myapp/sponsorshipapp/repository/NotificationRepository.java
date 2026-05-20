package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.Notification;
import com.myapp.sponsorshipapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    long countByUserAndIsReadFalse(User user);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = ?1")
    void deleteByUserId(Long userId);
}

