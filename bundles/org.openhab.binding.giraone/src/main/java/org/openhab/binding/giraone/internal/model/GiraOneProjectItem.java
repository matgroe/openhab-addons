package org.openhab.binding.giraone.internal.model;

import java.util.ArrayList;
import java.util.Collection;

public class GiraOneProjectItem {
    private GiraOneItemMainType mainType;
    private GiraOneItemSubType subType;
    private String name;
    private String urn;
    private Collection<GiraOneProjectItem> children = new ArrayList<>();
    private Collection<GiraOneItemReference> itemReferences = new ArrayList<>();

    public GiraOneItemMainType getMainType() {
        return mainType;
    }

    public GiraOneItemSubType getSubType() {
        return subType;
    }

    public String getName() {
        return name;
    }

    public String getUrn() {
        return urn;
    }

    public Collection<GiraOneProjectItem> getChildren() {
        return children;
    }

    public boolean isReferencing(GiraOneItemReference ref) {
        return this.itemReferences.stream().anyMatch(f -> f.getReferenceId() == ref.getReferenceId());
    }

    public Collection<GiraOneItemReference> getItemReferences() {
        return itemReferences;
    }

    @Override
    public String toString() {
        return String.format("%s(%s):{children=%d, itemReferences=%d}", getClass().getSimpleName(), urn,
                children.size(), itemReferences.size());
    }
}
