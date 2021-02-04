package si.hrovat.transactions.domain.in;

import io.smallrye.mutiny.Uni;
import si.hrovat.transactions.domain.model.Transaction;

import java.util.List;

public interface TransactionService {
    Uni<Transaction> findTransaction(String id);

    Uni<List<Transaction>> findTransactions();

    Uni<Transaction> createTransaction(Transaction transaction);

    Uni<Void> deleteTransaction(String id);
}
