package com.flow.flowboard.api.engine.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates condition expressions using JSONPath syntax.
 * 
 * Supported expressions:
 * - $.output.status === 'success'
 * - $.output.value > 100
 * - $.output.items.length > 0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionExpressionEvaluator {

    private final ObjectMapper objectMapper;

    // Pattern to match: $.path.to.value <operator> <value>
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "(\\$[.\\w\\[\\]]+)\\s*(===|!==|==|!=|>=|<=|>|<)\\s*(.+)");

    /**
     * Evaluates a condition expression against the provided context.
     *
     * @param expression The condition expression (e.g., "$.output.status ===
     *                   'success'")
     * @param context    The context containing the data to evaluate against
     * @return true if the condition is met, false otherwise
     */
    public boolean evaluate(String expression, Map<String, Object> context) {
        if (expression == null || expression.isBlank()) {
            log.warn("Empty expression, returning false");
            return false;
        }

        try {
            Matcher matcher = EXPRESSION_PATTERN.matcher(expression.trim());
            if (!matcher.matches()) {
                log.error("Invalid expression format: {}", expression);
                return false;
            }

            String jsonPath = matcher.group(1);
            String operator = matcher.group(2);
            String expectedValue = matcher.group(3).trim();

            // Convert context to JSON for JSONPath evaluation
            String contextJson = objectMapper.writeValueAsString(context);

            // Extract value using JSONPath
            Object actualValue;
            try {
                actualValue = JsonPath.read(contextJson, jsonPath);
            } catch (PathNotFoundException e) {
                log.warn("Path not found: {} in context", jsonPath);
                return false;
            }

            // Compare values
            return compare(actualValue, operator, expectedValue);

        } catch (Exception e) {
            log.error("Error evaluating expression: {}", expression, e);
            return false;
        }
    }

    private boolean compare(Object actualValue, String operator, String expectedValue) {
        // Handle null
        if (actualValue == null) {
            return "null".equals(expectedValue) || "===".equals(operator) && expectedValue.equals("null");
        }

        // Remove quotes from string expected values
        String cleanExpected = expectedValue;
        if ((expectedValue.startsWith("'") && expectedValue.endsWith("'")) ||
                (expectedValue.startsWith("\"") && expectedValue.endsWith("\""))) {
            cleanExpected = expectedValue.substring(1, expectedValue.length() - 1);
        }

        // Try numeric comparison first
        if (isNumericOperator(operator)) {
            try {
                double actualNum = toDouble(actualValue);
                double expectedNum = Double.parseDouble(cleanExpected);
                return compareNumbers(actualNum, operator, expectedNum);
            } catch (NumberFormatException e) {
                // Not a number, fall through to string comparison
            }
        }

        // String/equality comparison
        String actualStr = String.valueOf(actualValue);
        return switch (operator) {
            case "===", "==" -> actualStr.equals(cleanExpected);
            case "!==", "!=" -> !actualStr.equals(cleanExpected);
            case ">" -> actualStr.compareTo(cleanExpected) > 0;
            case "<" -> actualStr.compareTo(cleanExpected) < 0;
            case ">=" -> actualStr.compareTo(cleanExpected) >= 0;
            case "<=" -> actualStr.compareTo(cleanExpected) <= 0;
            default -> false;
        };
    }

    private boolean isNumericOperator(String operator) {
        return ">".equals(operator) || "<".equals(operator) ||
                ">=".equals(operator) || "<=".equals(operator);
    }

    private double toDouble(Object value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private boolean compareNumbers(double actual, String operator, double expected) {
        return switch (operator) {
            case ">" -> actual > expected;
            case "<" -> actual < expected;
            case ">=" -> actual >= expected;
            case "<=" -> actual <= expected;
            case "===", "==" -> actual == expected;
            case "!==", "!=" -> actual != expected;
            default -> false;
        };
    }
}
