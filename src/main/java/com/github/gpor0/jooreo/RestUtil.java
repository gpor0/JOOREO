package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
import com.github.gpor0.jooreo.operations.OrderByOperation;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Author: gpor0
 */
public class RestUtil {

    public static final Pattern FILTER_PATTERN = Pattern.compile("(\\[[^\\[]*\\]|[^,])+");

    public static final String TOTAL_COUNT_HEADER = "X-Total-Count";
    public static final String HAS_NEXT_HEADER = "X-Has-Next";
    public static final String CURSOR_REF_HEADER = "X-Cursor-Ref";


    public static URI buildCreatedLink(final UriInfo uri, final Object createdId) {

        return uri.getAbsolutePathBuilder().path(createdId.toString())
                .build();
    }

    public static String camelToSnake(String str) {
        return str == null ? null :str.replaceAll("(?<!^|_|[A-Z])([A-Z])", "_$1").toLowerCase();
    }

    public static DataOperation[] buildOperations(UriInfo uri) {

        String order = uri.getQueryParameters().getFirst("order");

        Stream<DataOperation> orderByOpsStream;
        if (order != null && !order.isBlank()) {
            orderByOpsStream = Stream.of(order.split(",")).map(OrderByOperation::parse);
        } else {
            orderByOpsStream = Stream.empty();
        }

        String filter = uri.getQueryParameters().getFirst("filter");

        Stream<DataOperation> filterOpsStream;
        if (filter != null && !filter.isBlank()) {
            filterOpsStream = FILTER_PATTERN.matcher(filter).results().map(MatchResult::group).map(FilterOperation::parse);
        } else {
            filterOpsStream = Stream.empty();
        }

        return Stream.concat(filterOpsStream, orderByOpsStream).toArray(DataOperation[]::new);
    }

}
