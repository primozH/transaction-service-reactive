package si.hrovat.transactions.domain.out;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import si.hrovat.transactions.domain.model.Transaction;

public interface TransactionRepository {

    Uni<Transaction> findById(String id);

    Uni<Transaction> save(Transaction transaction);

    Multi<Transaction> findAll();

    Uni<Void> deleteTransaction(String id);
}
