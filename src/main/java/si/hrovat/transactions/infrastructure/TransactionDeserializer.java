package si.hrovat.transactions.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import si.hrovat.transactions.domain.model.Transaction;

public class TransactionDeserializer extends ObjectMapperDeserializer<Transaction> {
    public TransactionDeserializer() {
        super(Transaction.class);
    }
}
