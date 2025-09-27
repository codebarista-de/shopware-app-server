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

    public SearchQuery includes(String entityName, String... fields) {
        if (includes == null) {
            includes = new HashedMap<>();
        }
        includes.put(entityName, Arrays.asList(fields));
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
