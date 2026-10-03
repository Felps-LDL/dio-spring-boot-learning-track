package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionSummaryOutput;

import java.util.List;

public record TransactionSummaryResponse(long count, double total, List<CategorySummaryResponse> categories) {
    public static TransactionSummaryResponse from(TransactionSummaryOutput output) {
        return new TransactionSummaryResponse(
                output.count(),
                output.total(),
                output.categories().stream().map(CategorySummaryResponse::from).toList());
    }
}
