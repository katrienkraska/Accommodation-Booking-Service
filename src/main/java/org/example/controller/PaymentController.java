package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.payment.PaymentDetailsDto;
import org.example.dto.payment.PaymentDto;
import org.example.service.payment.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments",
        description = "Manages payment-related operations including initiating payments "
                + "and handling payment status updates.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Initiate Stripe payment session",
            description = "Creates a Stripe Checkout session for the booking "
                    + "with the specified ID. Returns a URL to the hosted "
                    + "payment page where the customer can complete the transaction.")
    public String createPaymentIntent(@PathVariable Long id, Authentication authentication) {
        return paymentService.createPaymentCheckoutSession(id, authentication.getName());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Get all payments",
            description = "Retrieves all payments made by users. "
                    + "Accessible only to administrators.")
    public List<PaymentDetailsDto> getAllPayments() {
        return paymentService.findAllPayments();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(
            summary = "Get all payments",
            description = "Retrieves all payments associated with the currently authenticated "
                    + "customer based on their email address.")
    public List<PaymentDetailsDto> getUserPayments(Authentication authentication) {
        return paymentService.findPaymentsByUserEmail(authentication.getName());
    }

    @GetMapping("/success/{sessionId}")
    @Operation(summary = "Confirm payment",
            description = "Handles a successful Stripe Checkout session using "
                    + "the provided session ID. Retrieves session details, "
                    + "updates the corresponding booking and payment statuses, "
                    + "and returns the finalized payment information.")
    public PaymentDto successPayments(@PathVariable String sessionId) {
        return paymentService.successPayment(sessionId);
    }

    @GetMapping("/cancel/{sessionId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(
            summary = "Cancel payment",
            description = "Handles cancellation of a Stripe Checkout session using "
                    + "the provided session ID. Updates the payment and associated "
                    + "booking status if the session is still open.")
    public PaymentDto cancelPayments(@PathVariable String sessionId) {
        return paymentService.cancelPaymentAndBooking(sessionId);
    }
}
