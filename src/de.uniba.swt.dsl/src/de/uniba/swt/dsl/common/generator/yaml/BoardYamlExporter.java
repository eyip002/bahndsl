/*
 *
 * Copyright (C) 2020 University of Bamberg, Software Technologies Research Group
 * <https://www.uni-bamberg.de/>, <http://www.swt-bamberg.de/>
 *
 * This file is part of the BahnDSL project, a domain-specific language
 * for configuring and modelling model railways.
 *
 * BahnDSL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BahnDSL is a RESEARCH PROTOTYPE and distributed WITHOUT ANY WARRANTY, without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * The following people contributed to the conception and realization of the
 * present BahnDSL (in alphabetic order by surname):
 *
 * - Tri Nguyen <https://github.com/trinnguyen>
 *
 */

package de.uniba.swt.dsl.common.generator.yaml;

import de.uniba.swt.dsl.bahn.BoardElement;
import de.uniba.swt.dsl.bahn.BoardsProperty;
import de.uniba.swt.dsl.bahn.RootModule;
import de.uniba.swt.dsl.common.generator.yaml.exports.ElementExporterFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class BoardYamlExporter extends AbstractBidibYamlExporter {

    @Override
    protected String getHeaderComment() {
        return "BiDiB board configuration";
    }

    @Override
    protected void exportContent(RootModule rootModule) {
        List<BoardElement> boards = rootModule.getProperties().stream().filter(p -> p instanceof BoardsProperty).map(p -> ((BoardsProperty) p).getItems()).flatMap(Collection::stream).collect(Collectors.toList());
        appendLine("boards:");

        for (BoardElement board : boards) {
            increaseLevel();
            ElementExporterFactory.build(this, board);
            decreaseLevel();
        }
    }
}