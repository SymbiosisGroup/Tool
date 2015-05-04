package symbiosis.meta.traceability;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementFilter;
import symbiosis.project.ProjectRole;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AddRejected")
public class AddRejectedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @ManyToOne
    private ExternalInput rejection;

    public AddRejectedState() {
    }

    AddRejectedState(ExternalInput source, ExternalInput rejection, Reviewable review) {
        super(source, review);
        this.rejection = rejection;
        setReviewImpact(Impact.SERIOUS);
    }

    public String getJustification() {
        return rejection.getJustification();
    }

    @Override
    public void change(ExternalInput source, String previousContent) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("change not allowed in case of ADD_REJECTED");
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        if (source.getFrom().equals(externalInput.getFrom())) {
            reviewable.remove();
        } else {
            throw new ChangeNotAllowedException("approve only allowed by author");
        }
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("remove not allowed in case of ADD_REJECTED");
    }

    @Override
    public void reject(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("reject not allowed in case of ADD_REJECTED");
    }

    @Override
    public void rollBack(ProjectRole projectRole) throws ChangeNotAllowedException {
        if (reviewable.isOwner(projectRole)) {
            reviewable.setReviewState(new AddedState(externalInput, reviewable));
        } else {
            throw new ChangeNotAllowedException("roll Back not allowed by participant "
                    + projectRole.getName() + " in case of ADD_REJECTED");
        }
    }

    @Override
    public String toString() {
        return "ADD_REJ";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return projectRole.getName().equalsIgnoreCase(externalInput.getFrom().getName())
                || reviewable.isOwner(projectRole);

    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof AddRejectedState;
            }

            @Override
            public String toString() {
                return "AddRejected";
            }
        };
    }

}
