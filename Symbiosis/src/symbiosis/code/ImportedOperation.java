/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code;

/**
 *
 * @author frankpeeters
 */
public class ImportedOperation {
    private final OperationHeader oh;
    private final IndentedList api;
    private final IndentedList body;

    public ImportedOperation(OperationHeader oh, IndentedList api, IndentedList body) {
        this.oh = oh;
        this.api = api;
        this.body = body;
    }

    public OperationHeader getOh() {
        return oh;
    }

    public IndentedList getApi() {
        return api;
    }

    public IndentedList getBody() {
        return body;
    }
    
}
