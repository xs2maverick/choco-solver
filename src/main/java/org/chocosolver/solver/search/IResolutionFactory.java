/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface to define most commonly used resolution procedures.
 * <p>
 * Project: choco-solver.
 *
 * @author Jean-Guillaum Fages
 * @author Charles Prud'homme
 * @since 25/04/2016.
 */
public interface IResolutionFactory extends ISelf<Model> {

    /**
     * Attempts to find a solution of the declared problem.
     * <ul>
     * <li>If the method returns <tt>null</tt>: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before a solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a {@link Solution}:</li>
     * <ul>
     * <li>a solution has been found. This method can be called anew to look for the next solution, if any.</li>
     * </ul>
     * </ul>
     * <p>
     * If a solution has been found, since the search process stops on that solution, variables' value can be read,
     * e.g., {@code intvar.getValue()}
     * or the solution can be recorded:
     * <pre>{@code
     * Solution s = new Solution(model);
     * s.record();
     * }</pre>
     * <p>
     * This method run the following instructions:
     * <pre>
     *     {@code
     *     if(_me().solve()) {
     *          return new Solution(_me()).record();
     *     }else{
     *          return null;
     *       }
     *     }
     * </pre>
     *
     * @return a {@link Solution} if and only if a solution has been found, <tt>null</tt> otherwise.
     */
    default Solution findSolution() {
        if(_me().solve()) {
            return new Solution(_me()).record();
        }else{
            return null;
        }
    }

    /**
     * Attempts to find all solutions of the declared problem.
     * <ul>
     * <li>If the method returns an empty list: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before any solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all solutions have been found.</li>
     * </ul>
     * </ul>
     * <p>
     * This method run the following instructions:
     * <pre>
     *     {@code
     *     List<Solution> solutions = new ArrayList<>();
     *     while (model.solve()){
     *          solutions.add(new Solution(model).record());
     *     }
     *     return solutions;
     *     }
     * </pre>
     *
     * @return a list that contained the found solutions.
     */
    default List<Solution> findAllSolutions() {
        List<Solution> solutions = new ArrayList<>();
        while (_me().solve()) {
            solutions.add(new Solution(_me()).record());
        }
        return solutions;
    }

    /**
     * Attempt to find the solution that optimizes the mono-objective problem defined by
     * a unique objective variable and an optimization criteria.
     * <ul>
     * <li>If this method returns <i>null</i>:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion (e.g., a time limit)
     * and no solution has been found so far,</li>
     * <li>or the problem cannot be satisfied (i.e., over constrained).</li>
     * </ul>
     * <li>If this method returns a {@link Solution}:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion and the solution is the best found so far
     * but there is no guarantee that it is the optimal one,</li>
     * <li>or it is the optimal one.</li>
     * </ul>
     * </ul>
     * <p>
     * This method run the following instructions:
     * <pre>
     *     {@code
     *     model.setObjective(maximize ? ResolutionPolicy.MAXIMIZE : ResolutionPolicy.MINIMIZE, objective);
     *     Solution s = new Solution(model);
     *     while (model.solve()) {
     *          s.record();
     *     }
     *     return model.getSolver().isFeasible() == ESat.TRUE ? s : null;
     *     }
     * </pre>
     *
     * @param objective integer variable to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem,
     *                  set to <tt>false</tt> to solve a minimization problem.
     * @return <ul>
     * <li>
     * <tt>null</tt> if the problem has no solution or a stop criterion stops the search before finding a first solution
     * </li>
     * <li>a {@link Solution} if at least one solution has been found. The solution is proven to be optimal if no stop criterion stops the search.</li>
     * </ul>
     */
    default Solution findOptimalSolution(IntVar objective, boolean maximize) {
        _me().setObjective(maximize ? ResolutionPolicy.MAXIMIZE : ResolutionPolicy.MINIMIZE, objective);
        Solution s = new Solution(_me());
        while (_me().solve()) {
            s.record();
        }
        return _me().getSolver().isFeasible() == ESat.TRUE ? s : null;
    }

    /**
     * Attempt to find the solution that optimizes the mono-objective problem defined by
     * a unique objective variable and an optimization criteria, then finds and stores all optimal solution.
     * This method works as follow:
     * <ol>
     * <li>It finds and prove the optimum</li>
     * <li>It resets the search and enumerates all solutions of optimal cost</li>
     * </ol>
     * Note that the returned list can be empty.
     * <ul>
     * <li>If the method returns an empty list: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before any solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all optimal solutions have been found.</li>
     * </ul>
     * </ul>
     * <p>
     * This method run the following instructions:
     * <pre>
     *     {@code
     *     _me().findOptimalSolution(objective, maximize);
     *     if (model.getSolver().getMeasures().getSolutionCount() > 0) {
     *         int opt = _model.getSolver().getObjectiveManager().getBestSolutionValue().intValue();
     *         model.getSolver().reset();
     *         model.clearObjective();
     *         model.arithm(objective, "=", opt).post();
     *         return findAllSolutions();
     *     } else {
     *          return Collections.emptyList();
     *     }
     *     }
     * </pre>
     *
     * @param objective the variable to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem,
     *                  set to <tt>false</tt> to solve a minimization problem.
     * @return a list that contained the solutions found.
     */
    default List<Solution> findAllOptimalSolutions(IntVar objective, boolean maximize) {
        _me().findOptimalSolution(objective, maximize);
        if (_me().getSolver().getMeasures().getSolutionCount() > 0) {
            int opt = _me().getSolver().getObjectiveManager().getBestSolutionValue().intValue();
            _me().getSolver().reset();
            _me().clearObjective();
            _me().arithm(objective, "=", opt).post();
            return findAllSolutions();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Attempts optimize the value of the <i>objectives</i> variable w.r.t. to an optimization criteria.
     * Finds and stores all optimal solution.
     * Note that the returned list can be empty.
     * <ul>
     * <li>If the method returns an empty list: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before any solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all optimal solutions have been found.</li>
     * </ul>
     * </ul>
     *
     *  This method run the following instructions:
     * <pre>
     *     {@code
     *     ParetoOptimizer pareto = new ParetoOptimizer(maximize ? ResolutionPolicy.MAXIMIZE : ResolutionPolicy.MINIMIZE, objectives);
     *     while (_me().solve()){
     *          pareto.onSolution();
     *     }
     *     return pareto.getParetoFront();
     *     }
     * </pre>
     *
     * @param objectives the array of variables to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem,
     *                  set to <tt>false</tt> to solve a minimization problem.
     * @return a list that contained the solutions found.
     */
    default List<Solution> findParetoFront(IntVar[] objectives, boolean maximize) {
        ParetoOptimizer pareto = new ParetoOptimizer(maximize ? ResolutionPolicy.MAXIMIZE : ResolutionPolicy.MINIMIZE, objectives);
        while (_me().solve()){
            pareto.onSolution();
        }
        return pareto.getParetoFront();
    }

}