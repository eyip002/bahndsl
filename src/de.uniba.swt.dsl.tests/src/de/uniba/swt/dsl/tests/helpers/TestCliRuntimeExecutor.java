/*
 * This file is part of the BahnDSL project, a domain-specific language
 * for configuring and modelling model railways
 *
 * BahnDSL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BahnDSL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BahnDSL.  If not, see <https://www.gnu.org/licenses/>.
 *
 * The following people contributed to the conception and realization of the
 * present BahnDSL (in alphabetic order by surname):
 *
 * - Tri Nguyen <https://github.com/trinnguyen>
 */

package de.uniba.swt.dsl.tests.helpers;

import de.uniba.swt.dsl.common.util.Tuple;
import de.uniba.swt.dsl.generator.externals.CliRuntimeExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake runtime executor for testing external cli execution
 * Always success
 */
public class TestCliRuntimeExecutor extends CliRuntimeExecutor {

    private final List<Tuple<String, String[]>> commands = new ArrayList<>();

    public List<Tuple<String, String[]>> getCommands() {
        return commands;
    }

    @Override
    protected boolean internalExecuteCli(String command, String[] args, String workingDir) {
        commands.add(Tuple.of(command, args));
        return true;
    }

    public void clear() {
        commands.clear();
    }
}
