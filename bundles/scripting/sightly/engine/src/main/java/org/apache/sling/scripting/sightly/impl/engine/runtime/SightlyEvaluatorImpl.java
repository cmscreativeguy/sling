/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.sling.scripting.sightly.impl.engine.runtime;

import javax.script.Bindings;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.sightly.impl.compiler.expression.Expression;
import org.apache.sling.scripting.sightly.impl.compiler.expression.ExpressionNode;
import org.apache.sling.scripting.sightly.impl.compiler.expression.node.*;
import org.apache.sling.scripting.sightly.impl.compiler.frontend.ExpressionParser;
import org.apache.sling.scripting.sightly.impl.compiler.frontend.Fragment;
import org.apache.sling.scripting.sightly.impl.compiler.frontend.Interpolation;
import org.apache.sling.scripting.sightly.impl.utils.RenderUtils;
import org.apache.sling.scripting.sightly.runtime.Evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service(Evaluator.class)
public class SightlyEvaluatorImpl implements Evaluator {

    private final Logger LOG = LoggerFactory.getLogger(SightlyEvaluatorImpl.class);

    @Override
    /**
     * @see Evaluator#evaluate(String, Bindings)
     */
    public String evaluate(String input, Bindings bindings) {
        StringBuilder sb = new StringBuilder();

        Interpolation interpolation = new ExpressionParser().parseInterpolation(input);

        for (int i = 0, n = interpolation.size(); i < n; i++) {
            Fragment fragment = interpolation.getFragment(i);

            if (fragment.isExpression()) {
                Object evaluated = evaluateExpression(fragment.getExpression(), bindings);
                if (evaluated != null) {
                    sb.append(RenderUtils.toString(evaluated));
                }
            }
            else if (fragment.isString()) {
                sb.append(fragment.getText());
            }
        }

        return sb.toString();
    }

    private Object evaluateExpression(Expression expression, Bindings bindings) {
        Object evaluated = evaluateExpressionNode(expression.getRoot(), bindings);
        return  evaluated;
    }

    private Object evaluateExpressionNode(ExpressionNode node, Bindings bindings) {
        Object evaluated = null;

        if (node instanceof NumericConstant) {
            NumericConstant constant = (NumericConstant)node;
            evaluated = constant.getValue();
        }
        else if (node instanceof BooleanConstant) {
            BooleanConstant constant = (BooleanConstant)node;
            evaluated = constant.getValue();
        }
        else if (node instanceof StringConstant) {
            StringConstant constant = (StringConstant)node;
            evaluated = constant.getText();
        }
        else if (node instanceof ArrayLiteral) {
            evaluated = evaluateArrayLiteral((ArrayLiteral)node, bindings);
        }
        else if (node instanceof Identifier) {
            evaluated = evaluateIdentifier((Identifier)node, bindings);
        }
        else if (node instanceof PropertyAccess) {
            evaluated = evaluatePropertyAccess((PropertyAccess)node, bindings);
        } else if (node instanceof UnaryOperation) {
            evaluated = evaluateUnaryOperation((UnaryOperation)node, bindings);
        } else if (node instanceof BinaryOperation) {
            evaluated = evaluateBinaryOperation((BinaryOperation)node, bindings);
        } else if (node instanceof TernaryOperator) {
            evaluated = evaluateTernaryOperation((TernaryOperator)node, bindings);
        } else {
            LOG.error("Expression node not supported: " + node.getClass());
        }
        return evaluated;
    }

    private Object[] evaluateArrayLiteral(ArrayLiteral array, Bindings bindings) {
        Object[] temp = new Object[array.getItems().size()];
        int i = 0;
        for (ExpressionNode en : array.getItems()) {
            temp[i++] = evaluateExpressionNode(en, bindings);
        }
        return temp;
    }

    private Object evaluateIdentifier(Identifier identifier, Bindings bindings) {
        SlingBindings slingBindings = new SlingBindings();
        slingBindings.putAll(bindings);
        Object target = slingBindings.get(identifier.getName());
        if (target == null && slingBindings.getRequest() != null) {
            target = slingBindings.getRequest().getAttribute(identifier.getName());
        }
        return target;
    }

    private Object evaluatePropertyAccess(PropertyAccess access, Bindings bindings) {
        Object target = evaluateExpressionNode(access.getTarget(), bindings);
        Object property = evaluateExpressionNode(access.getProperty(), bindings);
        if (target == null || property == null) {
            return null;
        }
        return RenderUtils.resolveProperty(target, property);

    }

    private Object evaluateUnaryOperation(UnaryOperation operation, Bindings bindings) {
        UnaryOperator operator = operation.getOperator();
        Object target = evaluateExpressionNode(operation.getTarget(), bindings);
        return operator.eval(target);
    }

    private Object evaluateBinaryOperation(BinaryOperation operation, Bindings bindings) {
        BinaryOperator operator = operation.getOperator();
        Object leftOperand = evaluateExpressionNode(operation.getLeftOperand(), bindings);
        Object rightOperand = evaluateExpressionNode(operation.getRightOperand(), bindings);
        return operator.eval(leftOperand, rightOperand);
    }

    private Object evaluateTernaryOperation(TernaryOperator operation, Bindings bindings) {
        boolean condition = RenderUtils.toBoolean(evaluateExpressionNode(operation.getCondition(), bindings));
        boolean thenVal = RenderUtils.toBoolean(evaluateExpressionNode(operation.getThenBranch(), bindings));
        boolean elseVal = RenderUtils.toBoolean(evaluateExpressionNode(operation.getElseBranch(), bindings));
        return condition ? thenVal : elseVal;
    }

}
