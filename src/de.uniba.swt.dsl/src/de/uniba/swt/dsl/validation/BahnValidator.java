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
import de.uniba.swt.dsl.validation.typing.HintDataTypeUtl;
import de.uniba.swt.dsl.validation.typing.TypeCheckingTable;
import de.uniba.swt.dsl.validation.util.ValidationException;
import de.uniba.swt.dsl.validation.validators.*;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

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
    UniqueConfigNameValidator uniqueConfigNameValidator;

    @Inject
    UniqueVarIdValidator uniqueVarIdValidator;

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

    @Check
    public void valdiateModel(BahnModel model) {
        hexValidator.clear();
        uniqueConfigNameValidator.clear();
        segmentValidator.clear();
        typeCheckingTable.clear();
        uniqueVarIdValidator.clear();
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
            if (uniqueConfigNameValidator.lookup(name)) {
                error(String.format(ValidationErrors.DefinedConfigNameFormat, name), feature, i);
            } else {
                uniqueConfigNameValidator.insert(name);
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
            // valid id
            ensureValidId(funcDecl.getName(), BahnPackage.Literals.COMPONENT__NAME);

            // defined function
            if (uniqueVarIdValidator.lookupFunc(funcDecl.getName())) {
                error(String.format(ValidationErrors.DefinedFuncFormat, funcDecl.getName()), BahnPackage.Literals.COMPONENT__NAME);
            } else {
                uniqueVarIdValidator.insertFunc(funcDecl.getName());
            }

            // valid
            declValidator.validateReturn(funcDecl);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void checkIdVarDecl(VarDecl varDecl) {
        try {
            ensureValidRefVar(varDecl, BahnPackage.Literals.REF_VAR_DECL__NAME);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void checkIdParamDecl(ParamDecl paramDecl) {
        try {
            ensureValidRefVar(paramDecl, BahnPackage.Literals.REF_VAR_DECL__NAME);
        } catch (ValidationException e) {
            error(e.getMessage(), e.getFeature());
        }
    }

    @Check
    public void typeCheckingSelectionStmt(SelectionStmt stmt) {
        ExprDataType exprType = typeCheckingTable.computeDataType(stmt.getExpr());
        ensureTypesMatched(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.SELECTION_STMT__EXPR);
    }

    @Check
    public void typeCheckingIterationStmt(IterationStmt stmt) {
        ExprDataType exprType = typeCheckingTable.computeDataType(stmt.getExpr());
        ensureTypesMatched(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.SELECTION_STMT__EXPR);
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
        ensureTypesMatched(expectedType, actualType, BahnPackage.Literals.FOREACH_STMT__DECL);
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
    public void typeCheckingArray(ArrayLiteralExpr expr) {
        // ensure all elements in the same type
        if (expr.getArrExprs().size() > 0) {
            var itemType = typeCheckingTable.computeDataType(expr.getArrExprs().get(0));
            for (int i = 1; i < expr.getArrExprs().size(); i++) {
                var arrExpr = expr.getArrExprs().get(i);
                if (typeCheckingTable.canComputeType(arrExpr)) {
                    ExprDataType exprType = typeCheckingTable.computeDataType(arrExpr, HintDataTypeUtl.from(itemType.getDataType()));
                    ensureTypesMatched(itemType, exprType, BahnPackage.Literals.ARRAY_LITERAL_EXPR__ARR_EXPRS, i);
                }
            }
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
        ensureTypesMatched(ExprDataType.ScalarBool, typeCheckingTable.computeDataType(expr.getExpr()), BahnPackage.Literals.UNARY_EXPR__EXPR);

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
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrackExpr()), BahnPackage.Literals.GET_TRACK_STATE_FUNC_EXPR__TRACK_EXPR);
    }

    @Check
    public void typeCheckingGetTrainSpeedFuncExpr(GetTrainSpeedFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.GET_TRAIN_SPEED_FUNC_EXPR__TRAIN_EXPR);
    }

    @Check
    public void typeCheckingGetRoutesFuncExpr(GetRoutesFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getSrcSignalExpr()), BahnPackage.Literals.GET_ROUTES_FUNC_EXPR__SRC_SIGNAL_EXPR);
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getDestSignalExpr()), BahnPackage.Literals.GET_ROUTES_FUNC_EXPR__DEST_SIGNAL_EXPR);
    }

    @Check
    public void typeCheckingGetConfigFuncExpr(GetConfigFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getConfigExpr()), BahnPackage.Literals.GET_CONFIG_FUNC_EXPR__CONFIG_EXPR);
    }

    @Check
    public void typeCheckingSetTrainSpeed(SetTrainSpeedFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.SET_TRAIN_SPEED_FUNC_EXPR__TRAIN_EXPR);
        ensureTypesMatched(ExprDataType.ScalarInt, typeCheckingTable.computeDataType(funcExpr.getSpeedExpr()), BahnPackage.Literals.SET_TRAIN_SPEED_FUNC_EXPR__SPEED_EXPR);
    }

    @Check
    public void typeCheckingSetTrackStateFuncExpr(SetTrackStateFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrackExpr()), BahnPackage.Literals.SET_TRACK_STATE_FUNC_EXPR__TRACK_EXPR);
    }

    @Check
    public void typeCheckingSetConfigFuncExpr(SetConfigFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getConfigExpr()), BahnPackage.Literals.SET_CONFIG_FUNC_EXPR__CONFIG_EXPR);
        if (funcExpr.getProp().getType() == DataType.STRING_TYPE) {
            ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getValueExpr()), BahnPackage.Literals.SET_CONFIG_FUNC_EXPR__VALUE_EXPR);
        }
    }

    @Check
    public void typeCheckingGrantRouteFuncExpr(GrantRouteFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getRouteExpr()), BahnPackage.Literals.GRANT_ROUTE_FUNC_EXPR__ROUTE_EXPR);
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getTrainExpr()), BahnPackage.Literals.GRANT_ROUTE_FUNC_EXPR__TRAIN_EXPR);
    }

    @Check
    public void typeCheckingEvaluateFuncExpr(EvaluateFuncExpr funcExpr) {
        ensureTypesMatched(ExprDataType.ScalarString, typeCheckingTable.computeDataType(funcExpr.getObjectExpr()), BahnPackage.Literals.EVALUATE_FUNC_EXPR__OBJECT_EXPR);
    }

    private void ensureValidRefVar(RefVarDecl varDecl, EStructuralFeature feature) throws ValidationException {
        ensureValidId(varDecl.getName(), feature);

        // find local function
        var funcDecl = BahnUtil.findFuncDecl(varDecl.eContainer());
        if (funcDecl == null)
            return;

        if (uniqueVarIdValidator.lookup(funcDecl, varDecl.getName())) {
            throw new ValidationException(String.format(ValidationErrors.DefinedVariableFormat, varDecl.getName()), feature);
        } else {
            uniqueVarIdValidator.insert(funcDecl, varDecl.getName());
        }
    }

    private void ensureValidId(String id, EStructuralFeature feature) throws ValidationException {
        // ensure not starting with Id
        if (id.startsWith("_")) {
            throw new ValidationException(ValidationErrors.IdUnderscoreNotAllowedBeginning, feature);
        }
    }

    private void ensureTypesMatched(ExprDataType expectedType, ExprDataType actualType, EStructuralFeature feature) {
        ensureTypesMatched(expectedType, actualType, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
    }

    private void ensureTypesMatched(ExprDataType expectedType, ExprDataType actualType, EStructuralFeature feature, int index) {
        if (!expectedType.equals(actualType)) {
            error(ValidationErrors.createTypeErrorMsg(expectedType.displayTypeName(), actualType.displayTypeName()), feature, index);
        }
    }
}
