package si.hrovat.transactions.domain;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.hrovat.transactions.application.rest.model.TransactionStatus;
import si.hrovat.transactions.domain.model.Transaction;
import si.hrovat.transactions.domain.out.TransactionEventManager;
import si.hrovat.transactions.domain.out.TransactionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TransactionEventManagerImpl implements TransactionEventManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventManagerImpl.class);

    @Inject
    @Channel("transaction-events-out")
    Emitter<Transaction> emitter;

    @Inject
    TransactionRepository transactionRepository;

    @Override
    public Uni<Void> pushTransaction(Transaction transaction) {
        OutgoingKafkaRecordMetadata<Object> metadata = OutgoingKafkaRecordMetadata.builder()
                .withKey(transaction.getId().toString())
                .build();
        emitter.send(Message.of(transaction).addMetadata(metadata));
        return Uni.createFrom().nullItem();
    }

    @Incoming("transaction-events-in")
    @Outgoing("transaction-events-processed")
    public Uni<Message<Transaction>> processTransaction(Message<Transaction> message) {
        message.getMetadata(IncomingKafkaRecordMetadata.class).ifPresent(metadata -> logger.info("Transaction id {}", metadata.getKey()));
        Transaction transaction = message.getPayload();
        transaction.setStatus(TransactionStatus.ACCEPTED);
        return transactionRepository.save(transaction).map(message::withPayload);
    }
}
