/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropMin extends Propagator<IntVar> {

    private final int n;

    public PropMin(IntVar[] variables, IntVar maxVar) {
        super(ArrayUtils.append(variables, new IntVar[]{maxVar}), PropagatorPriority.LINEAR, false);
        n = variables.length;
        assert n > 0;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean filter;
        do {
            filter = false;
            int lb = Integer.MAX_VALUE;
            int ub = Integer.MAX_VALUE;
            int min = vars[n].getLB();
            // update min
            for (int i = 0; i < n; i++) {
                filter |= vars[i].updateLowerBound(min, this);
                lb = Math.min(lb, vars[i].getLB());
                ub = Math.min(ub, vars[i].getUB());
            }
            filter |= vars[n].updateLowerBound(lb, this);
            filter |= vars[n].updateUpperBound(ub, this);
            ub = Math.min(ub, vars[n].getUB()); // to deal with holes in vars[n] or its instantiation
            // back-propagation
            int c = 0, idx = -1;
            for (int i = 0; i < n; i++) {
                if (vars[i].getLB() > ub) {
                    c++;
                } else {
                    idx = i;
                }
            }
            if (c == vars.length - 2) {
                filter = false;
                vars[idx].updateBounds(vars[n].getLB(), vars[n].getUB(), this);
                if (vars[n].isInstantiated()) {
                    setPassive();
                } else if (vars[idx].hasEnumeratedDomain()) {
                    // for enumerated variables only
                    while (vars[n].getLB() != vars[idx].getLB()
                            || vars[n].getUB() != vars[idx].getUB()) {
                        vars[n].updateBounds(vars[idx].getLB(), vars[idx].getUB(), this);
                        vars[idx].updateBounds(vars[n].getLB(), vars[n].getUB(), this);
                    }
                }
            }
        } while (filter);
    }

    @Override
    public ESat isEntailed() {
        int lb = vars[n].getLB();
        int minLb = vars[0].getLB();
        for (int i = 0; i < n; i++) {
            minLb = Math.min(minLb,vars[i].getLB());
            if (vars[i].getUB() < lb) {
                return ESat.FALSE;
            }
        }
        if(minLb>vars[n].getUB()){
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() < lb) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(lb)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(vars[n]).append(" = min(");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
