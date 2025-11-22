package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Builder for constructing Shopware Admin API search queries.
 * <p>
 * This class provides a fluent API for building search criteria to query Shopware entities.
 * It supports pagination, filtering, sorting, associations, and field selection.
 * <p>
 * Example usage:
 * <pre>
 * SearchQuery query = new SearchQuery()
 *     .limit(25)
 *     .page(1)
 *     .addFilter(new EqualsFilter("active", true))
 *     .addSorting(Sorting.byField("name", Order.ASCENDING))
 *     .withAssociation("media", new AssociationCriteria());
 * </pre>
 */
@JsonInclude(value = Include.NON_NULL)
public class SearchQuery {
    @JsonProperty("limit")
    private int limit = 10;

    @JsonProperty("page")
    private int page = 1;

    @JsonProperty("associations")
    private Map<String, AssociationCriteria> associations;

    @JsonProperty("includes")
    private Map<String, List<String>> includes;

    @JsonProperty("ids")
    private Collection<String> ids;

    @JsonProperty("sort")
    private Collection<Sorting> sortings;

    @JsonProperty("filter")
    private Collection<Filter> filters;

    /**
     * Sets the maximum number of results to return.
     *
     * @param limit the maximum number of results (default is 10)
     * @return this SearchQuery for method chaining
     */
    public SearchQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the page number for pagination.
     *
     * @param page the page number (1-indexed, default is 1)
     * @return this SearchQuery for method chaining
     */
    public SearchQuery page(int page) {
        this.page = page;
        return this;
    }

    /**
     * Adds an association to load related entities.
     * <p>
     * Associations allow you to load related entities in the same request.
     * For example, loading products with their media or categories.
     *
     * @param name     the name of the association (e.g., "media", "categories")
     * @param criteria the search criteria for the associated entities
     * @return this SearchQuery for method chaining
     */
    public SearchQuery withAssociation(String name, AssociationCriteria criteria) {
        if (associations == null) {
            associations = new HashedMap<>();
        }
        associations.put(name, criteria);
        return this;
    }

    /**
     * Specifies which fields to include in the response for a given entity.
     *
     * @param entityName the entity name (e.g., "product")
     * @param fields     the field names to include
     * @return this SearchQuery for method chaining
     */
    public SearchQuery includes(String entityName, String... fields) {
        if (includes == null) {
            includes = new HashedMap<>();
        }
        includes.put(entityName, Arrays.asList(fields));
        return this;
    }

    /**
     * Limits the search to entities with specific IDs.
     * <p>
     * When set, only entities with the provided IDs will be returned.
     *
     * @param ids the collection of entity IDs to search for
     * @return this SearchQuery for method chaining
     */
    public SearchQuery ids(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    /**
     * Adds a sorting criterion to the query.
     * <p>
     * Multiple sortings can be added.
     *
     * @param sort the sorting criterion
     * @return this SearchQuery for method chaining
     */
    public SearchQuery addSorting(Sorting sort) {
        if (sortings == null) {
            sortings = new ArrayList<>();
        }
        sortings.add(sort);
        return this;
    }

    /**
     * Adds a filter to narrow down search results.
     * <p>
     * Multiple filters can be added and will be combined with AND logic.
     *
     * @param filter the filter to add
     * @return this SearchQuery for method chaining
     */
    public SearchQuery addFilter(Filter filter) {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.add(filter);
        return this;
    }

}
