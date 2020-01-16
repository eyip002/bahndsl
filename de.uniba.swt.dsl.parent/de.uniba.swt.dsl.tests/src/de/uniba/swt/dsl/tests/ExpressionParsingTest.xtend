/*
 * generated by Xtext 2.20.0
 */
package de.uniba.swt.dsl.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import de.uniba.swt.dsl.bahn.RootModule

@ExtendWith(InjectionExtension)
@InjectWith(BahnInjectorProvider)
class ExpressionParsingTest {
	
	@Inject extension ParseHelper<RootModule>
	@Inject extension ParserTestHelper

	@Test
	def void testLogicalOrExpr() {
		'''
			module test def test()
				bool result = true || false
			end end
		'''.parse.assertNoParsingErrors
	}
	
	@Test
	def void testLogicalAndExpr() {
		'''
			module test def test()
				bool result = true && false
				bool result = true && false || true
			end end
		'''.parse.assertNoParsingErrors
	}
	
	@Test
	def void testEqualityExpr() {
		'''
			module test def test()
				bool result1 = 3 == 4
				result1 = 3 != 4
				result2 = true == true
				result3 = true != false
				result4 = 1 == 2
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testEqualityExprReference() {
		'''
			module test def test()
				bool result1 = 3 == 4
				result1 = 3 != 4
				result2 = true == true
				result3 = true != false
				result4 = 1 == 2
			end end
		'''.parse.assertNoParsingErrors
	}
	
	@Test
	def void testRelationalExpr() {
		'''
			module test def test()
				bool success = 1 > 2
				success = 1 <= 2
				success = 1 > 2
				success = 1 >= 2
			end end
		'''.parse.assertNoParsingErrors
	}
	
	@Test
	def void testAdditiveExpr() {
		'''
			module test def test()
				int a = 3 + 4
				int b = 4 - 3
				int c = 3 - 4 +5
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testMultiplicativeExpr() {
		'''
			module test def test()
				int a = 3 * 4
				int b = 4 / 3
				float c = 3 * 4 / 5
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testUnaryExpr() {
		'''
			module test def test()
				bool result = !true
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
    def void testSignedNumber() {
        '''
            module test def test()
                int a = -3
                int a = -4
            end end
        '''.parse.assertNoParsingErrors
    }

	@Test
	def void testParenthesizedExpr() {
		'''
			module test def test()
				int a = 5 * (3 + 4)
				bool result = true || (false && true)
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testBooleanLiteral() {
		'''
			module test def test()
				bool result = true
				result = false
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testNumberLiteral() {
		'''
			module test def test()
				int a = 3
				float b = 4.5
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testStringLiteral() {
		'''
			module test def test()
				string aspectRed = "red"
				string aspectReverse = "reverse"
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testValuedReferenceExpr() {
		'''
			module test def test()
				float b = a * 5
				b = a + 5
				b = c || true
				b = d == true
				b = d > 3
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testArrayValuedReferenceExpr() {
		'''
			module test def test()
				float b = a[0] * 5
				b = a[1] + 5
				b = c[2] || true
				b = d[3] == true
				b = d[4] > 3
			end end
		'''.parse.assertNoParsingErrors
	}

	@Test
	def void testPropertyValuedReferenceExpr() {
		'''
			module test def test()
				float b = a.value * 5
				b = a.value + 5
				b = c.result || true
				b = d.result == true
				b = d.result > 3
			end end
		'''.parse.assertNoParsingErrors
	}
}
