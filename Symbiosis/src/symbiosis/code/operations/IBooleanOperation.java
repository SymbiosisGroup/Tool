/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.meta.requirements.RuleRequirement;

/**
 *
 * @author frankpeeters
 */
public interface IBooleanOperation extends IOperation {

    RuleRequirement getRuleRequirement();

    String returnValue();
}
