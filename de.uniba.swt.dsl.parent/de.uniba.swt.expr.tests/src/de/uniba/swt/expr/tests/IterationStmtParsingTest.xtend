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
import de.uniba.swt.expr.bahnExpr.BahnExpr

@ExtendWith(InjectionExtension)
@InjectWith(BahnExprInjectorProvider)
class IterationStmtParsingTest {
	@Inject
	ParseHelper<BahnExpr> parseHelper

	@Test
	def void testIterationStmt() {
		val result = parseHelper.parse('''
			def test()
				int max = 0
				for int a in array
					if a > max
						max = a
					end
				end
			end
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}
}
