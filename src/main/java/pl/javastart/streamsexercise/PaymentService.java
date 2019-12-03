package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;
    private List<Payment> payments;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    List<Payment> findPaymentsSortedByDateDesc() {
        payments = paymentRepository.findAll();
        return payments.stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForCurrentMonth() {
        payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> {
                    ZonedDateTime d1 = payment.getPaymentDate();
                    ZonedDateTime d2 = dateTimeProvider.zonedDateTimeNow();
                    return d1.getYear() == d2.getYear() && d1.getMonth().equals(d2.getMonth());
                }).collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> {
                    ZonedDateTime d1 = payment.getPaymentDate();
                    return d1.getYear() == yearMonth.getYear() && d1.getMonth().equals(yearMonth.getMonth());
                })
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenLastDays(int days) {
        payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getPaymentDate().compareTo(dateTimeProvider.zonedDateTimeNow().minusDays(days)) >= 0)
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithOnePaymentItem() {
        payments = paymentRepository.findAll();
        Set<Payment> set = payments.stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
        return set;
    }

    Set<String> findProductsSoldInCurrentMonth() {
        return findPaymentsForCurrentMonth().stream()
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth).stream()
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal::add).get();
    }

    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth).stream()
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(paymentItem -> paymentItem.getRegularPrice().subtract(paymentItem.getFinalPrice()))
                .reduce(BigDecimal::add).get();
    }

    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithValueOver(int value) {
        payments = paymentRepository.findAll();
        return payments.stream().filter(payment ->
                payment.getPaymentItems().stream()
                        .map(PaymentItem::getFinalPrice)
                        .reduce(BigDecimal::add).get()
                        .compareTo(BigDecimal.valueOf(value)) > 0
        ).collect(Collectors.toSet());
    }
}