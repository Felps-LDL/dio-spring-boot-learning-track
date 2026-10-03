package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.application.output.TransactionSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummarizeTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public SummarizeTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-transactions",
            description = "Resume os gastos registrados: quantidade e valor total em reais, no geral e por categoria")
    public TransactionSummaryOutput execute() {
        var transactions = transactionRepository.findAll();

        Map<Category, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));

        var categories = Arrays.stream(Category.values())
                .filter(byCategory::containsKey)
                .map(category -> summarize(category, byCategory.get(category)))
                .toList();

        return new TransactionSummaryOutput(transactions.size(), toReais(sumCents(transactions)), categories);
    }

    private static CategorySummaryOutput summarize(Category category, List<Transaction> transactions) {
        return new CategorySummaryOutput(category.name(), transactions.size(), toReais(sumCents(transactions)));
    }

    private static long sumCents(List<Transaction> transactions) {
        return transactions.stream().mapToLong(Transaction::getAmount).sum();
    }

    private static double toReais(long cents) {
        return BigDecimal.valueOf(cents, 2).doubleValue();
    }
}
