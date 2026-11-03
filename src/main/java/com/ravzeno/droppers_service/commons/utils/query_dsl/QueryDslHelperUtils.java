package com.ravzeno.droppers_service.commons.utils.query_dsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.ravzeno.droppers_service.commons.dto.enum_type.SortOption;
import java.time.Instant;

public class QueryDslHelperUtils {

    public static OrderSpecifier<?> buildSortSpec(StringPath field, SortOption option) {
        return switch (option) {
            case ASC -> field.asc();
            case DESC -> field.desc();
        };
    }

    public static OrderSpecifier<?> buildSortSpec(BooleanPath field, SortOption option) {
        return switch (option) {
            case ASC -> field.asc();
            case DESC -> field.desc();
        };
    }

    public static OrderSpecifier<?> buildSortSpec(NumberPath<?> field, SortOption option) {
        return switch (option) {
            case ASC -> field.asc();
            case DESC -> field.desc();
        };
    }

    public static OrderSpecifier<?> buildSortSpec(DateTimePath<Instant> field, SortOption option) {
        return switch (option) {
            case ASC -> field.asc();
            case DESC -> field.desc();
        };
    }
}
