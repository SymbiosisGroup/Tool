/*
 * Copyright (C) 2015 moridrin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package symbiosis.meta.requirements;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Jeroen Berkvens
 * @company Moridrin
 * @project Symbiosis
 */
public class RequirementFilter {

    private final BooleanProperty selected;
    private final Requirement requirement;
    private final FilterProperty selectedFilterProperty;

    /**
     * Default constructor for the Requirement Filter.
     *
     * @param requirement is the requirement this filter is filtering.
     * @param selectedFilterProperty is the property this filter is filtering.
     */
    public RequirementFilter(Requirement requirement, FilterProperty selectedFilterProperty) {
        selected = new SimpleBooleanProperty();
        this.requirement = requirement;
        this.selectedFilterProperty = selectedFilterProperty;
    }

    /**
     * This this operation binds the 'selected' booleanProperty to the
     * checkBoxValue parameter.
     *
     * @param checkBoxValue is the boolean value of the checkBox it is going to
     * be bind to.
     */
    public void bindSelectedProperty(Property checkBoxValue) {
        this.selected.bindBidirectional(checkBoxValue);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public boolean isSelected() {
        return this.selected.get();
    }

    public Requirement getRequirement() {
        return this.requirement;
    }

    public FilterProperty geFilterProperty() {
        return this.selectedFilterProperty;
    }

    public String getFilter() {
        switch (selectedFilterProperty) {
            case TEXT: {
                return requirement.getText();
            }
            case STATE: {
                return requirement.getReviewState().name();
            }
            case TYPE: {
                return requirement.getReqType();
            }
            case NAME: {
                return requirement.getName();
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.selected);
        hash = 67 * hash + Objects.hashCode(this.requirement);
        hash = 67 * hash + Objects.hashCode(this.selectedFilterProperty);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequirementFilter other = (RequirementFilter) obj;
        return Objects.equals(this.getFilter(), other.getFilter());
    }

    /**
     * This is the FilterProperty. This is used to specify what property this
     * filter is filtering on.
     */
    public enum FilterProperty {

        TEXT,
        STATE,
        TYPE,
        NAME;
    }
}
