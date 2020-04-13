/*
 * generated by Xtext 2.20.0
 */
package de.uniba.swt.dsl.validation;


import de.uniba.swt.dsl.bahn.*;
import de.uniba.swt.dsl.common.layout.models.CompositeLayoutException;
import de.uniba.swt.dsl.common.layout.models.LayoutException;
import de.uniba.swt.dsl.common.layout.validators.LayoutElementValidator;
import de.uniba.swt.dsl.common.util.BahnConstants;
import de.uniba.swt.dsl.validation.typing.TypeCheckingTable;
import de.uniba.swt.dsl.validation.util.ValidationException;
import de.uniba.swt.dsl.validation.validators.*;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.xtext.validation.Check;

import javax.inject.Inject;
import java.util.Map;

/**
 * This class contains custom validation rules.
 * <p>
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class BahnValidator extends AbstractBahnValidator {

    private static Logger logger = Logger.getLogger(BahnValidator.class);

    @Inject
    BahnLayoutValidator layoutValidator;

    @Inject
    UniqueSegmentValidator segmentValidator;

    @Inject
    UniqueHexValidator hexValidator;

    @Inject
    BoardRefValidator boardRefValidator;

    @Inject
    DeclValidator declValidator;

    @Inject
    TypeCheckingTable typeCheckingTable;

    @Inject
    ExpressionValidator expressionValidator;

    @Inject
    StatementValidator statementValidator;

    @Override
    public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
        typeCheckingTable.clear();
        return super.validate(eDataType, value, diagnostics, context);
    }

    @Check
    public void typeCheckingExpression(Expression expression) {
        logger.debug("expression: " + expression.getClass().getSimpleName());
        try {
            expressionValidator.validate(expression);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingStatement(Statement statement) {
        logger.debug("typeCheckingStatement: " + statement.getClass().getSimpleName());
        try {
            statementValidator.validate(statement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingVariableAssignment(VariableAssignment assignment) {
        logger.debug("typeCheckingVariableAssignment: " + assignment.getClass().getSimpleName());
        try {
            statementValidator.validateAssignment(assignment);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature(), e.getIndex());
        }
    }

    @Check
    public void typeCheckingFuncDecl(FuncDecl funcDecl) {
        logger.debug("typeCheckingFuncDecl: " + funcDecl.getClass().getSimpleName());
        try {
            declValidator.validateFuncDecl(funcDecl);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateLayoutElement(LayoutElement element) {
        logger.debug("validateLayoutElement: " + element.getClass().getSimpleName());
        try {
            LayoutElementValidator.validateElement(element);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }
    
    @Check
    public void validateLayoutReference(LayoutReference reference) {
        logger.debug("validateLayoutReference: " + reference.getClass().getSimpleName());
        try {
            LayoutElementValidator.validateReference(reference);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateNetworkLayout(LayoutProperty layoutProperty) {
        logger.debug("validateNetworkLayout: " + layoutProperty.getClass().getSimpleName());
        try {
            layoutValidator.validateLayout(layoutProperty);
        } catch (CompositeLayoutException compositeExp) {
            for (LayoutException exp : compositeExp.getExceptions()) {
                error(exp.getMessage(), BahnPackage.Literals.LAYOUT_PROPERTY__ITEMS);
            }
        }
    }

    @Check
    public void validateSegmentsProperty(SegmentsProperty segmentsProperty) {
        logger.debug("validateSegmentsProperty: " + segmentsProperty.getClass().getSimpleName());
        try {
            hexValidator.validateUniqueAddress(segmentsProperty.getItems(), SegmentElement::getAddress);
            boardRefValidator.validateBoard(segmentsProperty, segmentsProperty.getBoard());
        } catch (Exception e) {
            error(e.getMessage(), BahnPackage.Literals.SEGMENTS_PROPERTY__ITEMS);
        }
    }

    @Check
    public void validateSignalsProperty(SignalsProperty signalsProperty) {
        try {
            hexValidator.validateUniqueAddress(signalsProperty.getItems(), SignalElement::getNumber);
            boardRefValidator.validateBoard(signalsProperty, signalsProperty.getBoard());
        } catch (Exception e) {
            error(e.getMessage(), BahnPackage.Literals.SIGNALS_PROPERTY__ITEMS);
        }
    }

    @Check
    public void validatePointsProperty(PointsProperty pointsProperty) {
        try {
            hexValidator.validateUniqueAddress(pointsProperty.getItems(), PointElement::getNumber);
            boardRefValidator.validateBoard(pointsProperty, pointsProperty.getBoard());
        } catch (Exception e) {
            error(e.getMessage(), BahnPackage.Literals.POINTS_PROPERTY__ITEMS);
        }
    }

    @Check
    public void validatePoint(PointElement pointElement) {
        try {
            segmentValidator.validateSegment(pointElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validatePoint(BlockElement blockElement) {
        try {
            segmentValidator.validateSegment(blockElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateCrossing(CrossingElement crossingElement) {
        try {
            segmentValidator.validateSegment(crossingElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateRootModule(RootModule module) {
        int countBlock = 0;
        int countLayout = 0;
        int countCrossing = 0;

        int invalidBlockIdx = -1;
        int invalidLayoutIdx = -1;
        int invalidCrossingIdx = -1;

        for (int i = 0; i < module.getProperties().size(); i++) {
            var prop = module.getProperties().get(i);
            if (prop instanceof BlocksProperty) {
                countBlock ++;
                if (countBlock > 1) {
                    invalidBlockIdx = i;
                    continue;
                }
            }

            if (prop instanceof LayoutProperty) {
                countLayout++;
                if (countLayout > 1) {
                    invalidLayoutIdx = i;
                    continue;
                }
            }

            if (prop instanceof CrossingsProperty) {
                countCrossing++;
                if (countCrossing > 1) {
                    invalidCrossingIdx = i;
                }
            }
        }

        // show error
        if (invalidBlockIdx >= 0) {
            error("Only one blocks section is allowed", BahnPackage.Literals.ROOT_MODULE__PROPERTIES, invalidBlockIdx);
        }

        if (invalidLayoutIdx >= 0) {
            error("Only one layout section is allowed", BahnPackage.Literals.ROOT_MODULE__PROPERTIES, invalidLayoutIdx);
        }

        if (invalidCrossingIdx >= 0) {
            error("Only one crossing section is allowed", BahnPackage.Literals.ROOT_MODULE__PROPERTIES, invalidCrossingIdx);
        }
    }
}
