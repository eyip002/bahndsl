package de.uniba.swt.dsl.validation.validators;

import de.uniba.swt.dsl.bahn.*;
import de.uniba.swt.dsl.common.util.BahnUtil;
import de.uniba.swt.dsl.validation.typing.ExprDataType;
import de.uniba.swt.dsl.validation.typing.HintDataType;
import de.uniba.swt.dsl.validation.typing.HintDataTypeUtl;
import de.uniba.swt.dsl.validation.typing.TypeCheckingTable;
import de.uniba.swt.dsl.validation.util.ValidationException;
import org.eclipse.emf.ecore.EStructuralFeature;

import javax.inject.Inject;

public class StatementValidator {

    @Inject
    TypeCheckingTable typeCheckingTable;

    /**
     * validate an statment
     * @param stmt statement
     */
    public void validate(Statement stmt) throws ValidationException {
        // SelectionStmt
        if (stmt instanceof SelectionStmt) {
            ExprDataType exprType = typeCheckingTable.computeDataType(((SelectionStmt) stmt).getExpr());
            checkTypes(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.SELECTION_STMT__EXPR);
        }

        // while statement
        if (stmt instanceof IterationStmt) {
            ExprDataType exprType = typeCheckingTable.computeDataType(((IterationStmt) stmt).getExpr());
            checkTypes(ExprDataType.ScalarBool, exprType, BahnPackage.Literals.ITERATION_STMT__EXPR);
        }

        // foreach
        if (stmt instanceof ForeachStmt) {
            var foreachStmt = (ForeachStmt) stmt;

            // ensure array is selected
            if (!ensureArray(foreachStmt.getArrayExpr())) {
                throw new ValidationException("Type Error: Expected type array", BahnPackage.Literals.FOREACH_STMT__ARRAY_EXPR);
            }

            // ensure current element is matched
            var expectedType = new ExprDataType(foreachStmt.getArrayExpr().getDecl().getType(), false);
            var actualType = new ExprDataType(foreachStmt.getDecl().getType(), foreachStmt.getDecl().isArray());
            checkTypes(expectedType, actualType, BahnPackage.Literals.FOREACH_STMT__DECL);
        }

        // break
        if (stmt instanceof BreakStmt) {
            // ensure having while outside
            if (!BahnUtil.isInsideIterationStmt(stmt)) {
                throw new ValidationException("break can only be used inside 'for..in' or 'while' statement", null);
            }
        }
    }

    public void validateAssignment(VariableAssignment assignment) throws ValidationException {
        var declType = getDeclDataType(assignment);
        if (declType == null)
            return;

        if (assignment.getExpr() != null) {
            checkExprType(declType, assignment.getExpr(), HintDataTypeUtl.from(declType.getDataType()), BahnPackage.Literals.VARIABLE_ASSIGNMENT__EXPR);
        }
        else if (assignment.getArrExprs() != null) {
            // ensure all elements in the same type
            if (!declType.isArray()) {
                throw new ValidationException("Type Error: Expected literal value instead of an array", BahnPackage.Literals.VARIABLE_ASSIGNMENT__ARR_EXPRS);
            }

            var itemType = new ExprDataType(declType.getDataType(), false);
            for (int i = 0; i < assignment.getArrExprs().size(); i++) {
                var arrExpr = assignment.getArrExprs().get(i);
                if (typeCheckingTable.canComputeType(arrExpr)) {
                    ExprDataType exprType = typeCheckingTable.computeDataType(arrExpr, HintDataTypeUtl.from(itemType.getDataType()));
                    if (!itemType.equals(exprType)) {
                        var err = String.format("Type Error: Expected type %s, actual type: %s", itemType.displayTypeName(), exprType.displayTypeName());
                        throw new ValidationException(err, BahnPackage.Literals.VARIABLE_ASSIGNMENT__ARR_EXPRS, i);
                    }
                }
            }
        }
    }

    private ExprDataType getDeclDataType(VariableAssignment assignment) {
        if (assignment.eContainer() instanceof VarDeclStmt) {
            return typeCheckingTable.getDataType(((VarDeclStmt) assignment.eContainer()).getDecl());
        }

        if (assignment.eContainer() instanceof AssignmentStmt) {
            return typeCheckingTable.computeDataType(((AssignmentStmt) assignment.eContainer()).getReferenceExpr());
        }

        return null;
    }

    private boolean ensureArray(ValuedReferenceExpr expr) {
        return expr.getDecl().isArray() && !expr.isLength() && expr.getIndexExpr() == null;
    }

    private void checkExprType(ExprDataType declType, Expression expr, HintDataType hintDataType, EStructuralFeature feature) throws ValidationException {
        if (typeCheckingTable.canComputeType(expr)) {
            ExprDataType exprType = typeCheckingTable.computeDataType(expr, hintDataType);
            checkTypes(declType, exprType, feature);
        }
    }

    private void checkTypes(ExprDataType expected, ExprDataType actual, EStructuralFeature feature) throws ValidationException {
        if (!expected.equals(actual)) {
            throw new ValidationException(String.format("Type Error: Expected type %s, actual type: %s", expected.displayTypeName(), actual.displayTypeName()), feature);
        }
    }
}


