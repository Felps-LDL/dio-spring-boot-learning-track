package dio.budgeting.application.output;

import java.util.List;

public record TransactionSummaryOutput(long count, double total, List<CategorySummaryOutput> categories) {
}
