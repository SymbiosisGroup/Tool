package symbiosis.meta.requirements;

import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.objectmodel.FactType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.Tuple;
import symbiosis.meta.objectmodel.Type;
import symbiosis.meta.traceability.Category;
import symbiosis.meta.traceability.ExternalInput;
import symbiosis.meta.traceability.Impact;
import symbiosis.meta.traceability.ModelElement;
import symbiosis.meta.traceability.SynchronizationMediator;
import symbiosis.util.Naming;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author FrankP
 */
@Entity
@DiscriminatorValue("Fact")
public class FactRequirement extends Requirement {

    private static final long serialVersionUID = 1L;
    @ManyToOne
    private RequirementModel model;

    /**
     * Constructor; it is entirelly based on the constructor of the Requirement
     * class.
     *
     * @param nr number of requirement.
     * @param cat (category) of requirement.
     * @param text of requirement.
     * @param source of requirement, which is of external input.
     * @param parent of requirement, which is the requirements model.
     */
    FactRequirement(int nr, Category cat, String text,
        ExternalInput source, RequirementModel parent) {
        super(nr, cat, text, source, parent, "Fact");
    }

    /**
     * If the dependents of this fact-requirement-object have no instance of
     * Tuple, null is returned.
     *
     * @return tuple, which sould be a dependent of this fact-requirement-object
     */
    public Tuple getRealizedTuple() {
        List<ModelElement> modelElements = this.dependents();
        for (ModelElement modelElement : modelElements) {
            if (modelElement instanceof Tuple) {
                Tuple t = (Tuple) modelElement;
                if (!t.getFactType().isObjectType()) {

                    return t;

                }
//                if (!t.getFactType().isObjectType()) {
//                    tuple = t;
//                } else if (tuple == null && t.getFactType().isPureFactType()) {
//                    tuple = t;
//                }
            }
        }
        return null;
    }

    /**
     * @return 2, always.
     */
    @Override
    int order() {
        return 2;
    }

    /**
     * @return true if fact has been decomposed else false
     */
    @Override
    public boolean isRealized() {
        Tuple tuple = getRealizedTuple();
        return tuple != null;
    }

    @Override
    public String getText() {
        if (getCategory() == Category.SYSTEM) {
            return text;
        }
        Tuple tuple = getRealizedTuple();
        if (tuple != null) {
            FactType ft = tuple.getFactType();
            if (ft.getFTE() == null) {
                System.out.println(getId() + " " + ft.getName() + " without FTE");
                return text;
            } else {
                return Naming.withCapital(ft.getFTE().makeExpression(tuple));
            }
        } else {
            return text;
        }
    }

    /**
     * The evaluation of the change of text is done only if the current text and
     * the input text are not String.equal(...).
     *
     * @param source of external input.
     * @param text of requirement.
     * @throws ChangeNotAllowedException if the (trimmed) text is empty.
     */
    @Override
    public void setText(ExternalInput source, String text) throws ChangeNotAllowedException {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new ChangeNotAllowedException("text of requirement may not be empty");
        }

        if (!trimmedText.equals(this.text)) {
            RequirementModel rm = (RequirementModel) getParent();
            Requirement req = rm.searchFor(trimmedText);
            if (req == null) {
                if (!trimmedText.equalsIgnoreCase(text)) {
                    getReviewState().change(source, getText());
                    getReviewState().setReviewImpact(Impact.LIGHT);
                }
                this.text = trimmedText;
            }
        }
    }

    /**
     * @return "Fact" as kind of requirement, always.
     */
    @Override
    public String getReqType() {
        return "Fact";
    }

    public RequirementModel getModel() {
        return model;
    }

    public void setModel(RequirementModel model) {
        this.model = model;
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return (requirement instanceof FactRequirement);
            }

            @Override
            public String toString() {
                return "Fact";
            }
        };
    }

    @Override
    public String toString() {
        return getText();
    }

    @Override
    protected void removeDependentMediators() {
        SynchronizationMediator mediator = getDependentTupleMediator();

        if (mediator == null) {
            System.out.println("tuple of requirement " + getId() + " doesn't exist");
        } else {
            mediator.removeForward();
            setModifiedAtToNow();
        }
    }

}
