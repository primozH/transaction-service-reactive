package si.hrovat.transactions.domain.out;

import io.smallrye.mutiny.Uni;
import si.hrovat.transactions.domain.model.Transaction;

public interface TransactionEventManager {

    Uni<Void> pushTransaction(Transaction transaction);

}
