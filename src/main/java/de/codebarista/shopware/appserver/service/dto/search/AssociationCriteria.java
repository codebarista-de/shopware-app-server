package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssociationCriteria {

    @JsonProperty("sort")
    private List<Sorting> sortings = new ArrayList<>();

    @JsonProperty("filter")
    private List<Filter> filters = new ArrayList<>();

    @JsonProperty("associations")
    private Map<String, AssociationCriteria> associations;

    public AssociationCriteria addSorting(Sorting sort) {
        sortings.add(sort);
        return this;
    }

    public AssociationCriteria addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public AssociationCriteria withAssociation(String name, AssociationCriteria criteria) {
        if (associations == null) {
            associations = new HashedMap<>();
        }
        associations.put(name, criteria);
        return this;
    }
}
