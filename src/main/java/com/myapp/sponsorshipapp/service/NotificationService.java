package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.entity.Notification;
import com.myapp.sponsorshipapp.entity.User;
import com.myapp.sponsorshipapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private AuthService authService;
    
    public Notification createNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications() {
        User user = authService.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUnreadNotifications() {
        User user = authService.getCurrentUser();
        return notificationRepository.findByUserAndIsReadFalse(user);
    }
    
    public long getUnreadCount() {
        User user = authService.getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    public void markAllAsRead() {
        User user = authService.getCurrentUser();
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}

