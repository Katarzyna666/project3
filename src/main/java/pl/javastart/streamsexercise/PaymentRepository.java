package pl.javastart.streamsexercise;

import java.util.List;

public interface PaymentRepository {

    List<Payment> findAll();
}