package si.hrovat.transactions.application.rest.resources;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestSseElementType;
import si.hrovat.transactions.application.rest.model.CreateTransactionRequest;
import si.hrovat.transactions.domain.in.TransactionService;
import si.hrovat.transactions.domain.model.Transaction;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/transactions")
public class TransactionResource {

    private final TransactionService transactionService;

    private final UriInfo uriInfo;

    private final Multi<Transaction> transactionStream;

    @Inject
    public TransactionResource(TransactionService transactionService,
                               UriInfo uriInfo,
                               @Channel("transaction-events-processed-in") Multi<Transaction> transactionStream) {
        this.transactionService = transactionService;
        this.uriInfo = uriInfo;
        this.transactionStream = transactionStream;
    }

    @GET
    public Uni<Response> getTransactions() {
        return transactionService.findTransactions()
                .onItem()
                .transform(trList -> Response.ok(trList).build());
    }

    @GET
    @Path("{id}")
    public Uni<Response> getTransaction(@PathParam("id") String id) {
        return transactionService.findTransaction(id)
                .onItem()
                .transform(tr -> tr != null ? Response.ok(tr) :
                        Response.status(Response.Status.NOT_FOUND))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<Transaction> stream() {
        return transactionStream;
    }

    @POST
    public Uni<Response> createTransaction(CreateTransactionRequest request) {
        var transaction = from(request);
        return transactionService.createTransaction(transaction)
                .onItem()
                .transform(tr -> Response
                        .created(uriInfo
                                .getAbsolutePathBuilder()
                                .path(tr.getId().toString())
                                .build())
                        .entity(tr)
                        .build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteTransaction(@PathParam("id") String id) {
        return transactionService.deleteTransaction(id)
                .onItem()
                .transform(unused ->
                        Response.noContent().build());
    }

    public static Transaction from(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setCreditorAccountId(request.getCreditAccountId());
        transaction.setDebtorAccountId(request.getDebitAccountId());
        transaction.setDescription(request.getDescription());
        transaction.setCreated(request.getCreated());

        return transaction;
    }
}
