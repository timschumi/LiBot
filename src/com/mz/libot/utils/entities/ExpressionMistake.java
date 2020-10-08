package com.mz.libot.utils.entities;

import java.math.BigDecimal;
import java.math.MathContext;

import com.mz.expGen.Expression;

/**
 * A class used to indicate mathematical mistake
 * 
 * @author Marko Zajc
 */
public class ExpressionMistake {

	private Expression expression;
	private BigDecimal answer;
	private BigDecimal mistake;

	/**
	 * A class used to indicate mathematical mistake
	 * 
	 * @param expression
	 *            original expression
	 * @param answer
	 *            user's answer
	 */
	public ExpressionMistake(Expression expression, BigDecimal answer) {
		this.expression = expression;
		this.answer = answer.stripTrailingZeros();
		this.mistake = expression.getSolution().subtract(answer).abs(MathContext.DECIMAL128).stripTrailingZeros();
	}

	/**
	 * Returns the original expression
	 * 
	 * @return the original expression
	 */
	public Expression getExpression() {
		return this.expression;
	}

	/**
	 * Returns user's answer
	 * 
	 * @return user's answer
	 */
	public BigDecimal getAnswer() {
		return this.answer;
	}

	/**
	 * Returns mathematical mistake. This can be calculated with subtracting answer from
	 * expression's solution and getting absolute value for the result
	 * 
	 * @return mathematical mistake
	 */
	public BigDecimal getMistake() {
		return this.mistake;
	}

}
