package equa.meta.requirements;

import equa.meta.ChangeNotAllowedException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Tuple;
import equa.meta.objectmodel.Type;
import equa.meta.traceability.ApprovedState;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.SynchronizationMediator;
import equa.project.Project;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public FactRequirement() {
    }

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
        super(nr, cat, text, source, parent);
    }

    /**
     * If the dependents of this fact-requirement-object have no instance of
     * Tuple, null is returned.
     *
     * @return tuple, which should be a dependent of this
     * fact-requirement-object
     */
    public Tuple getRealizedTuple() {
        List<ModelElement> modelElements = this.dependents();
        if (modelElements.isEmpty()) {
            return null;
        } else {
            return (Tuple) modelElements.get(0);
        }

//        for (ModelElement modelElement : modelElements) {
//            if (modelElement instanceof Tuple) {
//                Tuple t = (Tuple) modelElement;
//                if (t.getFactType().isPureFactType()) {
//                    return t;
//                }
//            }
//        }
//        // fact was an object expression; the last one will refer to the object expression 
//        for (int i = modelElements.size() - 1; i >= 0; i--) {
//            if (modelElements.get(i) instanceof Tuple) {
//                return (Tuple) modelElements.get(i);
//            }
//        }
//        return null;
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
        if (getCategory().equals(Category.SYSTEM)) {
            return true;
        }
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
                //    System.out.println(getId() + " " + ft.getName() + " without FTE");
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
        List<SynchronizationMediator> copy = new ArrayList<>(mediators);
        for (SynchronizationMediator mediator : copy) {
            mediator.removeForward();
        }
        mediators.clear();


        setModifiedAtToNow();
    }

   

    protected void removeDependentMediator(SynchronizationMediator dependentMediator) {
        if (!mediators.contains(dependentMediator)) {
            System.out.println("remove mediator " + dependentMediator.getSource().getClass());
        } else {
            mediators.remove(dependentMediator);

            setModifiedAtToNow();
        }
    }

}
