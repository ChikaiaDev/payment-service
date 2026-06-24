package com.ncba.payment;

import com.ncba.payment.dto.PaymentDTO.*;
import com.ncba.payment.exception.PaymentNotFoundException;
import com.ncba.payment.exception.PaymentProcessingException;
import com.ncba.payment.models.Payment;
import com.ncba.payment.repository.PaymentRepository;
import com.ncba.payment.service.DownstreamNotifier;
import com.ncba.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationTests {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private DownstreamNotifier downstreamNotifier;

	@Mock
	private org.springframework.web.client.RestTemplate restTemplate;

	@InjectMocks
	private PaymentService paymentService;

	private Payment mockPayment;
	private PaymentRequest validRequest;

	@BeforeEach
	void setUp() {
		mockPayment = new Payment();
		mockPayment.setId(1L);
		mockPayment.setTransactionRef("TXN-12345678");
		mockPayment.setSenderAccount("ACC001");
		mockPayment.setReceiverAccount("ACC002");
		mockPayment.setAmount(new BigDecimal("1000.00"));
		mockPayment.setCurrency("KES");
		mockPayment.setStatus(Payment.PaymentStatus.COMPLETED);
		mockPayment.setCreatedAt(LocalDateTime.now());
		mockPayment.setUpdatedAt(LocalDateTime.now());

		validRequest = new PaymentRequest();
		validRequest.setSenderAccount("ACC001");
		validRequest.setReceiverAccount("ACC002");
		validRequest.setAmount(new BigDecimal("1000.00"));
		validRequest.setCurrency("KES");
	}

	// ── processPayment ──────────────────────────────────────────

	@Test
	void processPayment_ShouldReturnResponse_WhenRequestIsValid() {
		when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

		PaymentResponse response = paymentService.processPayment(validRequest);

		assertNotNull(response);
		assertEquals("TXN-12345678", response.getTransactionRef());
		assertEquals("COMPLETED", response.getStatus());
		assertEquals("ACC001", response.getSenderAccount());
		assertEquals("ACC002", response.getReceiverAccount());
		assertEquals(new BigDecimal("1000.00"), response.getAmount());
		assertEquals("KES", response.getCurrency());
		verify(paymentRepository, times(2)).save(any(Payment.class));
	}

	@Test
	void processPayment_ShouldThrowException_WhenAmountIsZero() {
		validRequest.setAmount(BigDecimal.ZERO);

		PaymentProcessingException ex = assertThrows(
				PaymentProcessingException.class,
				() -> paymentService.processPayment(validRequest)
		);

		assertEquals("Payment amount must be positive.", ex.getMessage());
		verifyNoInteractions(paymentRepository);
	}

	@Test
	void processPayment_ShouldThrowException_WhenAmountIsNegative() {
		validRequest.setAmount(new BigDecimal("-100.00"));

		assertThrows(
				PaymentProcessingException.class,
				() -> paymentService.processPayment(validRequest)
		);

		verifyNoInteractions(paymentRepository);
	}

	@Test
	void processPayment_ShouldThrowException_WhenAmountIsNull() {
		validRequest.setAmount(null);

		assertThrows(
				PaymentProcessingException.class,
				() -> paymentService.processPayment(validRequest)
		);

		verifyNoInteractions(paymentRepository);
	}

	@Test
	void processPayment_ShouldNotifyDownstreamServices_WhenPaymentSucceeds() {
		when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

		paymentService.processPayment(validRequest);

		// give @Async a moment to fire
		verify(downstreamNotifier, timeout(2000)).notifyNotificationService(any(PaymentEvent.class));
		verify(downstreamNotifier, timeout(2000)).notifyReportingService(any(PaymentEvent.class));
	}

	// ── getAllPayments ───────────────────────────────────────────

	@Test
	void getAllPayments_ShouldReturnListOfPayments() {
		Payment second = new Payment();
		second.setId(2L);
		second.setTransactionRef("TXN-87654321");
		second.setSenderAccount("ACC003");
		second.setReceiverAccount("ACC004");
		second.setAmount(new BigDecimal("500.00"));
		second.setCurrency("USD");
		second.setStatus(Payment.PaymentStatus.COMPLETED);

		when(paymentRepository.findAll()).thenReturn(List.of(mockPayment, second));

		List<PaymentResponse> responses = paymentService.getAllPayments();

		assertEquals(2, responses.size());
		assertEquals("TXN-12345678", responses.get(0).getTransactionRef());
		assertEquals("TXN-87654321", responses.get(1).getTransactionRef());
	}

	@Test
	void getAllPayments_ShouldReturnEmptyList_WhenNoPaymentsExist() {
		when(paymentRepository.findAll()).thenReturn(List.of());

		List<PaymentResponse> responses = paymentService.getAllPayments();

		assertTrue(responses.isEmpty());
	}

	// ── getPaymentById ──────────────────────────────────────────

	@Test
	void getPaymentById_ShouldReturnPayment_WhenIdExists() {
		when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));

		PaymentResponse response = paymentService.getPaymentById(1L);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("TXN-12345678", response.getTransactionRef());
	}

	@Test
	void getPaymentById_ShouldThrowException_WhenIdNotFound() {
		when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

		PaymentNotFoundException ex = assertThrows(
				PaymentNotFoundException.class,
				() -> paymentService.getPaymentById(99L)
		);

		assertEquals("Payment not found with id: 99", ex.getMessage());
	}

	// ── updatePayment ───────────────────────────────────────────

	@Test
	void updatePayment_ShouldReturnUpdatedPayment_WhenIdExists() {
		PaymentRequest updateRequest = new PaymentRequest();
		updateRequest.setSenderAccount("ACC010");
		updateRequest.setReceiverAccount("ACC020");
		updateRequest.setAmount(new BigDecimal("2000.00"));
		updateRequest.setCurrency("USD");

		Payment updatedPayment = new Payment();
		updatedPayment.setId(1L);
		updatedPayment.setTransactionRef("TXN-12345678");
		updatedPayment.setSenderAccount("ACC010");
		updatedPayment.setReceiverAccount("ACC020");
		updatedPayment.setAmount(new BigDecimal("2000.00"));
		updatedPayment.setCurrency("USD");
		updatedPayment.setStatus(Payment.PaymentStatus.COMPLETED);

		when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

		PaymentResponse response = paymentService.updatePayment(1L, updateRequest);

		assertEquals("ACC010", response.getSenderAccount());
		assertEquals("ACC020", response.getReceiverAccount());
		assertEquals(new BigDecimal("2000.00"), response.getAmount());
		assertEquals("USD", response.getCurrency());
		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	void updatePayment_ShouldThrowException_WhenIdNotFound() {
		when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(
				PaymentNotFoundException.class,
				() -> paymentService.updatePayment(99L, validRequest)
		);

		verify(paymentRepository, never()).save(any(Payment.class));
	}

	// ── deletePayment ───────────────────────────────────────────

	@Test
	void deletePayment_ShouldDeleteSuccessfully_WhenIdExists() {
		when(paymentRepository.existsById(1L)).thenReturn(true);
		doNothing().when(paymentRepository).deleteById(1L);

		assertDoesNotThrow(() -> paymentService.deletePayment(1L));

		verify(paymentRepository).deleteById(1L);
	}

	@Test
	void deletePayment_ShouldThrowException_WhenIdNotFound() {
		when(paymentRepository.existsById(99L)).thenReturn(false);

		PaymentNotFoundException ex = assertThrows(
				PaymentNotFoundException.class,
				() -> paymentService.deletePayment(99L)
		);

		assertEquals("Payment not found with id: 99", ex.getMessage());
		verify(paymentRepository, never()).deleteById(any());
	}

}
