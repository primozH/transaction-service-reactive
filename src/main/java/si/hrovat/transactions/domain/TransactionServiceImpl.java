package si.hrovat.transactions.domain;

import io.smallrye.mutiny.Uni;
import si.hrovat.transactions.application.rest.model.TransactionStatus;
import si.hrovat.transactions.domain.in.TransactionService;
import si.hrovat.transactions.domain.model.Transaction;
import si.hrovat.transactions.domain.out.TransactionEventManager;
import si.hrovat.transactions.domain.out.TransactionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionEventManager transactionEventManager;

    @Inject
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionEventManager transactionEventManager) {
        this.transactionRepository = transactionRepository;
        this.transactionEventManager = transactionEventManager;
    }

    @Override
    public Uni<Transaction> findTransaction(String id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Uni<List<Transaction>> findTransactions() {
        return transactionRepository.findAll().collect().asList();
    }

    @Override
    public Uni<Transaction> createTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.CREATED);
        return transactionRepository.save(transaction).call(tr ->
                transactionEventManager.pushTransaction(tr)
                        .ifNoItem().after(Duration.ofSeconds(5)).fail()
        );
    }

    @Override
    public Uni<Void> deleteTransaction(String id) {
        return transactionRepository.deleteTransaction(id);
    }
}
