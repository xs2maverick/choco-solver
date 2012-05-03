/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation.generator;

import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.propagation.IPropagationEngine;
import solver.propagation.generator.predicate.Predicate;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.recorders.fine.FineArcEventRecorder;
import solver.recorders.fine.FineVarEventRecorder;
import solver.variables.Variable;
import solver.variables.view.IView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static solver.propagation.generator.PrimitiveTools.getVar;
import static solver.propagation.generator.PrimitiveTools.validate;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/03/12
 */
public class PVar implements Generator<AbstractFineEventRecorder> {

    final List<AbstractFineEventRecorder> eventRecorders;

    public PVar(Variable... variables) {
        this(variables, PArc.NOV);
    }

    public PVar(Variable[] variables, Predicate[] validations) {
        super();
        Solver solver = variables[0].getSolver();
        IPropagationEngine propagationEngine = solver.getEngine();
        propagationEngine.prepareWM(solver);
        eventRecorders = new ArrayList<AbstractFineEventRecorder>();
        for (int i = 0; i < variables.length; i++) {
            Variable var = getVar(variables[i]); // deal with view
            if (validations.length == 0 || validate(var, validations)) {
                make(var, validations, propagationEngine, solver);
            }
        }
    }

    private void make(Variable var, Predicate[] validations, IPropagationEngine propagationEngine, Solver solver) {
        int vidx = var.getId();
        Propagator[] propagators = var.getPropagators();
        int[] pindices = var.getPIndices();
        int i = 0;
        int nbp = propagators.length;
        for (int j = 0; j < nbp; j++) {
            Propagator prop = propagators[j];
            int pidx = prop.getId();
            int pos = pindices[j];
            if (propagationEngine.isMarked(vidx, pidx, pos)) {
                if (validations.length == 0 || validate(prop, validations)) {
                    propagationEngine.clearWatermark(vidx, pidx, pos);
                    propagators[i] = prop;
                    pindices[i++] = pindices[j];
                }
            }
        }
        if (i > 0) {
            if (i == 1) { // in that case, there is only one propagator, an Arc is a better alternative
                eventRecorders.add(new FineArcEventRecorder(var, propagators[0], pindices[0], solver));
            } else if (i < nbp) { // if some propagators has been removed -- connectected previously
                eventRecorders.add(new FineVarEventRecorder(var, Arrays.copyOfRange(propagators, 0, i), Arrays.copyOfRange(pindices, 0, i), solver));
            } else {
                eventRecorders.add(new FineVarEventRecorder(var, propagators, pindices, solver));
            }
        }
        IView[] views = var.getViews();
        for (int j = 0; j < views.length; j++) {
            make(views[j], validations, propagationEngine, solver);
        }
    }

    @Override
    public AbstractFineEventRecorder[] getElements() {
        return eventRecorders.toArray(new AbstractFineEventRecorder[eventRecorders.size()]);
    }
}