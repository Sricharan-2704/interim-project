package com.myapp.sponsorshipapp.controller;

import com.myapp.sponsorshipapp.dto.ApiResponse;
import com.myapp.sponsorshipapp.dto.PaymentRequest;
import com.myapp.sponsorshipapp.entity.Payment;
import com.myapp.sponsorshipapp.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:4200","https://gregarious-naiad-7592c9.netlify.app"})
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.createPayment(request);
            return ResponseEntity.ok(new ApiResponse(true, "Payment created successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completePayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.completePayment(id);
            return ResponseEntity.ok(new ApiResponse(true, "Payment completed", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/influencer")
    public ResponseEntity<List<Payment>> getInfluencerPayments() {
        return ResponseEntity.ok(paymentService.getInfluencerPayments());
    }
    
    @GetMapping("/brand")
    public ResponseEntity<List<Payment>> getBrandPayments() {
        return ResponseEntity.ok(paymentService.getBrandPayments());
    }
    
    @GetMapping("/earnings")
    public ResponseEntity<?> getEarnings() {
        return ResponseEntity.ok(paymentService.getInfluencerEarnings());
    }
    
    @GetMapping("/spending")
    public ResponseEntity<?> getSpending() {
        return ResponseEntity.ok(paymentService.getBrandSpending());
    }
}

