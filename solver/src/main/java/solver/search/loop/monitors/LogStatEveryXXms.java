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

package solver.search.loop.monitors;

import solver.search.loop.AbstractSearchLoop;

/**
 * A search monitor logger which prints statistics every XX ms.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 ao�t 2010
 */
class LogStatEveryXXms extends Thread implements ISearchMonitor {

    private final long duration;

    private final AbstractSearchLoop searchloop;

    public LogStatEveryXXms(AbstractSearchLoop searchloop, long duration) {
        this.searchloop = searchloop;
        this.duration = duration;
        this.setDaemon(true);
    }


    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        try {
            long sleep = duration;
            Thread.sleep(sleep);
            do {
                searchloop.getMeasures().updateTimeCount();
                searchloop.getMeasures().updatePropagationCount();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(">> {}", searchloop.getMeasures().toOneLineString());
                }
                Thread.sleep(sleep);
            } while (true);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
        this.start();
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
    }

    @Override
    public void beforeOpenNode() {
    }

    @Override
    public void afterOpenNode() {
    }

    @Override
    public void onSolution() {
    }

    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
    }

    @Override
    public void onContradiction() {
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
    }

    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
    }
}