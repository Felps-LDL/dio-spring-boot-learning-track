package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.CategorySummaryOutput;

public record CategorySummaryResponse(String category, long count, double total) {
    public static CategorySummaryResponse from(CategorySummaryOutput output) {
        return new CategorySummaryResponse(output.category(), output.count(), output.total());
    }
}
