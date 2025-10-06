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

    @JsonProperty("fields")
    private Map<String, List<String>> fields;

    @JsonProperty("ids")
    private Collection<String> ids;

    @JsonProperty("sort")
    private Collection<Sorting> sortings;

    @JsonProperty("filter")
    private Collection<Filter> filters;

    public SearchQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SearchQuery page(int page) {
        this.page = page;
        return this;
    }

    public SearchQuery withAssociation(String name, AssociationCriteria criteria) {
        if (associations == null) {
            associations = new HashedMap<>();
        }
        associations.put(name, criteria);
        return this;
    }

    /**
     * Fields to include in the response.
     * <p>
     * In contrast to {@link SearchQuery#fields(String, String...)}, includes works as post-output processing,
     * so the complete entity is loaded in the backend side and then filtered.
     * @param entityName the name of the entity for which only the specified fields shall be included in the response
     * @param fields the names of the fields to include in the response
     * @return this search query
     */
    public SearchQuery includes(String entityName, String... fields) {
        if (includes == null) {
            includes = new HashedMap<>();
        }
        includes.put(entityName, Arrays.asList(fields));
        return this;
    }

    /**
     * Fields to include in the database query.
     * <p>
     * Filters for specific fields on database level.
     * This means that the database only loads the requested fields and not the whole entity.
     * @param entityName the name of the entity for which only the specified fields shall be queries from the database
     * @param fields the names of the fields to query
     * @return this search query
     */
    public SearchQuery fields(String entityName, String... fields) {
        if (this.fields == null) {
            this.fields = new HashedMap<>();
        }
        this.fields.put(entityName, Arrays.asList(fields));
        return this;
    }

    public SearchQuery ids(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    public SearchQuery addSorting(Sorting sort) {
        if (sortings == null) {
            sortings = new ArrayList<>();
        }
        sortings.add(sort);
        return this;
    }

    public SearchQuery addFilter(Filter filter) {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.add(filter);
        return this;
    }

}
