package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummarizeTransactionsUseCaseTest {
    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final SummarizeTransactionsUseCase useCase = new SummarizeTransactionsUseCase(repository);

    @Test
    void should_returnEmptySummary_when_noTransactionsExist() {
        var summary = useCase.execute();

        assertThat(summary.count()).isZero();
        assertThat(summary.total()).isZero();
        assertThat(summary.categories()).isEmpty();
    }

    @Test
    void should_sumTotalsInReais_when_transactionsExist() {
        repository.save(new Transaction("Mercado", 8000, Category.GROCERIES));
        repository.save(new Transaction("Padaria", 1250, Category.GROCERIES));
        repository.save(new Transaction("Remédio", 4099, Category.PHARMA));

        var summary = useCase.execute();

        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.total()).isEqualTo(133.49);
        assertThat(summary.categories()).containsExactly(
                new CategorySummaryOutput("GROCERIES", 2, 92.50),
                new CategorySummaryOutput("PHARMA", 1, 40.99));
    }

    @Test
    void should_exposeSummaryAsTool_when_registeredInChatClient() {
        var callbacks = ToolCallbacks.from(useCase);

        assertThat(callbacks).hasSize(1);
        assertThat(callbacks[0].getToolDefinition().name()).isEqualTo("summarize-transactions");

        repository.save(new Transaction("Gasolina", 20000, Category.AUTO));
        var result = callbacks[0].call("{}");

        assertThat(result).contains("\"total\":200.0").contains("AUTO");
    }

    static class InMemoryTransactionRepository implements TransactionRepository {
        private final List<Transaction> transactions = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            transactions.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAllByCategory(Category category) {
            return transactions.stream().filter(t -> t.getCategory() == category).toList();
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(transactions);
        }
    }
}
