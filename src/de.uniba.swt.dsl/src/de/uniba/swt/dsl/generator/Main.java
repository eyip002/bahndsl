/*
 * generated by Xtext 2.20.0
 */
package de.uniba.swt.dsl.generator;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import de.uniba.swt.dsl.BahnStandaloneSetup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

import de.uniba.swt.dsl.generator.cli.ArgOption;
import de.uniba.swt.dsl.generator.cli.ArgOptionContainer;
import de.uniba.swt.dsl.generator.cli.ArgParseResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

public class Main {

	public static void main(String[] args) {
		// define
		var container = new ArgOptionContainer(List.of(
				new ArgOption("o", "output folder", true, "path"),
				new ArgOption("v", "verbose logging")));

		// parse arg
		if (args.length == 0) {
			showHelp(container, true);
			return;
		}
		ArgParseResult result = null;
		try {
			result = container.parse(args, 1);
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
			showHelp(container, false);
			return;
		}

		// start
		if (result.hasOption("v")) {
			Logger.getRootLogger().setLevel(Level.DEBUG);
		}
		String outputPath = result.getValue("o", null);

		Injector injector = new BahnStandaloneSetup().createInjectorAndDoEMFRegistration();
		Main main = injector.getInstance(Main.class);

		boolean success = main.runGenerator(args[0], outputPath);
		if (!success) {
			System.exit(1);
		}
	}

	private static void showHelp(ArgOptionContainer container, boolean showAll) {
		if (showAll) {
			System.out.println("OVERVIEW: Bahn compiler\n");
		}
		System.out.println(container.formatHelp("bahnc file"));
		System.out.println("EXAMPLE: \n" +
				"  bahnc example.bahn\n" +
				"  bahnc example.bahn -o output/src-gen\n");
	}

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	@Inject
	private IResourceValidator validator;

	@Inject
	private GeneratorDelegate generator;

	@Inject
	private JavaIoFileSystemAccess fileAccess;

	protected boolean runGenerator(String filePath, String outputPath) {
		// load output
		File file = new File(filePath);
		if (outputPath == null || outputPath.isEmpty())
			outputPath = Paths.get(file.getAbsoluteFile().getParent(), "src-gen").toAbsolutePath().toString();

		// Load the resource
		ResourceSet set = resourceSetProvider.get();
		StandardLibHelper.loadStandardLibResource(set);

		Resource resource = set.getResource(URI.createFileURI(filePath), true);

		// Validate the resource
		List<Issue> list = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
		if (!list.isEmpty()) {
			boolean anyError = false;
			for (Issue issue : list) {
				anyError = issue.getSeverity() == Severity.ERROR;
				System.err.println(issue);
			}

			// stop
			if (anyError) {
				return false;
			}
		}

		// Configure and start the generator
		fileAccess.setOutputPath(outputPath);
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		generator.generate(resource, fileAccess, context);

		System.out.println(String.format("Code generation finished: %s", outputPath));
		return true;
	}
}