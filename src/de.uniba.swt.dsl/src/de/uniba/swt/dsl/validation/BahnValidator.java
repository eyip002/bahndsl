/*
 * generated by Xtext 2.20.0
 */
package de.uniba.swt.dsl.validation;


import de.uniba.swt.dsl.bahn.*;
import de.uniba.swt.dsl.common.layout.models.CompositeLayoutException;
import de.uniba.swt.dsl.common.layout.models.LayoutException;
import de.uniba.swt.dsl.common.layout.validators.LayoutElementValidator;
import de.uniba.swt.dsl.common.util.BahnUtil;
import de.uniba.swt.dsl.common.util.Tuple;
import de.uniba.swt.dsl.normalization.SyntacticTransformer;
import de.uniba.swt.dsl.validation.typing.ExprDataType;
import de.uniba.swt.dsl.validation.typing.TypeCheckingTable;
import de.uniba.swt.dsl.validation.util.ValidationException;
import de.uniba.swt.dsl.validation.validators.*;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains custom validation rules.
 * <p>
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class BahnValidator extends AbstractBahnValidator {

    private static final Logger logger = Logger.getLogger(BahnValidator.class);

    @Inject
    BahnLayoutValidator layoutValidator;

    @Inject
    UniqueSegmentUsageValidator segmentValidator;

    @Inject
    UniqueHexValidator hexValidator;

    @Inject
    UniqueIdValidator uniqueIdValidator;

    @Inject
    BoardValidator boardValidator;

    @Inject
    DeclValidator declValidator;

    @Inject
    TypeCheckingTable typeCheckingTable;

    @Inject
    SyntacticTransformer syntacticTransformer;

    @Inject
    ExpressionValidator expressionValidator;

    @Inject
    StatementValidator statementValidator;

    @Override
    public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
        typeCheckingTable.clear();
        hexValidator.clear();
        uniqueIdValidator.clear();
        segmentValidator.clear();
        return super.validate(eDataType, value, diagnostics, context);
    }

    @Check
    public void validateRootModule(RootModule module) {
        // ensure single board
        var errors = boardValidator.findSingleSectionError(module);
        for (Tuple<String, Integer> error : errors) {
            error(error.getFirst(), BahnPackage.Literals.ROOT_MODULE__PROPERTIES, error.getSecond());
        }

        // single track by board
        var boardErrors = boardValidator.findSingleTrackByBoardErrors(module);
        for (Tuple<String, Integer> error : boardErrors) {
            error(error.getFirst(), BahnPackage.Literals.ROOT_MODULE__PROPERTIES, error.getSecond());
        }
    }

    @Check
    public void validateBoards(BoardsProperty boardsProperty) {
        // ensure no duplicated address in board
        var errors = boardValidator.validateUniqueHex(boardsProperty.getItems(), BoardElement::getUniqueId);
        for (Tuple<String, Integer> error : errors) {
            error(error.getFirst(), BahnPackage.Literals.BOARDS_PROPERTY__ITEMS, error.getSecond());
        }

        // ensure no duplicated id
        validateUniqueName(boardsProperty.getItems(), BoardElement::getName, BahnPackage.Literals.BOARDS_PROPERTY__ITEMS);
    }

    @Check
    public void validateSegmentsProperty(SegmentsProperty prop) {
        validateUniqueHexInBoard(prop.getBoard().getName(), prop.getItems(), SegmentElement::getAddress, BahnPackage.Literals.SEGMENTS_PROPERTY__ITEMS);
        validateUniqueName(prop.getItems(), SegmentElement::getName, BahnPackage.Literals.SEGMENTS_PROPERTY__ITEMS);
    }

    @Check
    public void validateSignalsProperty(SignalsProperty prop) {
        validateUniqueHexInBoard(prop.getBoard().getName(), prop.getItems(), p -> {
            if (p instanceof RegularSignalElement) {
                return ((RegularSignalElement) p).getNumber();
            }

            return null;
        }, BahnPackage.Literals.SIGNALS_PROPERTY__ITEMS);
        validateUniqueName(prop.getItems(), SignalElement::getName, BahnPackage.Literals.SIGNALS_PROPERTY__ITEMS);
    }

    @Check
    public void validatePeripheralsProperty(PeripheralsProperty prop) {
        validateUniqueHexInBoard(prop.getBoard().getName(), prop.getItems(), p -> {
            if (p instanceof RegularSignalElement) {
                return ((RegularSignalElement) p).getNumber();
            }

            return null;
        }, BahnPackage.Literals.SIGNALS_PROPERTY__ITEMS);
        validateUniqueName(prop.getItems(), SignalElement::getName, BahnPackage.Literals.PERIPHERALS_PROPERTY__ITEMS);
        validateUniqueName(prop.getItems(), SignalElement::getName, BahnPackage.Literals.PERIPHERALS_PROPERTY__ITEMS);
    }

    @Check
    public void validatePointsProperty(PointsProperty prop) {
        validateUniqueHexInBoard(prop.getBoard().getName(), prop.getItems(), PointElement::getNumber, BahnPackage.Literals.POINTS_PROPERTY__ITEMS);
        validateUniqueName(prop.getItems(), PointElement::getName, BahnPackage.Literals.POINTS_PROPERTY__ITEMS);
    }

    @Check
    public void validateBlocksProperty(BlocksProperty prop) {
        validateUniqueName(prop.getItems(), BlockElement::getName, BahnPackage.Literals.BLOCKS_PROPERTY__ITEMS);
    }

    @Check
    public void validateBlocksProperty(PlatformsProperty prop) {
        validateUniqueName(prop.getItems(), BlockElement::getName, BahnPackage.Literals.PLATFORMS_PROPERTY__ITEMS);
    }

    @Check
    public void validateBlocksProperty(CrossingsProperty prop) {
        validateUniqueName(prop.getItems(), CrossingElement::getName, BahnPackage.Literals.CROSSINGS_PROPERTY__ITEMS);
    }

    @Check
    public void validateBlocksProperty(TrainsProperty prop) {
        validateUniqueName(prop.getItems(), TrainElement::getName, BahnPackage.Literals.TRAINS_PROPERTY__ITEMS);
    }

    private <T> void validateUniqueHexInBoard(String boardName, List<T> items, Function<T, String> addrMapper, EStructuralFeature feature) {
        var errors = hexValidator.validateUniqueAddress(boardName, items, addrMapper);
        for (Tuple<String, Integer> error : errors) {
            error(error.getFirst(), feature, error.getSecond());
        }
    }

    private <T> void validateUniqueName(List<T> items, Function<T, String> provider, EStructuralFeature feature) {
        for (int i = 0; i < items.size(); i++) {
            var name = provider.apply(items.get(i));
            if (uniqueIdValidator.lookup(name)) {
                error(String.format(ValidationErrors.DefinedConfigNameFormat, name), feature, i);
            } else {
                uniqueIdValidator.insert(name);
            }
        }
    }

    @Check
    public void validatePointElement(PointElement pointElement) {
        try {
            segmentValidator.validateSegment(pointElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateBlockElement(BlockElement blockElement) {
        try {
            segmentValidator.validateSegment(blockElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateCrossingElement(CrossingElement crossingElement) {
        try {
            segmentValidator.validateSegment(crossingElement);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void validateCompositionSignalElement(CompositionSignalElement element) {
        Set<String> usedNames = new HashSet<>();
        Set<String> usedTypes = new HashSet<>();
        for (int i = 0; i < element.getSignals().size(); i++) {
            var signal = element.getSignals().get(i);

            // check names
            if (usedNames.contains(signal.getName())) {
                error(String.format(ValidationErrors.UsedSignalInCompositionFormat, signal.getName()), BahnPackage.Literals.COMPOSITION_SIGNAL_ELEMENT__SIGNALS, i);
            } else {
                usedNames.add(signal.getName());
            }

            // check type
            if (usedTypes.contains(signal.getType().getName())) {
                error(String.format(ValidationErrors.UsedSignalTypeInCompositionFormat, signal.getType().getName()), BahnPackage.Literals.COMPOSITION_SIGNAL_ELEMENT__SIGNALS, i);
            } else {
                usedTypes.add(signal.getType().getName());
            }
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
    public void typeCheckingFuncDecl(FuncDecl funcDecl) {
        logger.debug("typeCheckingFuncDecl: " + funcDecl.getClass().getSimpleName());
        try {
            ensureValidId(funcDecl.getName(), BahnPackage.Literals.COMPONENT__NAME);
            declValidator.validateFuncDecl(funcDecl);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void checkIdVarDecl(VarDecl varDecl) {
        try {
            ensureValidId(varDecl.getName(), BahnPackage.Literals.REF_VAR_DECL__NAME);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void checkIdVarDecl(ParamDecl paramDecl) {
        try {
            ensureValidId(paramDecl.getName(), BahnPackage.Literals.REF_VAR_DECL__NAME);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingSelectionStmt(SelectionStmt stmt) {
        ExprDataType exprType = typeCheckingTable.computeDataType(stmt.getExpr());
        try {
            checkTypes(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.SELECTION_STMT__EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingIterationStmt(IterationStmt stmt) {
        ExprDataType exprType = typeCheckingTable.computeDataType(stmt.getExpr());
        try {
            checkTypes(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.SELECTION_STMT__EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingForeachStmt(ForeachStmt stmt) {

        // ensure array is selected
        var arrayType = typeCheckingTable.computeDataType(stmt.getArrayExpr());
        var ensureArray = arrayType.isArray();
        if (!ensureArray) {
            error(ValidationErrors.TypeExpectedArray, BahnPackage.Literals.FOREACH_STMT__ARRAY_EXPR);
        }

        // ensure current element is matched
        var expectedType = new ExprDataType(arrayType.getDataType(), false);
        var actualType = new ExprDataType(stmt.getDecl().getType(), stmt.getDecl().isArray());
        try {
            checkTypes(expectedType, actualType, BahnPackage.Literals.FOREACH_STMT__DECL);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingBreakStmt(BreakStmt stmt) {
        // ensure having while outside
        if (!BahnUtil.isInsideIterationStmt(stmt)) {
            error(ValidationErrors.BreakInIteration, null);
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
    public void typeCheckingValuedReferenceExpr(ValuedReferenceExpr expr) {
        try {
            expressionValidator.validateValuedReferenceExpr(expr);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingRegularFunctionCallExpr(RegularFunctionCallExpr expr) {
        try {
            expressionValidator.validateRegularFuncCall(expr);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingOpExpression(OpExpression expr) {
        try {
            if (expr.getLeftExpr() != null && expr.getRightExpr() != null) {
                expressionValidator.validateOpExpression(expr);
            }
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingValuedReferenceExpr(UnaryExpr expr) {
        try {
            checkTypes(ExprDataType.ScalarBool, typeCheckingTable.computeDataType(expr.getExpr()), BahnPackage.Literals.UNARY_EXPR__EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void checkArrayGetter(BehaviourGetExpr expr) {
        if (syntacticTransformer.isArrayGetter(expr)) {
            if (!(expr.eContainer() instanceof VariableAssignment)) {
                error(ValidationErrors.MissingArrayAssignment, BahnPackage.Literals.BEHAVIOUR_GET_EXPR__GET_EXPR);
            }
        }
    }

    @Check
    public void typeCheckingGetTrackStateFuncExpr(GetTrackStateFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrackExpr()), BahnPackage.Literals.GET_TRACK_STATE_FUNC_EXPR__TRACK_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingGetTrainSpeedFuncExpr(GetTrainSpeedFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.GET_TRAIN_SPEED_FUNC_EXPR__TRAIN_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingGetRoutesFuncExpr(GetRoutesFuncExpr funcExpr) {
        try {
            // type checking
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getSrcSignalExpr()), BahnPackage.Literals.GET_ROUTES_FUNC_EXPR__SRC_SIGNAL_EXPR);
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getDestSignalExpr()), BahnPackage.Literals.GET_ROUTES_FUNC_EXPR__DEST_SIGNAL_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingGetConfigFuncExpr(GetConfigFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getConfigExpr()), BahnPackage.Literals.GET_CONFIG_FUNC_EXPR__CONFIG_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingSetTrainSpeed(SetTrainSpeedFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.SET_TRAIN_SPEED_FUNC_EXPR__TRAIN_EXPR);
            checkTypes(ExprDataType.ScalarInt, typeCheckingTable.computeDataType(funcExpr.getSpeedExpr()), BahnPackage.Literals.SET_TRAIN_SPEED_FUNC_EXPR__SPEED_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingSetTrackStateFuncExpr(SetTrackStateFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrackExpr()), BahnPackage.Literals.SET_TRACK_STATE_FUNC_EXPR__TRACK_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingSetConfigFuncExpr(SetConfigFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getConfigExpr()), BahnPackage.Literals.SET_CONFIG_FUNC_EXPR__CONFIG_EXPR);
            if (funcExpr.getProp().getType() == DataType.STRING_TYPE) {
                checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getValueExpr()), BahnPackage.Literals.SET_CONFIG_FUNC_EXPR__VALUE_EXPR);
            }
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingGrantRouteFuncExpr(GrantRouteFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getRouteExpr()), BahnPackage.Literals.GRANT_ROUTE_FUNC_EXPR__ROUTE_EXPR);
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.GRANT_ROUTE_FUNC_EXPR__TRAIN_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingEvaluateFuncExpr(EvaluateFuncExpr funcExpr) {
        try {
            checkTypes(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getObjectExpr()), BahnPackage.Literals.EVALUATE_FUNC_EXPR__OBJECT_EXPR);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    private static void ensureValidId(String id, EStructuralFeature feature) throws ValidationException {
        if (id.startsWith("_")) {
            throw new ValidationException(ValidationErrors.IdUnderscoreNotAllowedBeginning, feature);
        }
    }

    private static void checkTypes(ExprDataType expectedType, ExprDataType actualType, EStructuralFeature feature) throws ValidationException {
        if (!expectedType.equals(actualType)) {
            throw ValidationException.createTypeException(expectedType, actualType, feature);
        }
    }
}
