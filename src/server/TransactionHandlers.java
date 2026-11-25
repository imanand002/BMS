package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Account;
import model.Customer;
import model.Transaction;
import repository.AccountRepository;
import repository.CustomerRepository;
import repository.TransactionRepository;
import service.BankService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionHandlers {
    private static final BankService bankService = new BankService();
    private static final AccountRepository accountRepo = new AccountRepository();
    private static final CustomerRepository customerRepo = new CustomerRepository();
    private static final TransactionRepository txRepo = new TransactionRepository();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a", Locale.ENGLISH);

    /**
     * POST /api/transactions/deposit
     */
    public static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            Integer accNum = JsonUtil.normalizeAccountNumber(JsonUtil.getInt(body, "accountNumber"));
            Double amount = JsonUtil.getDouble(body, "amount");

            if (accNum == null || amount == null || amount <= 0) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Valid accountNumber and positive amount are required.\"}");
                return;
            }

            Account account = accountRepo.getAccountByNumber(accNum);
            if (account == null) {
                BankHttpServer.sendResponse(exchange, 404, "{\"error\": \"Account not found.\"}");
                return;
            }

            boolean success = bankService.deposit(accNum, amount);
            if (!success) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Deposit failed.\"}");
                return;
            }

            Account updated = accountRepo.getAccountByNumber(accNum);
            String response = String.format(
                "{\"success\": true, \"message\": \"Successfully deposited ₹%.2f into Savings Account #%d\", \"newBalance\": %.2f, \"account\": {\"number\": %d, \"balance\": %.2f, \"type\": \"SAVINGS\"}}",
                amount, accNum, updated.getBalance(), updated.getAccountNumber(), updated.getBalance()
            );
            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }

    /**
     * POST /api/transactions/withdraw
     */
    public static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            Integer accNum = JsonUtil.normalizeAccountNumber(JsonUtil.getInt(body, "accountNumber"));
            Double amount = JsonUtil.getDouble(body, "amount");

            if (accNum == null || amount == null || amount <= 0) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Valid accountNumber and positive amount are required.\"}");
                return;
            }

            Account account = accountRepo.getAccountByNumber(accNum);
            if (account == null || account.getCustomerId() != customer.getCustomerId()) {
                BankHttpServer.sendResponse(exchange, 403, "{\"error\": \"Account not found or does not belong to you.\"}");
                return;
            }

            if (account.getBalance() < amount) {
                BankHttpServer.sendResponse(exchange, 400, String.format(
                    "{\"error\": \"Insufficient funds. Available balance is ₹%.2f\"}", account.getBalance()));
                return;
            }

            boolean success = bankService.withdraw(accNum, amount);
            if (!success) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Withdrawal failed.\"}");
                return;
            }

            Account updated = accountRepo.getAccountByNumber(accNum);
            String response = String.format(
                "{\"success\": true, \"message\": \"Successfully withdrew ₹%.2f from Savings Account #%d\", \"newBalance\": %.2f, \"account\": {\"number\": %d, \"balance\": %.2f, \"type\": \"SAVINGS\"}}",
                amount, accNum, updated.getBalance(), updated.getAccountNumber(), updated.getBalance()
            );
            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }

    /**
     * POST /api/transactions/transfer
     * Body: { "fromAccount": 1001, "toAccount": 1002, "amount": 500.0, "note": "Rent" }
     */
    public static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            Integer fromAcc = JsonUtil.normalizeAccountNumber(JsonUtil.getInt(body, "fromAccount"));
            Integer toAcc = JsonUtil.normalizeAccountNumber(JsonUtil.getInt(body, "toAccount"));
            Double amount = JsonUtil.getDouble(body, "amount");
            String note = JsonUtil.getString(body, "note");

            if (fromAcc == null || toAcc == null || amount == null || amount <= 0) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Source account, beneficiary account, and positive amount are required.\"}");
                return;
            }

            if (fromAcc.equals(toAcc)) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Cannot transfer funds to the exact same account.\"}");
                return;
            }

            Account source = accountRepo.getAccountByNumber(fromAcc);
            Account dest = accountRepo.getAccountByNumber(toAcc);

            if (source == null || source.getCustomerId() != customer.getCustomerId()) {
                BankHttpServer.sendResponse(exchange, 403, "{\"error\": \"Source account not found or does not belong to you.\"}");
                return;
            }

            if (dest == null) {
                BankHttpServer.sendResponse(exchange, 404, "{\"error\": \"Beneficiary Account #" + toAcc + " does not exist.\"}");
                return;
            }

            if (source.getBalance() < amount) {
                BankHttpServer.sendResponse(exchange, 400, String.format(
                    "{\"error\": \"Insufficient balance. Available: ₹%.2f\"}", source.getBalance()));
                return;
            }

            boolean success = bankService.transferFunds(fromAcc, toAcc, amount);
            if (!success) {
                BankHttpServer.sendResponse(exchange, 500, "{\"error\": \"Fund transfer failed. Rolled back.\"}");
                return;
            }

            Account updatedSource = accountRepo.getAccountByNumber(fromAcc);
            Customer destCustomer = customerRepo.getCustomerById(dest.getCustomerId());
            String recipientName = (destCustomer != null) ? destCustomer.getFirstName() + " " + destCustomer.getLastName() : "Account #" + toAcc;
            String referenceId = "TXN" + System.currentTimeMillis();

            String response = String.format(
                "{\"success\": true, \"message\": \"Successfully transferred ₹%.2f to %s (#%d)\", \"recipientName\": \"%s\", \"referenceId\": \"%s\", \"fromAccountBalance\": %.2f}",
                amount,
                JsonUtil.escape(recipientName),
                toAcc,
                JsonUtil.escape(recipientName),
                referenceId,
                updatedSource.getBalance()
            );
            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }

    /**
     * GET /api/transactions
     * Returns recent transactions across all accounts of the logged-in customer.
     */
    public static class TransactionHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use GET.\"}");
                return;
            }

            List<Account> accounts = accountRepo.getAccountsByCustomerId(customer.getCustomerId());
            Set<Integer> myAccNums = new HashSet<>();
            for (Account a : accounts) myAccNums.add(a.getAccountNumber());

            Set<Integer> seenTxIds = new HashSet<>();
            List<Transaction> allTx = new ArrayList<>();

            for (Account acc : accounts) {
                List<Transaction> list = txRepo.getTransactionsByAccount(acc.getAccountNumber());
                for (Transaction t : list) {
                    if (seenTxIds.add(t.getTransactionId())) {
                        allTx.add(t);
                    }
                }
            }

            // Sort newest first
            allTx.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"transactions\": [");
            for (int i = 0; i < allTx.size(); i++) {
                Transaction t = allTx.get(i);
                String displayType = t.getTransactionType();
                String detail;

                if ("TRANSFER".equals(t.getTransactionType())) {
                    boolean isSender = (t.getSourceAccountNumber() != null && myAccNums.contains(t.getSourceAccountNumber()));
                    boolean isReceiver = (t.getDestinationAccountNumber() != null && myAccNums.contains(t.getDestinationAccountNumber()));

                    String srcFmt = t.getSourceAccountNumber() != null ? AccountRepository.formatAccountNumber(t.getSourceAccountNumber()) : "";
                    String dstFmt = t.getDestinationAccountNumber() != null ? AccountRepository.formatAccountNumber(t.getDestinationAccountNumber()) : "";

                    if (isSender && isReceiver) {
                        detail = String.format("Self Transfer: #%s to #%s", srcFmt, dstFmt);
                    } else if (isSender) {
                        displayType = "TRANSFER_OUT";
                        detail = String.format("Paid to Beneficiary #%s", dstFmt);
                    } else {
                        displayType = "TRANSFER_IN";
                        detail = String.format("Received from Account #%s", srcFmt);
                    }
                } else if ("DEPOSIT".equals(t.getTransactionType())) {
                    String dstFmt = t.getDestinationAccountNumber() != null ? AccountRepository.formatAccountNumber(t.getDestinationAccountNumber()) : "";
                    detail = String.format("Deposit into Savings #%s", dstFmt);
                } else {
                    String srcFmt = t.getSourceAccountNumber() != null ? AccountRepository.formatAccountNumber(t.getSourceAccountNumber()) : "";
                    detail = String.format("Withdrawal from Savings #%s", srcFmt);
                }

                String dateStr = (t.getTimestamp() != null) ? t.getTimestamp().format(DATE_FORMATTER) : "";

                sb.append(String.format(
                    "{\"id\": %d, \"type\": \"%s\", \"rawType\": \"%s\", \"amount\": %.2f, \"detail\": \"%s\", \"date\": \"%s\", \"source\": %s, \"destination\": %s, \"timestamp\": \"%s\"}",
                    t.getTransactionId(),
                    displayType,
                    t.getTransactionType(),
                    t.getAmount(),
                    JsonUtil.escape(detail),
                    dateStr,
                    t.getSourceAccountNumber() != null ? t.getSourceAccountNumber().toString() : "null",
                    t.getDestinationAccountNumber() != null ? t.getDestinationAccountNumber().toString() : "null",
                    t.getTimestamp() != null ? t.getTimestamp().toString() : ""
                ));
                if (i < allTx.size() - 1) sb.append(",");
            }
            sb.append("]}");

            BankHttpServer.sendResponse(exchange, 200, sb.toString());
        }
    }
}
