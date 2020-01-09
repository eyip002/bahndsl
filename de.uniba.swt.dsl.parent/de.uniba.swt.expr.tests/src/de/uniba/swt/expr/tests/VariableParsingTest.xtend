/*
 * generated by Xtext 2.20.0
 */
package de.uniba.swt.expr.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import de.uniba.swt.expr.bahnexpr.BahnExpr

@ExtendWith(InjectionExtension)
@InjectWith(BahnExprInjectorProvider)
class VariableParsingTest {
	@Inject
	ParseHelper<BahnExpr> parseHelper

	@Test
	def void testScalarVarDeclStmt() {
		val result = parseHelper.parse('''
			def test()
				int a = 3
				float b = 4.5
				bool c = true
			end
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}

	@Test
	def void testArrayVarDeclStmt() {
		val result = parseHelper.parse('''
			def test()
				int a[2] = {3,4}
				float b[1+ 2] = {4.5, 2, 4}
				bool c[1] = {true}
			end
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}

	@Test
	def void testScalarAssignmentStmt() {
		val result = parseHelper.parse('''
			def test()
				a = 3
				b = 4.5
				c = true
			end
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}

	@Test
	def void testArrayAssignmentStmt() {
		val result = parseHelper.parse('''
			def test()
				a[2] = {3,4}
				b[1+ 2] = {4.5, 2, 4}
				c[1] = {true}
			end
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}
}
