package si.hrovat.transactions.infrastructure;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import si.hrovat.transactions.domain.out.TransactionRepository;
import si.hrovat.transactions.domain.model.Transaction;
import si.hrovat.transactions.infrastructure.entities.TransactionEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZoneId;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepositoryImpl implements TransactionRepository {

    private final Mutiny.Session session;

    @Inject
    public TransactionRepositoryImpl(Mutiny.Session session) {
        this.session = session;
    }

    @Override
    public Uni<Transaction> findById(String id) {
        return session.find(TransactionEntity.class, UUID.fromString(id))
                .onItem().transform(TransactionRepositoryImpl::from);
    }

    @Override
    public Uni<Transaction> save(Transaction transaction) {
        TransactionEntity entity = from(transaction);
        return session
                .withTransaction(tx -> {
                    if (transaction.getId() == null) {
                        return session.persist(entity).map(unused -> entity);
                    } else {
                        return session.find(TransactionEntity.class, transaction.getId())
                                .onItem()
                                .call(tr -> {
                                    if (tr == null) {
                                        return session.persist(entity);
                                    } else {
                                        map(tr, entity);
                                        return session.merge(entity);
                                    }
                                });
                    }
                })
                .onItem().transform(TransactionRepositoryImpl::from);
    }

    @Override
    public Multi<Transaction> findAll() {
        return session.createNamedQuery("Transaction.findAll", TransactionEntity.class)
                .getResults()
                .onItem().transform(TransactionRepositoryImpl::from);
    }

    @Override
    public Uni<Void> deleteTransaction(String id) {
        return session.withTransaction(tx ->
                session.find(TransactionEntity.class, UUID.fromString(id))
                        .chain(session::remove)
        );
    }

    public static Transaction from(TransactionEntity en) {
        if (en == null) return null;
        Transaction transaction = new Transaction();
        transaction.setId(en.getId());
        transaction.setAmount(en.getAmount());
        transaction.setCreditorAccountId(en.getCreditor());
        transaction.setDebtorAccountId(en.getDebtor());
        transaction.setCreated(en.getCreated().atZone(ZoneId.systemDefault()));
        transaction.setDescription(en.getDescription());
        transaction.setStatus(en.getStatus());
        return transaction;
    }

    public static TransactionEntity from(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setAmount(transaction.getAmount());
        entity.setCreditor(transaction.getCreditorAccountId());
        entity.setDebtor(transaction.getDebtorAccountId());
        entity.setDescription(transaction.getDescription());
        entity.setCreated(transaction.getCreated().toLocalDateTime());
        entity.setStatus(transaction.getStatus());
        return entity;
    }

    private static void map(TransactionEntity from, TransactionEntity to) {
        if (from == null || to == null) return;

        if (from.getCreatedOn() != null) to.setCreatedOn(from.getCreatedOn());
        if (from.getUpdatedOn() != null) to.setUpdatedOn(from.getUpdatedOn());

        if (from.getCreditor() != null) to.setCreditor(from.getCreditor());
        if (from.getDebtor() != null) to.setDebtor(from.getDebtor());
        if (from.getDescription() != null) to.setDescription(from.getDescription());
        if (from.getAmount() != null) to.setAmount(from.getAmount());
        if (from.getCreated() != null) to.setCreated(from.getCreated());
    }
}
